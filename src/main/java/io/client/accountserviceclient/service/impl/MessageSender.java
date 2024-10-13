package io.client.accountserviceclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MessageSender {
    private final AmqpTemplate amqpTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${queue.name}")
    private String queueName;

    @Value("${queue.request.name}")
    private String requestQueueName;

    @Value("${queue.response.name}")
    private String responseQueueName;

    public void send(String message) {
        amqpTemplate.convertAndSend(queueName, queueName + ": " + message);
    }

    public BigDecimal sendAndReceive(Integer accountId) {
        // Send account ID to the service and wait for response
        return (BigDecimal) rabbitTemplate.convertSendAndReceive(requestQueueName, accountId);
    }
}
