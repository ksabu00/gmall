package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.PublicKey;
import java.util.List;

/*@Service
public class IndexServiceImpl{
}*/

@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private GmallPmsFeign gmallPmsFeign;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "index:category";

    @Override
    public List<CategoryEntity> queryLevel1Category() {

        this.stringRedisTemplate.opsForValue().set("redis", "value1");
        System.out.println("===========" + this.stringRedisTemplate.opsForValue().get("redis"));

        Resp<List<CategoryEntity>> categoryResp = this.gmallPmsFeign.queryCategory(1, null);
        return categoryResp.getData();
    }

    @Override
    public List<CategoryVO> querySubCategory(Long pid) {
        // 1:查询缓存，缓存中有直接返回
        String categoryString = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(categoryString)){
           return JSON.parseArray(categoryString, CategoryVO.class);
        }
        // 2:缓存中没有，查询数据库
        Resp<List<CategoryVO>> listResp = this.gmallPmsFeign.queryCategoryWithSub(pid);
        List<CategoryVO> categoryVOS = listResp.getData();

        // 3:查询完之后，放入缓存
        if (!CollectionUtils.isEmpty(categoryVOS)) {
            this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryVOS));
        }
        return categoryVOS;
    }

    @Override
    public String testLock() {
        String val = this.stringRedisTemplate.opsForValue().get("key1");

        if (StringUtils.isEmpty(val)){
            return null;
        }
        int num = Integer.parseInt(val);

        this.stringRedisTemplate.opsForValue().set("key1", String.valueOf(++num));
        return "已经添加成功！！！";
    }
}
