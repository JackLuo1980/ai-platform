package com.aiplatform.console.role;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    public PageResult<Role> list(int page, int size, Long tenantId) {
        Page<Role> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(Role::getTenantId, tenantId);
        }
        wrapper.orderByDesc(Role::getCreatedAt);
        Page<Role> result = roleMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public Role create(Role role) {
        roleMapper.insert(role);
        return role;
    }

    public Role update(Long id, Role role) {
        role.setId(id);
        roleMapper.updateById(role);
        return role;
    }

    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    public void updatePermissions(Long roleId, List<RolePermission> permissions) {
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        permissions.forEach(p -> {
            p.setRoleId(roleId);
        });
    }
}
