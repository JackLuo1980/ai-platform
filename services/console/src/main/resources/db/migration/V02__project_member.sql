CREATE TABLE projects (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES tenants(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(32) DEFAULT 'ACTIVE',
    settings_json   JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projects_tenant_id ON projects(tenant_id);
CREATE INDEX idx_projects_status ON projects(status);

CREATE TABLE project_members (
    project_id      BIGINT NOT NULL REFERENCES projects(id),
    user_id         BIGINT NOT NULL REFERENCES users(id),
    role_id         BIGINT NOT NULL REFERENCES roles(id),
    joined_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id)
);

CREATE INDEX idx_project_members_user_id ON project_members(user_id);
CREATE INDEX idx_project_members_role_id ON project_members(role_id);
