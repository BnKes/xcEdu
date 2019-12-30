<template>
  <!--注意：template内容必须有一个根元素，否则vue会报错，这里我们在template标签内定义一个div-->
  <div>
    <el-form   :model="pageForm" label-width="80px" :rules="pageFormRules" ref="pageForm">
      <el-form-item label="所属站点" prop="siteId">
        <el-select v-model="pageForm.siteId" placeholder="请选择站点">
          <el-option
            v-for="item in siteList"
            :key="item.siteId"
            :label="item.siteName"
            :value="item.siteId">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="选择模版" prop="templateId">
        <el-select v-model="pageForm.templateId" placeholder="请选择">
          <el-option
            v-for="item in templateList"
            :key="item.templateId"
            :label="item.templateName"
            :value="item.templateId">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="页面名称" prop="pageName">
        <el-input v-model="pageForm.pageName" auto-complete="off" ></el-input>
      </el-form-item>

      <el-form-item label="别名" prop="pageAliase">
        <el-input v-model="pageForm.pageAliase" auto-complete="off" ></el-input>
      </el-form-item>
      <el-form-item label="访问路径" prop="pageWebPath">
        <el-input v-model="pageForm.pageWebPath" auto-complete="off" ></el-input>
      </el-form-item>

      <el-form-item label="物理路径" prop="pagePhysicalPath">
        <el-input v-model="pageForm.pagePhysicalPath" auto-complete="off" ></el-input>
      </el-form-item>

      <el-form-item label="类型">
        <el-radio-group v-model="pageForm.pageType">
          <el-radio class="radio" label="0">静态</el-radio>
          <el-radio class="radio" label="1">动态</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker type="datetime" placeholder="创建时间" v-model="pageForm.pageCreateTime"></el-date-picker>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="addSubmit" >提交</el-button>
      <el-button type="primary" @click="go_back" >返回</el-button>
      <el-button type="primary" @click="reset" >重置</el-button>
    </div>
  </div>
</template>
<script>
  /*编写静态页面，model及vm部分*/
  //导入cms.js的所有方法
  import * as cmsApi from '../api/cms'
  export default {
    data() {
      return {
        siteList: [],
        templateList: [],
        pageForm: {
          siteId: '',
          templateId: '',
          pageName: '',
          pageAliase: '',
          pageWebPath: '',
          pageParameter: '',
          pagePhysicalPath: '',
          pageType: '',
          pageCreateTime: new Date()
        },
        pageFormRules: {
          siteId:[
            {required: true, message: '请选择站点', trigger: 'blur'}
          ],
          templateId:[
            {required: true, message: '请选择模版', trigger: 'blur'}
          ],
          pageName: [
            {required: true, message: '请输入页面名称', trigger: 'blur'}
          ],
          pageWebPath: [
            {required: true, message: '请输入访问路径', trigger: 'blur'}
          ],
          pagePhysicalPath: [
            {required: true, message: '请输入物理路径', trigger: 'blur'}
          ]
        }
      }
    },
    methods: {
      addSubmit: function () {
        this.$refs['pageForm'].validate((valid)=>{  //表单校验
          if (valid) {      //表单校验成功
            //弹窗提示
            this.$confirm('你确认提交吗？', '提示', {}).then(() => {
              //调用page_add接口
              cmsApi.page_add(this.pageForm).then((res) => {
                //解析后端返回结果
                if (res.success) {
                  this.$message.success("新增成功");  //提示增加成功
                  this.$refs['pageForm'].resetFields(); //成功后清空表单
                } else if (res.message) {
                  this.$message.error(res.message)
                } else {
                  this.$message.error("提交失败")
                }
              });
            })
          }
        })
      },
      //返回
      go_back: function () {
        this.$router.push({   //取当前路由
          path: '/cms/page/list',  //设置返回路径
            query:{
              page: this.$route.query.page, //取出路由中传过来的参数
              siteId: this.$route.query.siteId
          }
        })
      },
      reset: function () {
        this.$refs['pageForm'].resetFields(); //重置表单
      }
    },
    mounted() {
      //初始化站点列表
      this.siteList = [
        {
          siteId: '5a751fab6abb5044e0d19ea1',
          siteName: '门户主站'
        },
        {
          siteId: '102',
          siteName: '测试站'
        }
      ]
      //模板列表
      this.templateList = [
        {
          templateId:'5a962b52b00ffc514038faf7',
          templateName:'首页'
        },
        {
          templateId:'5a962bf8b00ffc514038fafa',
          templateName:'轮播图'
        }
      ]
    }
  }
</script>
<style>
  /*编写页面样式，不是必须*/
</style>
