package io.client.accountserviceclient.service.impl;

import io.client.accountserviceclient.repository.AccountRepository;
import io.client.accountserviceclient.service.AccountService;
import io.client.accountserviceclient.util.AccountGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    public long countAccounts() {
        return accountRepository.count();
    }

    @Override
    public void generateAccounts(int accountsToGenerate) {
        IntStream.rangeClosed(1, accountsToGenerate)
                .forEach(i -> {
                    accountRepository.save(AccountGenerator.generateAccount(i));
                });
    }
}
