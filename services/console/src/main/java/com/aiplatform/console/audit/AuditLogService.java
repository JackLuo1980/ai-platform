package com.aiplatform.console.audit;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public PageResult<AuditLog> list(int page, int size, Long tenantId, Long userId, String action) {
        Page<AuditLog> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(AuditLog::getTenantId, tenantId);
        }
        if (userId != null) {
            wrapper.eq(AuditLog::getUserId, userId);
        }
        if (action != null && !action.isEmpty()) {
            wrapper.eq(AuditLog::getAction, action);
        }
        wrapper.orderByDesc(AuditLog::getCreatedAt);
        Page<AuditLog> result = auditLogMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public void save(AuditLog auditLog) {
        if (auditLog.getDetailJson() == null) {
            auditLog.setDetailJson("{}");
        }
        auditLogMapper.insert(auditLog);
    }
}
