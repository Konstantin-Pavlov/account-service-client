package io.client.accountserviceclient.server;

import io.client.accountserviceclient.util.ConsoleColors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MockAccountServer {
    private final AtomicInteger getAmountCounter = new AtomicInteger(0);
    private final AtomicInteger addAmountCounter = new AtomicInteger(0);

    public Long getAmount(Integer id) {
        getAmountCounter.incrementAndGet();
        log.info(ConsoleColors.BLUE +
                        "Getting amount for ID: {}" +
                        ConsoleColors.RESET, id);
        // Simulate getting amount
        return 100L; // Mocked value
    }

    public void addAmount(Integer id, Long value) {
        addAmountCounter.incrementAndGet();
        log.info(ConsoleColors.GREEN_BACKGROUND +
                "Adding amount {} for ID: {}" +
                ConsoleColors.RESET, value, id);
        // Simulate adding amount
    }

    public int getGetAmountCount() {
        return getAmountCounter.get();
    }

    public int getAddAmountCount() {
        return addAmountCounter.get();
    }
}