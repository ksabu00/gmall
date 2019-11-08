package com.atguigu.gmall.search;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import com.atguigu.gmall.search.VO.GoodsVO;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;


@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    private JestClient jestClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallPmsClient gmallPmsClient;

    /*@Test
    public void importData(){
        Long pageNum = 1L;
        Long pageSize = 100L;

        do{
            // 分页查询已上架商品，即spu中publish_status=1的商品
            QueryCondition condition = new QueryCondition();
            condition.setPage(pageNum);
            condition.setLimit(pageSize);
            System.out.println(condition);
            Resp<List<SpuInfoEntity>> listResp = this.gmallPmsClient.querySpuPage(condition);
            List<SpuInfoEntity> spuInfoEntities = listResp.getData();
            // 判断spuInfoEntities是否为null

            System.out.println();
            // 遍历spuInfo下的所有sku导入到索引库中
            for (SpuInfoEntity spuInfoEntity : spuInfoEntities){
                //System.out.println(spuInfoEntity.getId());
                Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsClient.querySkuInfoBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                if (CollectionUtils.isEmpty(skuInfoEntities)){
                    continue;
                }
                skuInfoEntities.forEach(skuInfoEntity ->{
                    GoodsVO goodsVO = new GoodsVO();

                    // 设置相关sku相关数据
                    goodsVO.setName(skuInfoEntity.getSkuTitle());
                    goodsVO.setId(skuInfoEntity.getSkuId());
                    goodsVO.setPic(skuInfoEntity.getSkuDefaultImg());
                    goodsVO.setPrice(skuInfoEntity.getPrice());

                    // 设置品牌相关的
                    Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
                    BrandEntity brandEntity = brandEntityResp.getData();
                    if (brandEntity != null){
                        goodsVO.setBrandId(skuInfoEntity.getBrandId());
                        goodsVO.setBrandName(brandEntity.getName());
                    }

                    // 设置分类相关的
                    Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCateGoryById(skuInfoEntity.getCatalogId());
                    CategoryEntity categoryEntity = categoryEntityResp.getData();
                    if (categoryEntity != null){
                        goodsVO.setProductCategoryName(categoryEntity.getName());
                        goodsVO.setProductCategoryId(skuInfoEntity.getCatalogId());
                    }

                    // 设置搜索属性
                    //Resp<List<SpuAttributeValueVO>> searchAttrValueResp = this.gmallPmsClient.querySearchAttrValue(spuInfoEntity.getId());
                    //List<SpuAttributeValueVO> spuAttributeValueVOList = searchAttrValueResp.getData();
                    //goodsVO.setAttrValueList(spuAttributeValueVOList);

                    // 设置搜索属性
                    System.out.println(spuInfoEntity.getId());
                    Resp<List<SpuAttributeValueVO>> searchAttrValueResp = this.gmallPmsClient.querySearchAttrValue(spuInfoEntity.getId());
                    System.out.println(searchAttrValueResp);
                    List<SpuAttributeValueVO> spuAttributeValueVOList = searchAttrValueResp.getData();
                    System.out.println("打印数据 " + spuAttributeValueVOList);
                    goodsVO.setAttrValueList(spuAttributeValueVOList);


                    // 库存信息
                    Resp<List<WareSkuEntity>> resp = this.gmallWmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = resp.getData();
                    if (wareSkuEntities.stream().anyMatch(t ->t.getStock() > 0)){
                        goodsVO.setStock(100L);
                    }else {
                        goodsVO.setStock(0L);                    }
                    Index index = new Index.Builder(goodsVO).index("goods").type("info").id(skuInfoEntity.getSkuId().toString()).build();
                    try {
                        this.jestClient.execute(index);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            pageSize = Long.valueOf(spuInfoEntities.size()); // 获取当前页的记录数
            pageNum++; // 下一页
        }while (pageSize == 100);// 循环条件
    }*/
    /*
    @Test
    void contextLoads() {
    }

    *//**
     * 有该记录就更新，没有则新增（以id为判断标准）
     * 会把没有设置值的字段更新为null
     * @throws
     *//*
    @Test
    public void create() throws Exception {
        //Person p1 = new Person("ksabu", "123456", 18);
        Person p1 = new Person("wang5", "123456789", 28);

        Index action = new Index.Builder(p1).index("user").type("info").id("2").build();

        jestClient.execute(action);
    }
    @Test
    public void update() {

        Person p1 = new Person("wang5", "123456789", 28);
        Map<String, Object> map= new HashMap<String, Object>();
        map.put("doc",p1);

        Update action = new Update.Builder(map).index("user").type("info").id("1").build();

        try {
            System.out.println(jestClient.execute(action));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void search() throws IOException {
        String query = "{\n" +
                "  \"query\": {\n" +
                "  \"match_all\": {}\n" +
                "  }\n" +
                "}";
        *//*Search search = new Search.Builder(query).addIndex("user").addType("info").build();

        SearchResult searchResult = jestClient.execute(search);
        System.out.println(searchResult.getSourceAsObject(Person.class, false));
        List<SearchResult.Hit<Person, Void>> hits =searchResult.getHits(Person.class);
        hits.forEach(hit -> {
            System.out.println(hit.source);
        });*//*
        Get get = new Get.Builder("user", "info").build();
        System.out.println(get.toString());
    }
    @Test
    public void delete() throws IOException {

        Delete action = new Delete.Builder("2").index("user").type("info").build();
        jestClient.execute(action);
    }
}
@Data
@AllArgsConstructor
@NoArgsConstructor
class Person{
    private String name;
    private String password;
    private Integer age;*/
}
