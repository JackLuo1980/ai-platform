CREATE TABLE IF NOT EXISTS prediction_records (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    model_id BIGINT,
    input_features TEXT,
    prediction_result TEXT,
    confidence DECIMAL(10,6),
    latency_ms BIGINT,
    status VARCHAR(32) DEFAULT 'SUCCESS',
    error_message TEXT,
    backflowed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_prediction_service_id ON prediction_records(service_id);
CREATE INDEX IF NOT EXISTS idx_prediction_model_id ON prediction_records(model_id);
CREATE INDEX IF NOT EXISTS idx_prediction_backflowed ON prediction_records(backflowed);
CREATE INDEX IF NOT EXISTS idx_prediction_created_at ON prediction_records(created_at);

CREATE TABLE IF NOT EXISTS data_backflow_tasks (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    model_id BIGINT,
    source_type VARCHAR(32) DEFAULT 'PREDICTION_LOG',
    record_count INTEGER DEFAULT 0,
    status VARCHAR(32) DEFAULT 'PENDING',
    target_dataset_id BIGINT,
    target_dataset_name VARCHAR(255),
    storage_path TEXT,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);
