package com.ihsanguldur.coreledger.infrastructure.persistence.repository;

import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
}
