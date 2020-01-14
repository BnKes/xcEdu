<template>
  <div>
    <el-upload
      action="/api/filesystem/upload"
      list-type="picture-card"
      :before-upload="setuploaddata"
      :on-success="handleSuccess"
      :on-error="handleError"
      :file-list="fileList"
      :limit="picmax"
      :on-exceed="rejectupload"
      :before-remove="handleRemove"
      :data="uploadval"
      name="multipartFile">
      <i class="el-icon-plus"></i>
    </el-upload>
  </div>
</template>
<script>
  import * as sysConfig from '@/../config/sysConfig';
  import * as courseApi from '../../api/course';
  import utilApi from '../../../../common/utils';
  import * as systemApi from '../../../../base/api/system';
  export default {
    data() {
      return {
        picmax:1,//最大上传文件的数量
        courseid:'',
        dialogImageUrl: '',
        dialogVisible: false,
        fileList:[],
        uploadval:{filetag:"course",businesskey:"testbusinesskey"},//上传提交的额外的数据 ，将uploadval转成key/value提交给服务器
        // imgUrl:sysConfig.imgUrl
        imgUrl:'http://192.168.8.77/'
        // imgUrl:'http://192.168.1.149/'
      }
    },
    methods: {
      //超出文件上传个数提示信息
      rejectupload(){
        this.$message.error("最多上传"+this.picmax+"个图片");
      },
      //在上传前设置上传请求的数据
      setuploaddata(){

      },
      //查询图片
      list() {
        courseApi.findCoursePicList(this.courseid).then((res) => {
          console.log(res)
          if (res.pic) {
            let name = '图片';
            let url = this.imgUrl + res.pic;
            debugger
            let fileId = res.courseid;
            //先清空文件列表，再将图片放入文件列表
            this.fileList = []
            this.fileList.push({name: name, url: url, fileId: fileId});
          }
          console.log(this.fileList);
        });
      },
      //删除图片
      handleRemove(file, fileList) {
        console.log(file)
        return new Promise((resolve,reject)=>{
          courseApi.deleteCoursePic(this.courseid).then(res=>{
            if(res.success){
              //成功
              resolve(); //返回resolve(),页面删除图片，使其消失
              this.$message.success("删除成功");
            }else{
              this.$message.error("删除失败");
              //失败
              reject(); //返回reject( )，则页面不删除图片，不使其消失
            }
          })
        })
      },
      //上传成功的钩子方法
      handleSuccess(response, file, fileList){
        console.log(response)
        //调用课程管理的保存图片接口，将图片信息保存到课程管理数据库course_pic中
        //从response得到新的图片文件的地址
        if(response.success){
          let fileId = response.fileSystem.fileId;
          courseApi.addCoursePic(this.courseid,fileId).then(res=>{
              if(res.success){
                  this.$message.success("上传图片成功")
              }else{
                this.$message.error(res.message)
              }

          })
        }

      },
      //上传失败执行的钩子方法
      handleError(err, file, fileList){
        this.$message.error('上传失败');
        //清空文件队列
        this.fileList = []
      },
      //promise 有三种状态:
      //进行中pending
      //执行成功 resolve
      //执行失败 reject
      testPromise(i){
          return new Promise((resolve,reject)=>{
              if(i<2){
                  //成功了
                resolve('成功了');
              }else{
                  //失败了
                reject('失败了');
              }
          })
      }
    },
    mounted(){
      //课程id
      this.courseid = this.$route.params.courseid;
      //查询图片
     this.list();
    }
  }
</script>
<style>

</style>
