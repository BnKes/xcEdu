package com.xuecheng.manage_cms.config;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";  //交换机


    //1.声明交换机"ex_routing_cms_postpage"
    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EXCHANEX_ROUTING_CMS_POSTPAGEGE_TOPICS_INFORM(){
        //设置交换机为通配符模式topic
        return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();//durable(true) 持久化，mq重启之后交换机还在
    }

}
