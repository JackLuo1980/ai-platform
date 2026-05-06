package com.aiplatform.console.team;

import com.aiplatform.common.model.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public AnnotationTeam getById(Long id) {
        return teamMapper.selectById(id);
    }

    public PageResult<AnnotationTeam> list(int page, int size, Long tenantId, Long projectId) {
        Page<AnnotationTeam> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<AnnotationTeam> wrapper = new LambdaQueryWrapper<>();
        if (tenantId != null) {
            wrapper.eq(AnnotationTeam::getTenantId, tenantId);
        }
        if (projectId != null) {
            wrapper.eq(AnnotationTeam::getProjectId, projectId);
        }
        wrapper.orderByDesc(AnnotationTeam::getCreatedAt);
        Page<AnnotationTeam> result = teamMapper.selectPage(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public AnnotationTeam create(AnnotationTeam team) {
        team.setStatus("ACTIVE");
        team.setCreatedAt(LocalDateTime.now());
        teamMapper.insert(team);
        return team;
    }

    public AnnotationTeam update(Long id, AnnotationTeam team) {
        team.setId(id);
        team.setUpdatedAt(LocalDateTime.now());
        teamMapper.updateById(team);
        return team;
    }

    public void delete(Long id) {
        teamMapper.deleteById(id);
        jdbcTemplate.update("DELETE FROM annotation_team_members WHERE team_id = ?", id);
    }

    public void addMember(Long teamId, Long userId, String role) {
        jdbcTemplate.update(
                "INSERT INTO annotation_team_members (team_id, user_id, role, joined_at) VALUES (?, ?, ?, ?)",
                teamId, userId, role, LocalDateTime.now());
    }

    public void removeMember(Long teamId, Long userId) {
        jdbcTemplate.update("DELETE FROM annotation_team_members WHERE team_id = ? AND user_id = ?", teamId, userId);
    }

    public List<AnnotationTeamMember> getMembers(Long teamId) {
        return jdbcTemplate.query(
                "SELECT * FROM annotation_team_members WHERE team_id = ?",
                (rs, rowNum) -> {
                    AnnotationTeamMember member = new AnnotationTeamMember();
                    member.setId(rs.getLong("id"));
                    member.setTeamId(rs.getLong("team_id"));
                    member.setUserId(rs.getLong("user_id"));
                    member.setRole(rs.getString("role"));
                    member.setJoinedAt(rs.getTimestamp("joined_at").toLocalDateTime());
                    return member;
                },
                teamId);
    }
}
