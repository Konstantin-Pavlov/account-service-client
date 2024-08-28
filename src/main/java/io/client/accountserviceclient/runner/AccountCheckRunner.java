package io.client.accountserviceclient.runner;

import io.client.accountserviceclient.service.AccountService;
import io.client.accountserviceclient.util.ConsoleColors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCheckRunner implements ApplicationRunner {
    private final AccountService accountService;
    @Value("${app.desired_account_number}")
    private Integer desiredAccountNumber;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(
                ConsoleColors.BLUE_BOLD +
                        "number of generated accounts is set to {} in application.yaml" +
                        ConsoleColors.RESET,
                desiredAccountNumber
        );
        long accountCount = accountService.countAccounts();
        if (accountCount < desiredAccountNumber) {
            int accountsToGenerate = desiredAccountNumber - (int) accountCount;
            accountService.generateAccounts(accountsToGenerate);
            log.info(
                    ConsoleColors.ANSI_PURPLE_BACKGROUND +
                            "Generated {} accounts to meet the minimum requirement of {} accounts." +
                            ConsoleColors.RESET,
                    accountsToGenerate,
                    desiredAccountNumber
            );
        } else {
            log.info(
                    ConsoleColors.ANSI_CYAN_BACKGROUND +
                            "There are already {} (>= {}) accounts in the database." +
                            ConsoleColors.RESET,
                    accountCount,
                    desiredAccountNumber
            );
        }

    }
}
