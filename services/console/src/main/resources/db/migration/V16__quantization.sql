CREATE TABLE model_quantizations (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    project_id              BIGINT NOT NULL,
    source_archive_id       BIGINT NOT NULL REFERENCES model_archives(id),
    output_archive_id       BIGINT REFERENCES model_archives(id),
    name                    VARCHAR(255) NOT NULL,
    quant_method            VARCHAR(32),
    precision_bits          INT,
    calibration_dataset_id  BIGINT,
    calibration_config_json JSONB,
    original_size_bytes     BIGINT,
    quantized_size_bytes    BIGINT,
    compression_ratio       DECIMAL,
    accuracy_loss_pct       DECIMAL,
    latency_improvement_pct DECIMAL,
    metrics_before_json     JSONB,
    metrics_after_json      JSONB,
    hardware_target         VARCHAR(128),
    status                  VARCHAR(32),
    started_at              TIMESTAMP,
    finished_at             TIMESTAMP,
    error_message           TEXT,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_model_quantizations_tenant_id ON model_quantizations(tenant_id);
CREATE INDEX idx_model_quantizations_project_id ON model_quantizations(project_id);
CREATE INDEX idx_model_quantizations_source_archive_id ON model_quantizations(source_archive_id);
CREATE INDEX idx_model_quantizations_output_archive_id ON model_quantizations(output_archive_id);
CREATE INDEX idx_model_quantizations_quant_method ON model_quantizations(quant_method);
CREATE INDEX idx_model_quantizations_status ON model_quantizations(status);
