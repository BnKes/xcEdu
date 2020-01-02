<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
<br/>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#if stus??>
        集合stus大小： ${stus?size}
    <#list stus as stu>
        <tr>
            <td>${stu_index+1}</td>
            <td <#if stu.name == '小明'>style="background: red" </#if>>${stu.name}</td>
            <td>${(stu.age)!''}</td>
            <td <#if (stu.mondy>300)>style="background: red" </#if>>${stu.mondy}</td>
            <td>显示年月日: ${stu.birthday?date}</td>
            <td>显示时分秒：${stu.birthday?time}</td>
            <td>显示日期+时间：${stu.birthday?datetime}</td>
            <td>自定义格式化： ${stu.birthday?string("yyyy年MM月dd日")}</td>
        </tr>
    </#list>
    </#if>
</table>
<br/><br/>
输出stu1的学生信息：<br/>
姓名： ${stuMap['stu1'].name} || ${stu1.name} <br/>
年龄： ${stuMap['stu1'].age}  || ${stu1.age}
遍历两个学生信息：<br/>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
<#list stuMap?keys as k>
    <tr>
        <td>${k_index+1}</td>
        <td>${stuMap[k].name}</td>
        <td>${stuMap[k].age}</td>
        <td>${stuMap[k].mondy}</td>
    </tr>
</#list>
</table>
<#assign text="{'bank':'工商银行','account':'10101920201920212'}" />
<#assign data=text?eval />
开户行：${data.bank} 账号：${data.account}
</body>
</html>
























