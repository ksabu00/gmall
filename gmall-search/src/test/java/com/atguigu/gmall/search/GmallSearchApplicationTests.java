package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import io.searchbox.client.JestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    private JestClient jestClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Test
    public void importData(){
        Long pageNum = 1L;
        Long pageSize = 100L;

        do{
            // 分页查询已上架商品，即spu中publish_status=1的商品=
            QueryCondition condition = new QueryCondition();
            condition.setPage(1L);
            condition.setLimit(10L);
            Resp<PageVo> pageVoResp = this.gmallPmsClient.list(condition);
            PageVo pageVo = pageVoResp.getData();
            if (pageVo == null){
                pageSize = 0L;
            }
        }while (pageSize == 100);
    }
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
