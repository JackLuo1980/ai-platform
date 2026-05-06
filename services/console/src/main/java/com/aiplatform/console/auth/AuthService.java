package com.aiplatform.console.auth;

import com.aiplatform.common.model.R;
import com.aiplatform.common.util.JwtUtil;
import com.aiplatform.console.role.Role;
import com.aiplatform.console.role.RoleMapper;
import com.aiplatform.console.user.User;
import com.aiplatform.console.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String TOKEN_PREFIX = "auth:token:";
    private static final String REFRESH_PREFIX = "auth:refresh:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public R<LoginResponse> login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            return R.fail(401, "Invalid username or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            return R.fail(403, "Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return R.fail(401, "Invalid username or password");
        }

        List<Role> roles = roleMapper.selectByUserId(user.getId());
        List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getTenantId(), roleNames);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getTenantId());

        redisTemplate.opsForValue().set(TOKEN_PREFIX + user.getId(), accessToken, 2, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(REFRESH_PREFIX + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        LoginResponse response = new LoginResponse(
                accessToken, refreshToken, user.getId(), user.getTenantId(), roleNames,
                jwtUtil.getAccessTokenExpiration() / 1000
        );
        return R.ok(response);
    }

    public R<Void> logout(Long userId) {
        redisTemplate.delete(TOKEN_PREFIX + userId);
        redisTemplate.delete(REFRESH_PREFIX + userId);
        return R.ok();
    }

    public R<LoginResponse> refresh(String refreshToken) {
        if (!jwtUtil.validate(refreshToken)) {
            return R.fail(401, "Invalid refresh token");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        Long tenantId = jwtUtil.getTenantId(refreshToken);

        String storedToken = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            return R.fail(401, "Refresh token expired");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            return R.fail(401, "User not found or disabled");
        }

        List<Role> roles = roleMapper.selectByUserId(userId);
        List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String newAccessToken = jwtUtil.generateAccessToken(userId, tenantId, roleNames);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, tenantId);

        redisTemplate.opsForValue().set(TOKEN_PREFIX + userId, newAccessToken, 2, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, newRefreshToken, 7, TimeUnit.DAYS);

        LoginResponse response = new LoginResponse(
                newAccessToken, newRefreshToken, userId, tenantId, roleNames,
                jwtUtil.getAccessTokenExpiration() / 1000
        );
        return R.ok(response);
    }
}
