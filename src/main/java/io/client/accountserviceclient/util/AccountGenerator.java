package io.client.accountserviceclient.util;

import io.client.accountserviceclient.entity.Account;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountGenerator {
    private AccountGenerator() {}
    public static Account generateAccount(int counter) {
        String formattedCounter = String.format("%5d", counter).replace(' ', '.');
        log.info(
                ConsoleColors.ANSI_GREEN + "{} account generated" + ConsoleColors.ANSI_RESET, formattedCounter
        );
        return Account.builder()
                .balance(0)
                .build();
    }
}
