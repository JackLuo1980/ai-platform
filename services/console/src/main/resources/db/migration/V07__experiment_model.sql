CREATE TABLE experiments (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    project_id              BIGINT NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    workflow_run_id         BIGINT,
    mlflow_experiment_id    VARCHAR(128),
    status                  VARCHAR(32),
    params_json             JSONB,
    metrics_json            JSONB,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_experiments_tenant_id ON experiments(tenant_id);
CREATE INDEX idx_experiments_project_id ON experiments(project_id);
CREATE INDEX idx_experiments_workflow_run_id ON experiments(workflow_run_id);

CREATE TABLE model_archives (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    project_id              BIGINT NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    format                  VARCHAR(32),
    artifact_uri            VARCHAR(1024),
    runtime_image           VARCHAR(512),
    feature_schema_json     JSONB,
    evaluation_summary_json JSONB,
    approval_status         VARCHAR(32) DEFAULT 'PENDING',
    source_type             VARCHAR(32),
    source_experiment_id    BIGINT,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_model_archives_tenant_id ON model_archives(tenant_id);
CREATE INDEX idx_model_archives_project_id ON model_archives(project_id);
CREATE INDEX idx_model_archives_approval_status ON model_archives(approval_status);
CREATE INDEX idx_model_archives_source_experiment_id ON model_archives(source_experiment_id);

CREATE TABLE model_files (
    id              BIGSERIAL PRIMARY KEY,
    archive_id      BIGINT NOT NULL REFERENCES model_archives(id),
    file_path       VARCHAR(512),
    file_size       BIGINT,
    checksum        VARCHAR(128)
);

CREATE INDEX idx_model_files_archive_id ON model_files(archive_id);
