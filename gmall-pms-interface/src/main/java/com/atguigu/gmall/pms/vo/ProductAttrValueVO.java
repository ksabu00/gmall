package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 注意：attrValue的值在实体类中是String，而传过来时参数名是valueSelected，值为集合类型。
 *
 * 所以，必须扩展ProductAttrValueEntity类：
 */
public class ProductAttrValueVO extends ProductAttrValueEntity {
    public void setValueSelected (List<Object> valueSelected){
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
