<template>
  <!--注意：template内容必须有一个根元素，否则vue会报错，这里我们在template标签内定义一个div-->
  <div>
    <!--编写静态页面部分，即view部分-->

      <!--查询表单-->
      <el-form :model="params">
      <el-select v-model="params.siteId" placeholder="请选择站点">
        <el-option
          v-for="item in siteList"
          :key="item.siteId"
          :label="item.siteName"
          :value="item.siteId">
        </el-option>
      </el-select>
      页面别名：<el-input v-model="params.pageAliase"  style="width: 100px"></el-input>
      <el-button type="primary" size="small" @click="query">查询</el-button>
      <router-link :to="{path:'/cms/page/add',
        query:{
          page:this.params.page,
          siteId:this.params.siteId
          }}">
        <el-button  type="primary" size="small" style="float: right">新增</el-button>
      </router-link>
    </el-form>

    <el-table
      :data="list"
      stripe
      style="width: 100%">
      <el-table-column type="index" width="60"></el-table-column>
      <el-table-column prop="pageName" label="页面名称" width="100"></el-table-column>
      <el-table-column prop="pageAliase" label="别名" width="120"></el-table-column>
      <el-table-column prop="pageType" label="页面类型" width="100"></el-table-column>
      <el-table-column prop="pageWebPath" label="访问路径" width="250"></el-table-column>
      <el-table-column prop="pagePhysicalPath" label="物理路径" width="250"></el-table-column>
      <el-table-column label="操作" width="180">
        <template slot-scope="page">
          <el-button
            size="small"type="text"
            @click="edit(page.row.pageId)">编辑
          </el-button>
          <el-button
            size="small" type="text"
            @click="del(page.row.pageId)">删除
          </el-button>
          <el-button
            size="small" type="text"
            @click="preview(page.row.pageId)">页面预览
          </el-button>
        </template>

      </el-table-column>
    </el-table>
    <el-pagination
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="params.size"
      :current-page="params.page"
      @current-change="changePage"
      style="float: right">
    </el-pagination>
  </div>
</template>
<script>
  /*编写静态页面，model及vm部分*/
  //导入cms.js的所有方法
  import * as cmsApi from '../api/cms'
  export default {
    data() {
      return {
        list: [  //识别的id为"list"
        ],
        total: 0,
        params: {
          page: 1,
          size: 10,
          siteId: '',
          pageAliase: ''
        },
        siteList: [],//下拉列表
      }
    },
    methods: {
      query: function () {
        //调用后端的接口,res为结果
        cmsApi.page_list(this.params.page, this.params.size, this.params).then((res) => {
          //将res结果数据赋值给model
          this.list = res.queryResult.list;
          this.total = res.queryResult.total;
        })
      },
      changePage: function (page) {//形参就是当前页码
        //调用query方法
        this.params.page = page;
        this.query();
      },
      edit: function (pageId) {
        this.$router.push({
          path: '/cms/page/edit/' + pageId,
          query: {
            page: this.params.page,
            siteId: this.params.siteId
          }
        })
      },
      del:function (pageId) {
        this.$confirm('您确认删除吗?', '提示', { }).then(() => {
          //调用服务端接口
          cmsApi.page_del(pageId).then(res=>{

            if(res.success){
              this.$message.success("删除成功")
              //刷新页面
              this.query()
            }else{
              this.$message.error("删除失败")
            }
          })
        })
      },
      preview:function (pageId) {
        //打开浏览器窗口
        window.open("http://localhost:31001/cms/preview/"+pageId);
      }
    },
      created() {
        //取出路由中的参数，赋值给数据对象
        this.params.page = Number.parseInt(this.$route.query.page || 1)   //若无page参数，设为1
        this.params.siteId = this.$route.query.siteId || ''               //若无siteId参数，设为不存在
      },
      mounted() {
        this.query()
        //初始化下拉列表
        this.siteList = [
          {
            siteId: '5a751fab6abb5044e0d19ea1',
            siteName: '门户主站'
          },
          {
            siteId: '102',
            siteName: '测试站'
          },
          {
            siteId: '',
            siteName: '所有'
          }
        ]
      }
  }
</script>
<style>
  /*编写页面样式，不是必须*/
</style>
