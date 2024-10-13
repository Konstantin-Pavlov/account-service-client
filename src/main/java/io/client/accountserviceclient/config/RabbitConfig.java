package io.client.accountserviceclient.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitConfig {

    @Value("${queue.request.name}")
    private String getAmountQueue;

    @Value("${queue.response.name}")
    private String addAmountQueue;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Bean
    public Queue getAmountQueue() {
        return new Queue(getAmountQueue, false);
    }

    @Bean
    public Queue addAmountQueue() {
        return new Queue(addAmountQueue, false);
    }

    @Bean
    public List<Queue> allQueues() {
        return List.of( getAmountQueue(), addAmountQueue());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(cachingConnectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setReplyTimeout(10000);  // 10 seconds timeout
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
