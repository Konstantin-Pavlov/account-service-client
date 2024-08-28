package io.client.accountserviceclient.repository;

import io.client.accountserviceclient.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
