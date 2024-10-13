package io.client.accountserviceclient;

import io.client.accountserviceclient.config.ClientProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@EnableConfigurationProperties(ClientProperties.class)
public class AccountServiceClientApplication {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private List<Queue> allQueues;

    @PostConstruct
    public void declareQueue() {
        for (Queue queue : allQueues) {
            rabbitAdmin.declareQueue(queue);
        }
    }

//    private final static String QUEUE_NAME = "hello";
    public static void main(String[] args) throws IOException, TimeoutException {
        SpringApplication.run(AccountServiceClientApplication.class, args);

//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
//        try (Connection connection = factory.newConnection();
//             Channel channel = connection.createChannel()) {
//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//            String message = "Hello World!";
//            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
//            System.out.println(" [x] Sent '" + message + "'");
//        }

    }

}
