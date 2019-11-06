package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.VO.GoodsVO;
import com.atguigu.gmall.search.VO.SearchParamVO;
import com.atguigu.gmall.search.VO.SearchResponse;
import com.atguigu.gmall.search.VO.SearchResponseAttrVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private JestClient jestClient;

    public void search(SearchParamVO searchParamVO){
        try {
            String dsl = buildDSL(searchParamVO);
            System.out.println("查询条件：" + dsl);
            Search search = new Search.Builder(dsl).addIndex("goods").addType("info").build();
            SearchResult searchResult = this.jestClient.execute(search);

            SearchResponse response = parseResult(searchResult);

            // 设置分页参数
            response.setPageNum(searchParamVO.getPageNum());
            response.setPageSize(searchParamVO.getPageSize());
            response.setTotal(searchResult.getTotal());
            System.out.println(searchResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 解析数据
    private SearchResponse parseResult(SearchResult result) {
        SearchResponse response = new SearchResponse();
        MetricAggregation aggregations = result.getAggregations();

        // 解析品牌的聚合结果集
        // 获取品牌聚合
        TermsAggregation brandAgg = aggregations.getTermsAggregation("brandAgg");
        // 获取品牌聚合的所有桶
        List<TermsAggregation.Entry> buckets = brandAgg.getBuckets();
        // 判断品牌聚合是否为空
        if (!CollectionUtils.isEmpty(buckets)){
            // 初始化
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setName("品牌");// 写死品牌聚合名称
            List<String> brandValues = buckets.stream().map(bucket -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", bucket.getKeyAsString());
                TermsAggregation brandNameAgg = bucket.getTermsAggregation("brandNameAgg");
                map.put("name", brandNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            attrVO.setValue(brandValues);// 设置品牌的所有聚合值
            response.setBrand(attrVO);
        }

        // 解析分类的聚合结果集
         TermsAggregation categoryAgg = aggregations.getTermsAggregation("categoryAgg");
         List<TermsAggregation.Entry> catBuckets = categoryAgg.getBuckets();
         if (!CollectionUtils.isEmpty(catBuckets))
         {
             SearchResponseAttrVO categoryVo = new SearchResponseAttrVO();
             categoryVo.setName("分类");
             List<String> categoryValues = catBuckets.stream().map(bucket -> {
                 Map<String, Object> map = new HashMap<>();
                 map.put("id", bucket.getKeyAsString());
                 TermsAggregation categoryNameAgg = bucket.getTermsAggregation("categoryNameAgg");

                 map.put("name", categoryNameAgg.getBuckets().get(0).getKeyAsString());
                 return JSON.toJSONString(map);
             }).collect(Collectors.toList());

             categoryVo.setValue(categoryValues);
             response.setCatelog(categoryVo);
         }

        // 解析检索属性的结果集
        ChildrenAggregation childrenAggregation = aggregations.getChildrenAggregation("attrAgg");
        TermsAggregation attrIdAgg = childrenAggregation.getTermsAggregation("attrIdAgg");
        List<SearchResponseAttrVO> attrVOS = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setProductAttributeId((Long.valueOf(bucket.getKeyAsString())));
            // 获取收索属性的子聚合（搜索属性名）
            TermsAggregation attrNameAgg = bucket.getTermsAggregation("attrNameAgg");
            attrVO.setName(attrNameAgg.getBuckets().get(0).getKeyAsString());

            // 获取搜索属性的子聚合
            TermsAggregation attrValueAgg = bucket.getTermsAggregation("attrValueAgg");
            List<String> values = attrValueAgg.getBuckets().stream().map(bucket1 ->
                    bucket.getKeyAsString()).collect(Collectors.toList());
            attrVO.setValue(values);
            return attrVO;
        }).collect(Collectors.toList());
        response.setAttrs(attrVOS);
        // 解析商品列表的结果集
        List<GoodsVO> goodVOS = result.getSourceAsObjectList(GoodsVO.class, false);
        response.setProducts(goodVOS);

        return response;
    }

    /**
     *
     * @param searchParamVO
     * @return
     */
    // search?catelog3=手机&catelog3=配件&brand=1&props=43:3g-4g-5g&props=45:4.7-5.0
    // &order=2:asc/desc&priceFrom=100&priceTo=10000&pageNum=1&pageSize=12&keyword=手机
    private String buildDSL(SearchParamVO searchParamVO) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1：完成查询和过滤条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 获取用户输入的检索关键字并判断是否为空
        //检索的关键字
        String keyword = searchParamVO.getKeyword();
        if (StringUtils.isNotBlank(keyword)){
            boolQuery.must(QueryBuilders.matchQuery("name", keyword).operator(Operator.AND));
        }
        // 过滤条件
        // 品牌
        String[] brand = searchParamVO.getBrand();
        if (ArrayUtils.isNotEmpty(brand)){
            boolQuery.filter(QueryBuilders.termsQuery("brandId", brand));
        }
        // 分类
        String[] catelog3 = searchParamVO.getCatelog3();
        if (ArrayUtils.isNotEmpty(catelog3)){
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryId", catelog3));
        }
        // 规格属性
        //页面提交的数组
        String[] props = searchParamVO.getProps();
        if (ArrayUtils.isNotEmpty(props)){
            for (String prop : props){
                String[] attr = StringUtils.split(prop,":");
                if (attr !=null && attr.length ==2){
                    BoolQueryBuilder propBoolQuery = QueryBuilders.boolQuery();
                    propBoolQuery.must(QueryBuilders.termQuery("attrValueList.productAttributeId", attr[0]));
                    String[] values = StringUtils.split(attr[1], "-");
                    propBoolQuery.must(QueryBuilders.termsQuery("attrValueList.value", values));
                    boolQuery.filter(QueryBuilders.nestedQuery("attrValueList", propBoolQuery, ScoreMode.None));
                }
            }
        }
        sourceBuilder.query(boolQuery);
        // 2：完成分页构建条件
        Integer pageNum = searchParamVO.getPageNum();
        Integer pageSize = searchParamVO.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        // 3：完成排序条件构建
        // order=1:asc  排序规则   0:asc
        String order = searchParamVO.getOrder();
        if(StringUtils.isNotBlank(order)){
            String[] orders = StringUtils.split(order, ":");
            if (orders !=null && orders.length == 2){
                SortOrder sortOrder = StringUtils.equals("asc", orders[1]) ? SortOrder.ASC : SortOrder.DESC;
                switch (orders[0]){
                    // 0：综合排序  1：销量  2：价格
                    case "0":sourceBuilder.sort("_score", sortOrder);break;
                    case "1":sourceBuilder.sort("sale", sortOrder);break;
                    case "2":sourceBuilder.sort("price", sortOrder);break;
                    default:break;
                }
            }
        }
        // 4：完成高亮条件构建
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        sourceBuilder.highlighter(highlightBuilder);


        // 5：完成聚合条件构造
        // 品牌聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandName").field("brandNameAgg")));


        // 分类聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryAgg").field("productCategoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("productCategoryName ")));
        // 搜索属性
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrValueList").subAggregation(
                AggregationBuilders.terms("productAttributeId").field("attrValueList.productAttributeId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrValueList.name"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrValueList.value"))
        ));
        return  sourceBuilder.toString();
    }
}
