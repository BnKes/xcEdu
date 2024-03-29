package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * 消费队列，1-4步骤和生产队列一样
 */
public class Consumer01 {
    //队列名
    private static final String QUEUE = "helloworld";

    public static void main(String[] args) {
        //1.通过连接工厂创建新的连接和mq建议连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);  //端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");     //设置虚拟机，一个mq服务可以设置多个虚拟机，每一个虚拟机就相当于一个独立的mq

        Connection connection = null;
        Channel channel = null;

        try {
            //2.建立新连接
            connection = connectionFactory.newConnection();
            //3.创建会话通道，生产者和mq服务所有的通信都在channel通道中完成
            channel = connection.createChannel();

            //4.声明队列，如果队列在mq中没有要创建
            //参数：String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
            /**
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            channel.queueDeclare(QUEUE,true,false,false,null);

            //5.实现消费队列方法
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
                /**
                 * 当接收到消息后此方法将被调用
                 * @param consumerTag  消费者标签，用来标识消费者的，在监听队列时设置channel.basicConsume
                 * @param envelope 信封，通过envelope
                 * @param properties 消息属性
                 * @param body 消息内容
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //消息id，mq在channel中用来标识消息的id，可用于确认消息已接收
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String message= new String(body,"utf-8");
                    System.out.println("receive message:"+message);
                }
            };

            //监听队列
            channel.basicConsume(QUEUE,true,defaultConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}












