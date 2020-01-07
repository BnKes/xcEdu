package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TechplanMapper {
    //课程计划查询
    public TeachplanNode slelectList(String courseId);
}
