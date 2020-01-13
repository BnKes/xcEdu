package com.xuecheng.framework.domain.course;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@NoArgsConstructor
public class CourseView implements Serializable {
    CourseBase courseBase;
    CoursePic coursePic;
    CourseMarket courseMarket;
    TeachplanNode teachplanNode;
}
