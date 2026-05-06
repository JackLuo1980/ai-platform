CREATE TABLE inference_models (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    archive_id      BIGINT NOT NULL REFERENCES model_archives(id),
    name            VARCHAR(255) NOT NULL,
    version         INT,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inference_models_tenant_id ON inference_models(tenant_id);
CREATE INDEX idx_inference_models_project_id ON inference_models(project_id);
CREATE INDEX idx_inference_models_archive_id ON inference_models(archive_id);

CREATE TABLE online_services (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    model_id        BIGINT NOT NULL REFERENCES inference_models(id),
    name            VARCHAR(255) NOT NULL,
    endpoint_url    VARCHAR(512),
    release_type    VARCHAR(32),
    replicas        INT,
    cpu             DECIMAL,
    memory          DECIMAL,
    gpu             DECIMAL,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_online_services_tenant_id ON online_services(tenant_id);
CREATE INDEX idx_online_services_project_id ON online_services(project_id);
CREATE INDEX idx_online_services_model_id ON online_services(model_id);

CREATE TABLE batch_services (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    model_id        BIGINT NOT NULL REFERENCES inference_models(id),
    name            VARCHAR(255) NOT NULL,
    data_source_id  BIGINT,
    output_path     VARCHAR(1024),
    schedule_json   JSONB,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_batch_services_tenant_id ON batch_services(tenant_id);
CREATE INDEX idx_batch_services_project_id ON batch_services(project_id);
CREATE INDEX idx_batch_services_model_id ON batch_services(model_id);

CREATE TABLE service_metrics (
    id              BIGSERIAL PRIMARY KEY,
    service_id      BIGINT NOT NULL,
    qps             DECIMAL,
    avg_latency_ms  DECIMAL,
    error_rate      DECIMAL,
    request_count   BIGINT,
    recorded_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_metrics_service_id ON service_metrics(service_id);
CREATE INDEX idx_service_metrics_recorded_at ON service_metrics(recorded_at);

CREATE TABLE service_logs (
    id              BIGSERIAL PRIMARY KEY,
    service_id      BIGINT NOT NULL,
    request_json    JSONB,
    response_json   JSONB,
    latency_ms      DECIMAL,
    status_code     INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_logs_service_id ON service_logs(service_id);
CREATE INDEX idx_service_logs_created_at ON service_logs(created_at);
