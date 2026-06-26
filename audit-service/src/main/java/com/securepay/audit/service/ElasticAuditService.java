package com.securepay.audit.service;
import co.elastic.clients.elasticsearch.ElasticsearchClient;

import com.securepay.audit.model.AuditDocument;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Service
@RequiredArgsConstructor
public class ElasticAuditService {

    private final ElasticsearchClient client;

    public void index(
            AuditDocument document)
            throws IOException {

        String index =
                "securepay-audit-" +
                        LocalDate.now()
                                .format(
                                        DateTimeFormatter
                                                .ofPattern(
                                                        "yyyy.MM"));

        client.index(i -> i
                .index(index)
                .document(document));
    }
}