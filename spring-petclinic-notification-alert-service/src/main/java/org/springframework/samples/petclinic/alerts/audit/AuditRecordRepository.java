package org.springframework.samples.petclinic.alerts.audit;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.Repository;

/**
 * Deliberately a plain Repository: it offers no way to delete a record.
 */
public interface AuditRecordRepository extends Repository<AuditRecord, Long> {

    AuditRecord save(AuditRecord record);

    boolean existsByEventId(UUID eventId);

    Page<AuditRecord> findAll(Specification<AuditRecord> specification, Pageable pageable);

    long count();
}
