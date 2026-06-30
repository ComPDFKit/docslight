package com.compdf.listener;

import com.compdf.constant.RabbitMqConstant;
import com.compdf.service.ComIDPService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author ComPDFKit-WPH 2023/11/30
 * <p>
 * 文件处理监听
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FileHandleListener {

    private final ComIDPService comidpService;

    @RabbitHandler(isDefault = true)
    @RabbitListener(queues = RabbitMqConstant.API_EXTRACT_FILE_HANDLE_QUEUE, concurrency = "3-5")
    public void apiExtractFileQueues(Message message, Channel channel, String fileId) throws IOException {
        try {
//            log.info("进来了：{}",fileId);
            comidpService.apiExtractFile(fileId);
            log.info("fileId:{} apiExtractFileQueues handle success", fileId);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    @RabbitHandler(isDefault = true)
    @RabbitListener(queues = RabbitMqConstant.API_RESOLVE_FILE_HANDLE_QUEUE)
    public void apiResolveFileQueues(Message message, Channel channel, String fileId) throws IOException {
        try {
            comidpService.apiResolveFile(fileId);
            log.info("fileId:{} apiResolveFileQueues handle success", fileId);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    @RabbitHandler(isDefault = true)
    @RabbitListener(queues = RabbitMqConstant.API_SPLIT_FILE_HANDLE_QUEUE)
    public void apiSplitFileQueues(Message message, Channel channel, String fileId) throws IOException {
        try {
            comidpService.apiSplitFile(fileId);
            log.info("fileId:{} apiSplitFileQueues handle success", fileId);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

}
