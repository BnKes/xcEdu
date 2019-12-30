package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {


    @Autowired
    CmsPageRepository cmsPageRepository;

    @Test
    public void testFindPage(){

        Pageable pageable = PageRequest.of(1,10);//从第一页开始，每页10条
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }
}
