package com.aiplatform.fastlabel.team;

import com.aiplatform.common.model.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fastlabel/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public R<List<Map<String, Object>>> listTeams() {
        return R.ok(teamService.listTeams());
    }

    @GetMapping("/{id}/members")
    public R<List<Map<String, Object>>> listMembers(@PathVariable Long id) {
        return R.ok(teamService.listTeamMembers(id));
    }
}
