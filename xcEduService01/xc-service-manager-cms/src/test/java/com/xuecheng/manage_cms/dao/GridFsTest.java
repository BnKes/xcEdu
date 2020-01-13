package com.xuecheng.manage_cms.dao;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    //存储文件
    @Test
    public void testGridFs() throws FileNotFoundException {
        //要存储的文件
        File file = new File("D:\\学习视频\\xcEdu\\xcEduService01\\test-freemarker\\src\\main\\resources\\templates\\course.ftl");
        //定义输入流
        FileInputStream inputStream = new FileInputStream(file);
        //向GridFs存储文件,返回文件id
        ObjectId objectId = gridFsTemplate.store(inputStream, "课程详情模板文件", "");
        //得到文件id
        String fileId = objectId.toString();
        System.out.println(fileId);

        //关闭流
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //读取.下载文件
    @Test
    public void queryFile() throws IOException {
        String fileId = "5e1b244db604e821c8e7be2b";
        //根据id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));

        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());

        //创建girdFsResource,用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);

        //获取流中的数据
        String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
        System.out.println(s);

        //关闭流
        gridFSDownloadStream.close();
    }

    //删除文件
    @Test
    public void testDelFile(){
        //根据文件id删除fs.files和fs.chunks中的记录
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is("5e0c4a96d0c72c4258447fe8")));
    }

}
