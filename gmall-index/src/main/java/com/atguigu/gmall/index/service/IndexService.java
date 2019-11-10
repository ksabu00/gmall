package com.atguigu.gmall.index.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IndexService {

    List<CategoryEntity> queryLevel1Category();

    List<CategoryVO> querySubCategory(Long pid);

    String testLock();

    String read();

    String write();

    String latch();

    String out();
}
