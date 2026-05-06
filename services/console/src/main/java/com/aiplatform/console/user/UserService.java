package com.aiplatform.console.user;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public PageResult<User> list(int page, int size, Long tenantId, String username) {
        Page<User> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(User::getTenantId, tenantId);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPasswordHash(null));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public User create(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        user.setPasswordHash(null);
        return user;
    }

    public User update(Long id, User user) {
        user.setId(id);
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        } else {
            user.setPasswordHash(null);
        }
        userMapper.updateById(user);
        return user;
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}
