package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.dao.BrandDao;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private BrandService brandService;

    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setDescript("hellogmall");
//        brandEntity.setFirstLetter("s");
//        brandEntity.setShowStatus(1);
//        brandEntity.setName("test2");
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("name", "fds发多少");
//        System.out.println(this.brandDao.selectList(new QueryWrapper<BrandEntity>().eq("name", "test2")));
        //IPage<BrandEntity> page = this.brandDao.page(new Page<BrandEntity>(2l, 2l), new QueryWrapper<BrandEntity>());
        IPage<BrandEntity> page = this.brandService.page(new Page<>(2, 2), new QueryWrapper<BrandEntity>());
        System.out.println("当前页数据为：" + page.getRecords());
        System.out.println("总页数：" + page.getPages());
        System.out.println("总条数" + page.getTotal());
    }

}
