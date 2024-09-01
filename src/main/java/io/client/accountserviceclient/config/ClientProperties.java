package io.client.accountserviceclient.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "client")
public class ClientProperties {
//    private int rCount;
//    private int wCount;
//    private int simulationTime;
    private List<Integer> idList;
}