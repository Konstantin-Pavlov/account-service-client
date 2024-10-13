package io.client.accountserviceclient.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.UpdateAccountBalanceRequest;
import exception.AccountNotFoundException;
import io.client.accountserviceclient.entity.Account;
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
    private final ObjectMapper objectMapper;

    @Value("${queue.name}")
    private String queueName;

    @Value("${queue.request.name}")
    private String getAmountRequestQueueName;

    @Value("${queue.response.name}")
    private String addAmountResponseQueueName;

    public void send(String message) {
        amqpTemplate.convertAndSend(queueName, queueName + ": " + message);
    }

    public BigDecimal getAmount(Integer accountId) {
        // Send account ID to the service and wait for response
        return (BigDecimal) rabbitTemplate.convertSendAndReceive(getAmountRequestQueueName, accountId);
    }

    public UpdateAccountBalanceRequest addAmount(Integer accountId, BigDecimal amount) throws AccountNotFoundException, JsonProcessingException {

        // Create the request object
        UpdateAccountBalanceRequest request = new UpdateAccountBalanceRequest(accountId, amount);
        // Serialize the request object to JSON
        String requestJson = objectMapper.writeValueAsString(request);
        // Send the JSON string and wait for response
        String responseJson = (String) rabbitTemplate.convertSendAndReceive(addAmountResponseQueueName, requestJson);

        if (responseJson == null) {
            throw new IllegalStateException("responseJson is null");
        }

        if (responseJson.equalsIgnoreCase("Account not found")) {
            throw new AccountNotFoundException(String.format("account with id %s not found", accountId));
        }

        // Deserialize the response JSON to AccountDTO
        return objectMapper.readValue(responseJson, UpdateAccountBalanceRequest.class);


    }
}
