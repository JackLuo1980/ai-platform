CREATE TABLE operators (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    name                VARCHAR(255) NOT NULL,
    type                VARCHAR(32),
    category            VARCHAR(64),
    description         TEXT,
    params_schema_json  JSONB,
    code                TEXT,
    version             INT,
    is_shared           BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_operators_tenant_id ON operators(tenant_id);
CREATE INDEX idx_operators_type ON operators(type);
CREATE INDEX idx_operators_category ON operators(category);

CREATE TABLE operator_versions (
    id                  BIGSERIAL PRIMARY KEY,
    operator_id         BIGINT NOT NULL REFERENCES operators(id),
    version             INT,
    code                TEXT,
    params_schema_json  JSONB,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_operator_versions_operator_id ON operator_versions(operator_id);

CREATE TABLE workflows (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(32),
    nodes_json      JSONB,
    edges_json      JSONB,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workflows_tenant_id ON workflows(tenant_id);
CREATE INDEX idx_workflows_project_id ON workflows(project_id);

CREATE TABLE workflow_runs (
    id              BIGSERIAL PRIMARY KEY,
    workflow_id     BIGINT NOT NULL REFERENCES workflows(id),
    status          VARCHAR(32),
    started_at      TIMESTAMP,
    finished_at     TIMESTAMP,
    log_path        VARCHAR(1024)
);

CREATE INDEX idx_workflow_runs_workflow_id ON workflow_runs(workflow_id);
CREATE INDEX idx_workflow_runs_status ON workflow_runs(status);
