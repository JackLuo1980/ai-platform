package com.aiplatform.fastlabel.team;

import com.aiplatform.fastlabel.client.ConsoleApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final ConsoleApiClient consoleApiClient;

    public List<Map<String, Object>> listTeams() {
        return consoleApiClient.listTeams();
    }

    public List<Map<String, Object>> listTeamMembers(Long teamId) {
        return consoleApiClient.listTeamMembers(teamId);
    }
}
