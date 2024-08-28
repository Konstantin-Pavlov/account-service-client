package io.client.accountserviceclient.service;

public interface AccountService {
    public long countAccounts();

    void generateAccounts(int accountsToGenerate);
}
