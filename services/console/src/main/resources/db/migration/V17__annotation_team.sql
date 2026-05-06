CREATE TABLE annotation_teams (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(32) DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE annotation_team_members (
    id              BIGSERIAL PRIMARY KEY,
    team_id         BIGINT NOT NULL REFERENCES annotation_teams(id),
    user_id         BIGINT NOT NULL,
    role            VARCHAR(32),
    joined_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_annotation_teams_tenant_id ON annotation_teams(tenant_id);
CREATE INDEX idx_annotation_teams_project_id ON annotation_teams(project_id);
CREATE INDEX idx_annotation_team_members_team_id ON annotation_team_members(team_id);
CREATE INDEX idx_annotation_team_members_user_id ON annotation_team_members(user_id);
