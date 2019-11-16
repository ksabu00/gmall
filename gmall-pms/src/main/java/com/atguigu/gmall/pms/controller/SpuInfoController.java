package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.service.SpuInfoService;




/**
 * spu信息
 *
 * @author ksabu00
 * @email 2054693@qq.com
 * @date 2019-10-28 20:29:35
 */
@Api(tags = "spu信息 管理")
@RestController
@RequestMapping("pms/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 保存
     */
    @ApiOperation("保存spu")
    @PostMapping("/save")
    //@PreAuthorize("hasAuthority('pms:spuinfo:save')")
    public Resp<Object> saveSale(@RequestBody SpuInfoVO spuInfoVo){
        //this.spuInfoService.saveSpuInfoVO(spuInfoVo);
        this.spuInfoService.saveSpuInfoVO(spuInfoVo);
        return Resp.ok("新增成功");
    }

    @GetMapping
    public Resp<PageVo> queryByCatId(QueryCondition condition, @RequestParam("catId")Long catId){
        PageVo pageVo = this.spuInfoService.querySpuByCatId(condition, catId);

        return Resp.ok(pageVo);
    }
    /**
     * 列表
     */
/*    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:spuinfo:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = spuInfoService.queryPage(queryCondition);

        return Resp.ok(page);
    }*/
    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('pms:spuinfo:list')")
    public Resp<List<SpuInfoEntity>> querySpuPage(@RequestBody QueryCondition queryCondition) {
        PageVo page = spuInfoService.queryPage(queryCondition);

        return Resp.ok((List<SpuInfoEntity>)page.getList());
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('pms:spuinfo:info')")
    public Resp<SpuInfoEntity> info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return Resp.ok(spuInfo);
    }

    public void sendMsg(Long spuId, String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", spuId);
        map.put("type", type);
        this.amqpTemplate.convertAndSend("GMALL-ITEM-EXCHANGE", "item." + type, map);
        System.out.println("222");
    }
    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:spuinfo:update')")
    public Resp<Object> update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);
        this.sendMsg(spuInfo.getId(), "update");
        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:spuinfo:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }
    /*@ApiOperation("分页查询已发布spu商品信息")
    @PostMapping("{status}")
    public Resp<List<SpuInfoEntity>> querySpuInfoByStatus(@RequestBody QueryCondition condition, @PathVariable("status")Integer status){

        IPage<SpuInfoEntity> spuInfoEntityIPage = this.spuInfoService.page(new Query<SpuInfoEntity>().getPage(condition), new QueryWrapper<SpuInfoEntity>().eq("publish_status", status));
        return Resp.ok(spuInfoEntityIPage.getRecords());
    }*/


}
