package com.aiplatform.console.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogMapper auditLogMapper;

    @org.aspectj.lang.annotation.After("execution(* com.aiplatform.console..controller.*Controller.create*(..)) || " +
            "execution(* com.aiplatform.console..controller.*Controller.update*(..)) || " +
            "execution(* com.aiplatform.console..controller.*Controller.delete*(..))")
    public void auditWriteOperation(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            HttpServletRequest request = attrs.getRequest();
            String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(request.getMethod() + " " + request.getRequestURI());
            auditLog.setResourceType(className);
            auditLog.setResourceId(methodName);
            auditLog.setIpAddress(request.getRemoteAddr());

            try {
                Object[] args = joinPoint.getArgs();
                if (args.length > 0 && args[0] != null) {
                    String details = args[0].toString();
                    if (details.length() > 1000) details = details.substring(0, 1000);
                    auditLog.setDetailJson(details);
                }
            } catch (Exception ignored) {}

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.warn("Failed to write audit log: {}", e.getMessage());
        }
    }
}
