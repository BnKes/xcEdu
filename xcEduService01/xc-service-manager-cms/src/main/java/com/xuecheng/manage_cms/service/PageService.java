package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        if(queryPageRequest==null){
            queryPageRequest = new QueryPageRequest();
        }

        //自定义条件查询
        //1.条件值查询
        CmsPage cmsPage = new CmsPage();
        //设置某个站点id为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置某个模板id为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名作为查询条件
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        //2.定义条件匹配器，迷糊查询方式等（包含，开头）
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());//对pageAliase属性，按包含模糊查询
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        //设置分页参数,页码从1开始
        if(page <=0){
            page = 1;
        }
        page = page - 1;

        if(size<=0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page,size);

        //使用cmsPageRepository，查询
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);

        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());

        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    public CmsPageResult add(CmsPage cmsPage) {
        if(cmsPage == null){
            //抛出异常
        }
        //校验页面名称、站点Id、页面webpath的唯一性
        //根据页面名称、站点Id、页面webpath去cms_page集合，如果查到说明此页面已经存在，如果查询不到再继续添加
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cmsPage1 != null){
            //页面已存在抛出异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //调用dao新增页面
        cmsPage.setPageId(null);//防止主键重复，先置空，再由mongodb自动生成主键
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);

    }

    public CmsPage getById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    public CmsPageResult update(String id, CmsPage cmsPage) {
        CmsPage one = this.getById(id);  //调用本类的getById方法
        if (one!=null){
            //设置要修改的数据
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新数据url
            one.setDataUrl(cmsPage.getDataUrl());
            //更新类型
            one.setPageType(cmsPage.getPageType());
            //更新时间
            one.setPageCreateTime(cmsPage.getPageCreateTime());

            //提交
            cmsPageRepository.save(one);

            return new CmsPageResult(CommonCode.SUCCESS,one);
        }
        //修改失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    public ResponseResult delete(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }

        //删除失败
        return new ResponseResult(CommonCode.FAIL);
    }


    //页面发布
    public ResponseResult postPage(String pageId){
        //1.执行静态化
        String pageHtml = this.getPageHtml(pageId);
        if (StringUtils.isEmpty(pageHtml)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }

        //2.保存静态化文件到Gridfs
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);

        //3.消息队列发送消息
        this.sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //1.页面静态化
    public String getPageHtml(String pageId){
        //1.1获取页面模型数据
        Map model = this.getModelByPPageId(pageId);
        if(model == null){
            //根据页面的数据url获取不到数据
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //1.2.获取页面模板
        String templateContent = this.getTemplateByPageId(pageId);
        if (templateContent==null){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //1.3.执行静态化，将模型数据融入到页面模板
        String html = this.generateHtml(templateContent, model);
        if (StringUtils.isEmpty(html)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    //1.1获取页面模型数据
    public Map getModelByPPageId(String pageId){
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出dataUrl
        String dataUrl = cmsPage.getDataUrl(); // "dataUrl" : "http://localhost:40200/portalview/course/getpre/4028e58161bd3b380161bd3bcd2f0000"
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);  //发请求获取结果
        Map body = forEntity.getBody();
        return body;
    }

    //1.2获取页面模板
    public String getTemplateByPageId(String pageId){
        //1.查询页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage==null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //2.取页面模板
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //3.取出模板文件id，templateFileId
            String templateFileId = cmsTemplate.getTemplateFileId();//5aec5d8c0e6618376c08e47d
            //4.取出文件模板内容,templateFileId是fs.files数据库的_id,和fs.chunks的files_id
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //5.打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //6.创建GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //7.输出流
            String content = null;
            try {
                content = IOUtils.toString(gridFSDownloadStream, "UTF-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //1.3页面静态化，将模型数据融入到页面模板
    public String generateHtml(String tempalate,Map model){
        try {
            //1.生成配置类
            Configuration configuration = new Configuration(Configuration.getVersion());
            //2.模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",tempalate);
            //3.配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);

            //4.获取模板
            Template template1 = null;
            template1 = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //2.保存静态化文件到Gridfs
    public CmsPage saveHtml(String pageId,String content){
        //判断页面是否存在
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        //保存html前,先将该页面原来保存在Gridfs中的保存代码内容的文件删除掉，再保存新的
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isEmpty(htmlFileId)){
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存html文件到GridFS，同时更新CmsPage的htmlFileId
        InputStream inputStream = IOUtils.toInputStream(content);
        ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());//返回存储文件的id
        htmlFileId = objectId.toString();
        cmsPage.setHtmlFileId(htmlFileId);
        cmsPageRepository.save(cmsPage);

        return cmsPage;
    }

    //3.消息队列发送页面发布消息,JSON串
    private void sendPostPage(String pageId){
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("pageId",pageId);
        //将发送的消息内容Map转为JSON
        String msg = JSON.toJSONString(msgMap);
        //获取站点id作为routingkey
        String routingkey = cmsPage.getSiteId();
        //发布消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,routingkey,msg);
    }

    //保存页面，有则更新，没有则添加
    public CmsPageResult save(CmsPage cmsPage) {
        //判断页面是否存在
        CmsPage one = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(one!=null){
            //进行更新
            return this.update(one.getPageId(),cmsPage);
        }
        return this.add(cmsPage);

    }
}
