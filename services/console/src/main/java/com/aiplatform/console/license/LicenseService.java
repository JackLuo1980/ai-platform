package com.aiplatform.console.license;

import com.aiplatform.common.model.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class LicenseService {

    @Autowired
    private LicenseMapper licenseMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public R<License> getActiveLicense() {
        LambdaQueryWrapper<License> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(License::getStatus, "ACTIVE")
                .orderByDesc(License::getCreatedAt)
                .last("LIMIT 1");
        License license = licenseMapper.selectOne(wrapper);
        if (license == null) {
            return R.fail(404, "No active license found");
        }
        return R.ok(license);
    }

    public R<License> activate(String licenseKey) {
        LambdaQueryWrapper<License> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(License::getLicenseKey, licenseKey);
        License license = licenseMapper.selectOne(wrapper);

        if (license == null) {
            return R.fail(404, "Invalid license key");
        }

        if ("ACTIVE".equals(license.getStatus())) {
            return R.fail(400, "License already activated");
        }

        if (license.getExpiresAt() != null && license.getExpiresAt().isBefore(LocalDateTime.now())) {
            return R.fail(400, "License has expired");
        }

        R<License> validation = validateLimits(license);
        if (validation.getCode() != 200) {
            return validation;
        }

        license.setStatus("ACTIVE");
        license.setIssuedAt(LocalDateTime.now());
        licenseMapper.updateById(license);
        return R.ok(license);
    }

    private R<License> validateLimits(License license) {
        if (license.getMaxTenants() != null) {
            Long tenantCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tenants WHERE status = 'ACTIVE'", Long.class);
            if (tenantCount != null && tenantCount > license.getMaxTenants()) {
                return R.fail(400, "Tenant limit exceeded");
            }
        }

        if (license.getMaxUsers() != null) {
            Long userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'", Long.class);
            if (userCount != null && userCount > license.getMaxUsers()) {
                return R.fail(400, "User limit exceeded");
            }
        }

        return R.ok(license);
    }
}
