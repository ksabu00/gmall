package com.atguigu.gmall.pms.controller;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RequestMapping("pms/oss")
@RestController
public class OSSController {

//    LTAI4FuPx2SYTvFzYW3xyWmJ
//    XDaPWWojYArZ7x7LCNPafsLHEwXWx7
    String accessId = "LTAI4FuPx2SYTvFzYW3xyWmJ"; // 请填写您的AccessKeyId。
    String accessKey = "XDaPWWojYArZ7x7LCNPafsLHEwXWx7"; // 请填写您的AccessKeySecret。
    String endpoint = "oss-cn-shanghai.aliyuncs.com"; // 请填写您的 endpoint。
    String bucket = "oss-gmall"; // 请填写您的 bucketname 。
    String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
    // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
    //String callbackUrl = "http://88.88.88.88:8888";
    // 图片目录，每天一个目录
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dir = sdf.format(new Date()); // 用户上传文件时指定的前缀。

    @GetMapping("policy")
    public Resp<Object> policy() throws UnsupportedEncodingException {

        OSSClient client = new OSSClient(endpoint, accessId, accessKey);

        long expireTime = 30;
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
        Date expiration = new Date(expireEndTime);
        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

        String postPolicy = client.generatePostPolicy(expiration, policyConds);
        byte[] binaryData = postPolicy.getBytes("utf-8");
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = client.calculatePostSignature(postPolicy);

        Map<String, String> respMap = new LinkedHashMap<String, String>();
        respMap.put("accessid", accessId);
        respMap.put("policy", encodedPolicy);
        respMap.put("signature", postSignature);
        respMap.put("dir", dir);
        respMap.put("host", host);
        respMap.put("expire", String.valueOf(expireEndTime / 1000));
        // respMap.put("expire", formatISO8601Date(expiration));

        return Resp.ok(respMap);
    }

    /**
     * spu属性值
     *
     * @author ksabu00
     * @email 2054693@qq.com
     * @date 2019-10-28 20:29:35
     */
    @Api(tags = "spu属性值 管理")
    @RestController
    @RequestMapping("pms/productattrvalue")
    public static class ProductAttrValueController {
        @Autowired
        private ProductAttrValueService productAttrValueService;

        /**
         * 列表
         */
        @ApiOperation("分页查询(排序)")
        @GetMapping("/list")
        @PreAuthorize("hasAuthority('pms:productattrvalue:list')")
        public Resp<PageVo> list(QueryCondition queryCondition) {
            PageVo page = productAttrValueService.queryPage(queryCondition);

            return Resp.ok(page);
        }


        /**
         * 信息
         */
        @ApiOperation("详情查询")
        @GetMapping("/info/{id}")
        @PreAuthorize("hasAuthority('pms:productattrvalue:info')")
        public Resp<ProductAttrValueEntity> info(@PathVariable("id") Long id){
            ProductAttrValueEntity productAttrValue = productAttrValueService.getById(id);

            return Resp.ok(productAttrValue);
        }

        /**
         * 保存
         */
        @ApiOperation("保存")
        @PostMapping("/save")
        @PreAuthorize("hasAuthority('pms:productattrvalue:save')")
        public Resp<Object> save(@RequestBody ProductAttrValueEntity productAttrValue){
            productAttrValueService.save(productAttrValue);

            return Resp.ok(null);
        }

        /**
         * 修改
         */
        @ApiOperation("修改")
        @PostMapping("/update")
        @PreAuthorize("hasAuthority('pms:productattrvalue:update')")
        public Resp<Object> update(@RequestBody ProductAttrValueEntity productAttrValue){
            productAttrValueService.updateById(productAttrValue);

            return Resp.ok(null);
        }

        /**
         * 删除
         */
        @ApiOperation("删除")
        @PostMapping("/delete")
        @PreAuthorize("hasAuthority('pms:productattrvalue:delete')")
        public Resp<Object> delete(@RequestBody Long[] ids){
            productAttrValueService.removeByIds(Arrays.asList(ids));

            return Resp.ok(null);
        }

    }
}
