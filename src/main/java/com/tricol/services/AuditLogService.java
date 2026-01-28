package com.tricol.services;

public interface AuditLogService {
    void log(String username, String action, String entity, Long entityId, String details);
}
