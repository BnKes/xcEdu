package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TechplanMapper techplanMapper;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course‐publish.siteId}")
    private String publish_siteId;
    @Value("${course‐publish.templateId}")
    private String publish_templateId;
    @Value("${course‐publish.previewUrl}")
    private String previewUrl;

    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode = techplanMapper.slelectList(courseId);
        return teachplanNode;
    }

    @Transactional  //mysql数据库的增删改需要添加事务
    public ResponseResult addTeachplan(Teachplan teachplan) {
    //1.校验课程id和课程计划名称
        if(teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) || StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //取课程id
        String courseid = teachplan.getCourseid();
        //取父节点
        String parentid = teachplan.getParentid();

        //2.如果父节点不存在，则其父节点为根节点
        if (StringUtils.isEmpty(parentid)){
            //2.1添加根节点
            String teachplanRoot = this.getTeachplanRoot(teachplan.getCourseid());
            teachplan.setParentid(teachplanRoot);
        }

        //3.取出父节点信息
        Optional<Teachplan> optional = teachplanRepository.findById(teachplan.getParentid());
        if (!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Teachplan teachplanParent = optional.get();
        //根据父节点级别设置子节点jibie
        String parentGrade = teachplanParent.getGrade();
        if (parentGrade.equals("1")){
            teachplan.setGrade("2");
        }else if (parentGrade.equals("2")){
            teachplan.setGrade("3");
        }
        //4.保存
        teachplanRepository.save(teachplan);

        return new ResponseResult(CommonCode.SUCCESS);

    }

    //2.1添加根节点
    public String getTeachplanRoot(String courseId){
        //校验课程号是否存在
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            return null;
        }
        CourseBase courseBase = optional.get();
        //取出该课程根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        //如果该课程没有根节点
        if (teachplanList==null || teachplanList.size()==0){
            //新增一个根节点
            Teachplan teachplanRoot = new Teachplan();
            teachplanRoot.setCourseid(courseId);
            teachplanRoot.setParentid("0");
            teachplanRoot.setPname(courseBase.getName());
            teachplanRoot.setGrade("1");//一级
            teachplanRoot.setStatus("0");//未发布
            teachplanRepository.save(teachplanRoot);
            return teachplanRoot.getId();//新增的根节点id
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }

    //添加课程信息
    @Transactional
    public ResponseResult saveCoursePic(String courseId, String pic) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            ExceptionCast.cast(CommonCode.EXIST);
        }
        CoursePic coursePic = new CoursePic();
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程信息
    public CoursePic findCoursepic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        return optional.get();
    }

    //删除课程信息
    public ResponseResult deleteCoursePic(String courseId) {
        try {
            coursePicRepository.deleteById(courseId);
            return new ResponseResult(CommonCode.SUCCESS);
        }catch (Exception ex){
          return new ResponseResult(CommonCode.FAIL);
        }
    }

    public CourseView getCoruseView(String id) {
        CourseView courseView = new CourseView();
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()){
            courseView.setCourseBase(optional.get());
        }
        Optional<CoursePic> optional1 = coursePicRepository.findById(id);
        if (optional1.isPresent()){
            courseView.setCoursePic(optional1.get());
        }
        Optional<CourseMarket> optional2 = courseMarketRepository.findById(id);
        if (optional2.isPresent()){
            courseView.setCourseMarket(optional2.get());
        }
        TeachplanNode teachplanNode = techplanMapper.slelectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    //课程预览
    public CoursePublishResult preview(String courseId) {

        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(!baseOptional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        }
        CourseBase one = baseOptional.get();

        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId+".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);
        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        //页面url
        String pageUrl = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }
}
