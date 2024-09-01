package io.client.accountserviceclient;

import io.client.accountserviceclient.config.ClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ClientProperties.class)
public class AccountServiceClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceClientApplication.class, args);
    }

}
