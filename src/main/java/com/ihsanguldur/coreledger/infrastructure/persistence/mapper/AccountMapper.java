package com.ihsanguldur.coreledger.infrastructure.persistence.mapper;

import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.AccountJpaEntity;

import java.util.Currency;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountJpaEntity toJpaEntity(Account account) {
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setAccountId(account.getAccountId().value());
        entity.setCurrency(account.getBalance().getCurrency().getCurrencyCode());
        entity.setBalance(account.getBalance().getAmount());
        entity.setVersion(account.getVersion());
        return entity;
    }

    public static Account toDomain(AccountJpaEntity entity) {
        Money balance = Money.of(entity.getBalance(), Currency.getInstance(entity.getCurrency()));
        return Account.reconstitute(AccountId.of(entity.getAccountId()), balance, entity.getVersion());
    }
}
