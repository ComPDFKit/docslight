package com.compdf.config;

import com.compdf.constant.RabbitMqConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableRabbit
public class RabbitMqConfig {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送回调
     */
    @PostConstruct
    public void publisherConfirm() {
        // 消息抵达服务器确认回调，correlationData：当前消息唯一关联数据（消息的唯一id）,ack：消息是否抵达服务器，cause：失败的原因
        rabbitTemplate.setConfirmCallback(
                (correlationData, ack, cause) -> log.info("发送成功，correlationData -> {}, ack -> {}, cause -> {}", correlationData, ack, cause)
        );

        // 消息到队列的确认回调（当消息未抵达到队列时才会回调），message：当前发送的消息，replyCode：回复的状态码，replyText：回复的信息，exchange：交换机，routingKey：路由键
        rabbitTemplate.setReturnCallback(
                (message, replyCode, replyText, exchange, routingKey) -> log.info("发送失败，message -> {}, replyCode -> {}, replyText -> {}, exchange -> {}, routingKey -> {}", message, replyCode, replyText, exchange, routingKey)
        );
    }

    @Bean
    public TopicExchange fileHandleExchange() {
        return new TopicExchange(RabbitMqConstant.FILE_HANDLE_EXCHANGE, true, false);
    }

    @Bean
    public Queue fileHandleQueue() {
        return new Queue(RabbitMqConstant.FILE_HANDLE_QUEUE, true, false, false);
    }

    @Bean
    public Binding fileHandleBinding() {
        return BindingBuilder.bind(fileHandleQueue()).to(fileHandleExchange()).with(RabbitMqConstant.FILE_HANDLE_ROUTING_KEY);
    }

    @Bean
    public TopicExchange apiFileHandleExchange() {
        return new TopicExchange(RabbitMqConstant.API_FILE_HANDLE_EXCHANGE, true, false);
    }

    @Bean
    public Queue apiFileHandleQueue() {
        return new Queue(RabbitMqConstant.API_FILE_HANDLE_QUEUE, true, false, false);
    }

    @Bean
    public Binding apiFileHandleBinding() {
        return BindingBuilder.bind(apiFileHandleQueue()).to(apiFileHandleExchange()).with(RabbitMqConstant.API_FILE_HANDLE_ROUTING_KEY);
    }

    @Bean
    public TopicExchange idpHandleExchange() {
        return new TopicExchange(RabbitMqConstant.IDP_HANDLE_EXCHANGE, true, false);
    }

    @Bean
    public Queue apiExtractQueue() {
        return new Queue(RabbitMqConstant.API_EXTRACT_FILE_HANDLE_QUEUE, true, false, false);
    }

    @Bean
    public Binding apiExtractBinding() {
        return BindingBuilder.bind(apiExtractQueue()).to(idpHandleExchange()).with(RabbitMqConstant.API_EXTRACT_FILE_HANDLE_ROUTING_KEY);
    }

    @Bean
    public Queue apiResolveQueue() {
        return new Queue(RabbitMqConstant.API_RESOLVE_FILE_HANDLE_QUEUE, true, false, false);
    }

    @Bean
    public Binding apiResolveBinding() {
        return BindingBuilder.bind(apiResolveQueue()).to(idpHandleExchange()).with(RabbitMqConstant.API_RESOLVE_FILE_HANDLE_ROUTING_KEY);
    }

    @Bean
    public Queue apiSplitQueue() {
        return new Queue(RabbitMqConstant.API_SPLIT_FILE_HANDLE_QUEUE, true, false, false);
    }

    @Bean
    public Binding apiSplitBinding() {
        return BindingBuilder.bind(apiSplitQueue()).to(idpHandleExchange()).with(RabbitMqConstant.API_SPLIT_FILE_HANDLE_ROUTING_KEY);
    }
}
