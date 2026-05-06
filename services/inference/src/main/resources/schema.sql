CREATE TABLE inference_models (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(50),
    framework VARCHAR(50),
    model_type VARCHAR(50),
    file_path VARCHAR(500),
    file_size BIGINT,
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    source_type VARCHAR(50),
    source_id BIGINT,
    audit_remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE TABLE online_services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    model_id BIGINT REFERENCES inference_models(id),
    model_name VARCHAR(255),
    model_version VARCHAR(50),
    model_path VARCHAR(500),
    status VARCHAR(50) DEFAULT 'CREATED',
    replicas INTEGER DEFAULT 1,
    cpu_cores DECIMAL(10,2) DEFAULT 1.00,
    memory_mb INTEGER DEFAULT 1024,
    port INTEGER DEFAULT 8080,
    release_type VARCHAR(50) DEFAULT 'canary',
    config TEXT,
    k8s_deployment_name VARCHAR(255),
    k8s_service_name VARCHAR(255),
    endpoint VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE TABLE batch_services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    model_id BIGINT REFERENCES inference_models(id),
    model_name VARCHAR(255),
    model_version VARCHAR(50),
    status VARCHAR(50) DEFAULT 'CREATED',
    input_path VARCHAR(500),
    output_path VARCHAR(500),
    result_path VARCHAR(500),
    total_records INTEGER DEFAULT 0,
    processed_records INTEGER DEFAULT 0,
    failed_records INTEGER DEFAULT 0,
    k8s_job_name VARCHAR(255),
    config TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE TABLE service_metrics (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT,
    service_name VARCHAR(255),
    qps DECIMAL(10,4),
    avg_latency_ms DECIMAL(10,4),
    p99_latency_ms DECIMAL(10,4),
    error_rate DECIMAL(10,4),
    request_count INTEGER,
    period VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE TABLE service_logs (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT,
    service_name VARCHAR(255),
    method VARCHAR(10),
    path VARCHAR(500),
    status_code INTEGER,
    latency_ms BIGINT,
    request_body TEXT,
    response_body TEXT,
    error_message TEXT,
    client_ip VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE TABLE model_drift_reports (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT,
    model_name VARCHAR(255),
    model_version VARCHAR(50),
    drift_type VARCHAR(50),
    status VARCHAR(50),
    drift_score DECIMAL(12,6),
    threshold DECIMAL(10,4),
    is_drifted BOOLEAN,
    details TEXT,
    sample_size BIGINT,
    baseline_period VARCHAR(100),
    detection_period VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE TABLE model_evaluations (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT,
    model_name VARCHAR(255),
    model_version VARCHAR(50),
    evaluation_type VARCHAR(50),
    template VARCHAR(50),
    status VARCHAR(50),
    auc DECIMAL(12,6),
    ks DECIMAL(12,6),
    gini DECIMAL(12,6),
    accuracy DECIMAL(12,6),
    precision_val DECIMAL(12,6),
    recall DECIMAL(12,6),
    f1_score DECIMAL(12,6),
    rmse DECIMAL(12,6),
    mae DECIMAL(12,6),
    r2 DECIMAL(12,6),
    mape DECIMAL(12,6),
    map_val DECIMAL(12,6),
    iou DECIMAL(12,6),
    confusion_matrix TEXT,
    roc_curve TEXT,
    details TEXT,
    sample_size INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE TABLE model_quantizations (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT,
    model_name VARCHAR(255),
    original_path VARCHAR(500),
    quantized_path VARCHAR(500),
    quantization_type VARCHAR(50),
    status VARCHAR(50),
    original_size_mb DECIMAL(10,2),
    quantized_size_mb DECIMAL(10,2),
    compression_ratio DECIMAL(10,2),
    accuracy_loss DECIMAL(10,4),
    latency_improvement DECIMAL(10,2),
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE TABLE marketplace_models (
    id BIGSERIAL PRIMARY KEY,
    marketplace_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    framework VARCHAR(50),
    description TEXT,
    artifact_path VARCHAR(500),
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE INDEX idx_inference_models_name ON inference_models(name);
CREATE INDEX idx_inference_models_status ON inference_models(status);
CREATE INDEX idx_online_services_model_id ON online_services(model_id);
CREATE INDEX idx_online_services_status ON online_services(status);
CREATE INDEX idx_batch_services_status ON batch_services(status);
CREATE INDEX idx_service_metrics_service_id ON service_metrics(service_id);
CREATE INDEX idx_service_logs_service_id ON service_logs(service_id);
CREATE INDEX idx_drift_reports_model_id ON model_drift_reports(model_id);
CREATE INDEX idx_evaluations_model_id ON model_evaluations(model_id);
CREATE INDEX idx_quantizations_model_id ON model_quantizations(model_id);
