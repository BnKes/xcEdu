//public是对axios的工具类封装，定义了http请求方法，有get,post等
import http from './../../../base/api/public.js'
import querystring from 'querystring'
let sysConfig = require('@/../config/sysConfig')
let apiUrl = sysConfig.xcApiUrlPre;

//页面查询
export const page_list = (page,size,params) =>{
  //将json转成url后面的key/value对
  //page=1&size=10&siteId=5a751fab6abb5044e0d19ea1&pageAliase=%E8%AF%BE%E7%A8%88
  let queryString =  querystring.stringify(params);
  //请求服务端的页面接口查询接口
  return http.requestGet(apiUrl+'/cms/page/list/'+page+'/'+size+'?'+queryString);
}

//新增页面
export const page_add = params =>{
  return http.requestPost(apiUrl+'/cms/page/add',params)
}

//根据id查询页面
export const page_get = id =>{
  return http.requestGet(apiUrl+'/cms/page/get/'+id)
}

//修改页面提交
export const page_edit = (id,params) =>{
  return http.requestPut(apiUrl+'/cms/page/edit/'+id,params)
}

//删除提交
export const page_del = (id) =>{
  return http.requestDelete(apiUrl+'/cms/page/del/'+id)
}
