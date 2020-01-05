package com.xuecheng.manage_cms_client.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    public static final String QUEUE_CMS_POSTPAGE = "queue_cms_postpage";  //队列,QUEUE_CMS_POSTPAGE相当于代号，供注入使用
    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";  //交换机

    //1.绑定配置文件里的值
    //队列的名称
    @Value("${xuecheng.mq.queue}")
    public String queue_cms_postpage_name;   //queue_cms_postpage_name,队列的名称

    //routingkey
    @Value("${xuecheng.mq.routingkey}")
    public String routingkey;

    //2.声明交换机使用direct类型
    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EXCHANGE_DIRECT_INFORM(){
        //设置交换机为通配符模式topic
        return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();//durable(true) 持久化，mq重启之后交换机还在
    }

    //3.声明QUEUE_CMS_POSTPAGE队列
    @Bean(QUEUE_CMS_POSTPAGE)
    public Queue QUEUE_CMS_POSTPAGE(){
        return new Queue(queue_cms_postpage_name);
    }


    //4.QUEUE_INFORM_SMS队列绑定交换机，指定routingkey
    @Bean
    public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_CMS_POSTPAGE) Queue queue, @Qualifier(EX_ROUTING_CMS_POSTPAGE) Exchange exchange){  //@Qualifier注入
        return BindingBuilder.bind(queue).to(exchange).with(routingkey).noargs();
    }
}