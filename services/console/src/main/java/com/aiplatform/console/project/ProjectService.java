package com.aiplatform.console.project;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Project getById(Long id) {
        return projectMapper.selectById(id);
    }

    public PageResult<Project> list(int page, int size, Long tenantId, String name) {
        Page<Project> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(Project::getTenantId, tenantId);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(Project::getName, name);
        }
        wrapper.orderByDesc(Project::getCreatedAt);
        Page<Project> result = projectMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public Project create(Project project) {
        project.setStatus("ACTIVE");
        projectMapper.insert(project);
        return project;
    }

    public Project update(Long id, Project project) {
        project.setId(id);
        projectMapper.updateById(project);
        return project;
    }

    public void delete(Long id) {
        projectMapper.deleteById(id);
        jdbcTemplate.update("DELETE FROM project_members WHERE project_id = ?", id);
    }

    public void addMember(Long projectId, Long userId, Long roleId) {
        jdbcTemplate.update(
                "INSERT INTO project_members (project_id, user_id, role_id, joined_at) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING",
                projectId, userId, roleId, LocalDateTime.now());
    }

    public void removeMember(Long projectId, Long userId) {
        jdbcTemplate.update("DELETE FROM project_members WHERE project_id = ? AND user_id = ?", projectId, userId);
    }
}
