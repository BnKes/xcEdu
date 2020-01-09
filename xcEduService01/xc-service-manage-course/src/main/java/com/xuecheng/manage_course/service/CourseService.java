package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.dao.CoursePicRepository;
import com.xuecheng.manage_course.dao.TeachplanRepository;
import com.xuecheng.manage_course.dao.TechplanMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
}
