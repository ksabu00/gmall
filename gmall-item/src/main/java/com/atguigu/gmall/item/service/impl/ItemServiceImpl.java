package com.atguigu.gmall.item.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public ItemVO item(Long skuId)
    {
        ItemVO itemVO = new ItemVO();
        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            // 1.查询sku信息
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                BeanUtils.copyProperties(skuInfoEntity, itemVO);
            }
            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> brandCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 2.品牌
            Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
            itemVO.setBrand(brandEntityResp.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> categoryCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 3.分类
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCateGoryById(skuInfoEntity.getCatalogId());
            itemVO.setCategory(categoryEntityResp.getData());
        }, threadPoolExecutor);


        CompletableFuture<Void> spuCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 4.spu信息
            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(skuInfoEntity.getSpuId());
            itemVO.setSpuInfo(spuInfoEntityResp.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> picsCompletableFuture = CompletableFuture.runAsync(() -> {
            // 5.设置图片信息
            Resp<List<String>> pics = this.gmallPmsClient.queryPicsBySkuId(skuId);
            itemVO.setPics(pics.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> ItemCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 6.营销信息
            Resp<List<ItemSaleVO>> itemSaleVOs = this.gmallSmsClient.querySaleItemVOs(skuInfoEntity.getSkuId());
            itemVO.setSales(itemSaleVOs.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> wareCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 7.是否有货
            Resp<List<WareSkuEntity>> wareSkuResp = this.gmallWmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
            itemVO.setStore(wareSkuEntities.stream().anyMatch(t -> t.getStock() > 0));
        }, threadPoolExecutor);


        CompletableFuture<Void> skuSaleCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 8.销售属性
            Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsClient.querySaleAttrValues(skuInfoEntity.getSpuId());
            itemVO.setSkuSales(saleAttrValueResp.getData());
        }, threadPoolExecutor);


        CompletableFuture<Void> spuDescCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 9.spu的描述信息
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescById(skuInfoEntity.getSpuId());
            itemVO.setDesc(spuInfoDescEntityResp.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> groupCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((skuInfoEntity) -> {
            // 10.规格属性分组
            Resp<List<GroupVO>> listResp = this.gmallPmsClient.queryGroupVOByCid(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
            itemVO.setGroups(listResp.getData());
        }, threadPoolExecutor);

        try {
            CompletableFuture.allOf(skuInfoEntityCompletableFuture, brandCompletableFuture, categoryCompletableFuture, spuCompletableFuture, picsCompletableFuture,
                    ItemCompletableFuture, wareCompletableFuture, skuSaleCompletableFuture, spuDescCompletableFuture, groupCompletableFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return itemVO;

    }
}

/*@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Override
    public ItemVO item(Long skuId)
    {

        ItemVO itemVO = new ItemVO();
        // 1.查询sku信息
        Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(skuId);
        SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
        BeanUtils.copyProperties(skuInfoEntity, itemVO);

        Long spuId = skuInfoEntity.getSpuId();
        // 2.品牌
        Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
        itemVO.setBrand(brandEntityResp.getData());

        // 3.分类
        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCateGoryById(skuInfoEntity.getCatalogId());
        itemVO.setCategory(categoryEntityResp.getData());


        // 4.spu信息
        Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(spuId);
        itemVO.setSpuInfo(spuInfoEntityResp.getData());


        // 5.设置图片信息
        Resp<List<String>>  pics = this.gmallPmsClient.queryPicsBySkuId(skuId);
        itemVO.setPics(pics.getData());

        // 6.营销信息
        Resp<List<ItemSaleVO>> itemSaleVOs = this.gmallSmsClient.querySaleItemVOs(skuId);
        itemVO.setSales(itemSaleVOs.getData());

        // 7.是否有货
        Resp<List<WareSkuEntity>> wareSkuResp = this.gmallWmsClient.queryWareSkuBySkuId(skuId);
        List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
        itemVO.setStore(wareSkuEntities.stream().anyMatch(t-> t.getStock() > 0));
        // 8.销售属性
        Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsClient.querySaleAttrValues(spuId);
        itemVO.setSkuSales(saleAttrValueResp.getData());

        // 9.spu的描述信息
        Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescById(spuId);
        itemVO.setDesc(spuInfoDescEntityResp.getData());

        // 10.规格属性分组
        Resp<List<GroupVO>> listResp = this.gmallPmsClient.queryGroupVOByCid(skuInfoEntity.getCatalogId(), spuId);
        itemVO.setGroups(listResp.getData());

        return itemVO;
    }
}*/
