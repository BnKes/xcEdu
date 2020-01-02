package com.xuecheng.manage_cms.dao;

import com.xuecheng.manage_cms.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GetPageHtmlTest {

    @Autowired
    PageService pageService;

    @Test
    public void testGetPageHtml(){
        String html = pageService.getPageHtml("5ad92e9068db52404cad0f79");
        System.out.println(html);
    }
}
