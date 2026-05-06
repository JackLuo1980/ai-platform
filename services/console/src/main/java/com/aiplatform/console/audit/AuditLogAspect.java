package com.aiplatform.console.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return result;
            }

            HttpServletRequest request = attributes.getRequest();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            String action = null;
            String httpMethod = request.getMethod();

            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof org.springframework.web.bind.annotation.PostMapping) {
                    action = "CREATE";
                } else if (annotation instanceof org.springframework.web.bind.annotation.PutMapping) {
                    action = "UPDATE";
                } else if (annotation instanceof org.springframework.web.bind.annotation.DeleteMapping) {
                    action = "DELETE";
                }
            }

            AuditLog log = new AuditLog();
            log.setAction(action);
            log.setIpAddress(request.getRemoteAddr());
            log.setCreatedAt(LocalDateTime.now());

            String tenantIdHeader = request.getHeader("X-Tenant-Id");
            String userIdHeader = request.getHeader("X-User-Id");
            if (tenantIdHeader != null) {
                log.setTenantId(Long.valueOf(tenantIdHeader));
            }
            if (userIdHeader != null) {
                log.setUserId(Long.valueOf(userIdHeader));
            }

            String uri = request.getRequestURI();
            log.setResourceType(extractResourceType(uri));

            Map<String, Object> detail = new HashMap<>();
            detail.put("uri", uri);
            detail.put("method", httpMethod);
            if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] != null) {
                detail.put("body", joinPoint.getArgs()[0].toString());
            }
            log.setDetailJson(objectMapper.writeValueAsString(detail));

            auditLogService.save(log);
        } catch (Exception e) {
            // ignore audit logging failures
        }

        return result;
    }

    private String extractResourceType(String uri) {
        String[] parts = uri.split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "UNKNOWN";
    }
}
