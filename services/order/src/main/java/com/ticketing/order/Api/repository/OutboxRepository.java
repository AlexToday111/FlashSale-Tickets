package com.ticketing.order.Api.repository;

import com.ticketing.order.Api.model.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    long countByStatus(OutboxEntity.Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            select o
            from OutboxEntity o
            where o.status = :status
            order by o.createdAt
            """)
    List<OutboxEntity> findTopBatchForUpdate(@Param("status") OutboxEntity.Status status,
                                             org.springframework.data.domain.Pageable pageable);
}

