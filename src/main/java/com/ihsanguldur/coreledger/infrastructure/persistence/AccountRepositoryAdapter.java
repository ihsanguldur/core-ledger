package com.ihsanguldur.coreledger.infrastructure.persistence;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.event.DomainEvent;
import com.ihsanguldur.coreledger.domain.event.MoneyCredited;
import com.ihsanguldur.coreledger.domain.event.MoneyDebited;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.EntryId;
import com.ihsanguldur.coreledger.infrastructure.persistence.mapper.AccountMapper;
import com.ihsanguldur.coreledger.infrastructure.persistence.mapper.LedgerEntryMapper;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.AccountJpaRepository;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return accountJpaRepository.findById(accountId.value())
                .map(AccountMapper::toDomain);
    }

    @Override
    @Transactional
    public void save(Account account) {
        accountJpaRepository.save(AccountMapper.toJpaEntity(account));

        for (DomainEvent event : account.getEvents()) {
            LedgerEntry entry = toLedgerEntry(event);
            ledgerEntryJpaRepository.save(LedgerEntryMapper.toJpaEntity(entry));
        }
    }

    private LedgerEntry toLedgerEntry(DomainEvent event) {
        return switch (event) {
            case MoneyDebited e -> new LedgerEntry(EntryId.generate(), e.accountId(),
                    LedgerEntry.Direction.DEBIT, e.amount(), e.transactionId(), e.occurredAt());
            case MoneyCredited e -> new LedgerEntry(EntryId.generate(), e.accountId(),
                    LedgerEntry.Direction.CREDIT, e.amount(), e.transactionId(), e.occurredAt());
            default -> throw new IllegalArgumentException("Unknown domain event: " + event);
        };
    }
}
