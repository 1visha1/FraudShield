package com.securepay.audit.repository;

import com.securepay.audit.model.AuditEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEventRepository extends ElasticsearchRepository<AuditEvent, String> {
}