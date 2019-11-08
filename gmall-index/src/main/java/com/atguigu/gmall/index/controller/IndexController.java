package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index/cates")
public class IndexController {
    @Autowired
    private IndexService indexService;
    @Autowired
    private GmallPmsFeign gmallPmsFeign;

    @GetMapping
    public Resp<List<CategoryEntity>> queryLevel1Category(){

        List<CategoryEntity> categoryEntities = this.indexService.queryLevel1Category();
        return Resp.ok(categoryEntities);
    }

    @GetMapping("{pid}")
    public Resp<List<CategoryVO>> querySubCategory(@PathVariable("pid")Long pid){

        List<CategoryVO> categoryVOS = this.indexService.querySubCategory(pid);
        return Resp.ok(categoryVOS);
    }

    // redis缓存测试
    @GetMapping("testLock")
    public Resp<Object> testLock(){
        String msg = this.indexService.testLock();


        return Resp.ok("data：" + msg );
    }
}
