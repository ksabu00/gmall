package com.atguigu.gmall.pms.api;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    // 分页查询spu
    @ApiOperation("分页查询(排序)")
    @PostMapping("pms/spuinfo/list")
    public Resp<List<SpuInfoEntity>> querySpuPage(@RequestBody QueryCondition queryCondition);

    // 根据spuid查询sku
    @ApiOperation("查询spu下的sku")
    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkuInfoBySpuId(@PathVariable("spuId")Long spuId);

    // 根据brandId查询品牌
    @ApiOperation("详情查询")
    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    // 根据categoryId查询分类
    @ApiOperation("详情查询")
    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCateGoryById(@PathVariable("catId") Long catId);

    @GetMapping("pms/category")
    @ApiOperation("根据分类等级id来查询分类")
    public Resp<List<CategoryEntity>> queryCategory(@RequestParam(value = "level", defaultValue = "0")Integer level,
                                                    @RequestParam(value = "parentCid", required = false)Long parentCid);

    // 根据skuId查询库存


    // 根据spuId查询检索属性
    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<SpuAttributeValueVO>> querySearchAttrValue(@PathVariable("spuId")Long spuId);


    @GetMapping("pms/category/{pid}")
    public Resp<List<CategoryVO>> queryCategoryWithSub(@PathVariable("pid")Long pid);

}
