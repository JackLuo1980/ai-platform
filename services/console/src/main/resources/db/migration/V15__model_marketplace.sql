CREATE TABLE marketplace_models (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    project_id              BIGINT NOT NULL,
    archive_id              BIGINT REFERENCES model_archives(id),
    name                    VARCHAR(255) NOT NULL,
    display_name            VARCHAR(255),
    description             TEXT,
    category                VARCHAR(64),
    tags_json               JSONB,
    framework               VARCHAR(64),
    task_type               VARCHAR(64),
    input_schema_json       JSONB,
    output_schema_json      JSONB,
    demo_endpoint_url       VARCHAR(512),
    icon_uri                VARCHAR(512),
    documentation_uri       VARCHAR(512),
    license_type            VARCHAR(64),
    visibility              VARCHAR(32) DEFAULT 'PRIVATE',
    download_count          BIGINT DEFAULT 0,
    rating_avg              DECIMAL,
    rating_count            INT,
    approval_status         VARCHAR(32) DEFAULT 'PENDING',
    published_at            TIMESTAMP,
    status                  VARCHAR(32) DEFAULT 'ACTIVE',
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_marketplace_models_tenant_id ON marketplace_models(tenant_id);
CREATE INDEX idx_marketplace_models_project_id ON marketplace_models(project_id);
CREATE INDEX idx_marketplace_models_archive_id ON marketplace_models(archive_id);
CREATE INDEX idx_marketplace_models_category ON marketplace_models(category);
CREATE INDEX idx_marketplace_models_task_type ON marketplace_models(task_type);
CREATE INDEX idx_marketplace_models_visibility ON marketplace_models(visibility);
CREATE INDEX idx_marketplace_models_approval_status ON marketplace_models(approval_status);
CREATE INDEX idx_marketplace_models_status ON marketplace_models(status);
