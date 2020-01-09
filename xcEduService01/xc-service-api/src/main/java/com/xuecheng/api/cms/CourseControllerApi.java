package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "课程管理接口",description = "课程管理接口")
public interface CourseControllerApi {
    @ApiOperation("查询课程计划")
    public TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("新增课程")
    public ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("添加课程图片信息")
    public ResponseResult addCoursePic(String courseId, String pic);

    @ApiOperation("获取课程基础信息")
    public CoursePic findCoursePic(String courseId);

    @ApiOperation("删除课程图片")
    public ResponseResult deleteCoursePic(String courseId);

}
