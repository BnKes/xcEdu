//导入model01.js和vue.min.js
var {add} = require("./model01.js");
var Vue = require("./vue.min.js")

//编写MMVM中的VM(ViewModel)部分及model部分
//整体处理数据的是VM
var VM = new Vue({
    el: "#app", //表示vm接管了app区域
    data:{      //model,数据
        name: '黑马',
        num1: 0,
        num2: 0,
        result: 0,
        url: 'www.baidu.com',
        size: 11

    },
    methods:{
        change:function () {
            this.result = add(Number.parseInt(this.num1),Number.parseInt(this.num2))//调用自己VM里面的对象要用this.
        }
    }
});
