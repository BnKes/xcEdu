package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSONObject;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileSystemService {

    @Autowired
    FileSystemRepository fileSystemRepository;

    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;

    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;

    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;

    @Value("${xuecheng.fastdfs.charset}")
    String charset;

    //上传文件
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata){
        if (multipartFile==null){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        //1.上传文件到fastDFS,返回fileId
        String fileId = this.fdfs_upload(multipartFile);
        if (StringUtils.isEmpty(fileId)){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        //2.将filId和其他信息存入mongodb
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(fileId);
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setMetadata(JSONObject.parseObject(metadata, Map.class));
        fileSystem.setFiletag(filetag);
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        fileSystem.setFileType(multipartFile.getContentType());
        try {
            fileSystemRepository.save(fileSystem);
        }catch (Exception ex){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        return new UploadFileResult(CommonCode.SUCCESS,fileSystem);
    }

    //1.上传文件到fastDFS,返回fileId
    private String fdfs_upload(MultipartFile multipartFile){
        //1.1初始化fastDFS环境,要么读取.properties文件，要么一个一个设置
        try {
            ClientGlobal.initByTrackers(tracker_servers);//初始化tracker服务地址（多个tracker中间以半角逗号分隔）
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_charset(charset);

            //1.2定义TrackerClient，用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //1.3连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //1.4获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //1.5创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //1.6上传文件
            byte[] bytes = multipartFile.getBytes();
            String originalFilename = multipartFile.getOriginalFilename();//得到文件名称
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);//扩展名，png等
            String fileId = storageClient1.upload_file1(bytes, ext, null);//存储成功，返回fileId
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
