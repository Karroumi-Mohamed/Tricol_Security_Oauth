package com.tricol.services.impl;

import com.tricol.entities.AuditLog;
import com.tricol.repositories.AuditLogRepository;
import com.tricol.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String username, String action, String entity, Long entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .details(details)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }

}
