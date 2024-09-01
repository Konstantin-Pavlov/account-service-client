package io.client.accountserviceclient.service;

import io.client.accountserviceclient.entity.Account;

import java.util.List;

public interface AccountService {
    public long countAccounts();

    void generateAccounts(int accountsToGenerate);

    List<Account> getAccounts();
}
