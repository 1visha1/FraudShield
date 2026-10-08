package com.fraudshield.audit.repository;

import com.fraudshield.audit.model.AuditEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEventRepository extends ElasticsearchRepository<AuditEvent, String> {
}