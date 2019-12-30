package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

//MongoRepository<T>spring定义的接口类，T指定模型类和模型的主键类,模型类中指定表
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {

    //根据页面名称、站点Id、页面webpath查询
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);

}
