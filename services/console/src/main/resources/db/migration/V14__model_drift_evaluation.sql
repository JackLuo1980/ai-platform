CREATE TABLE model_drift_reports (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    project_id              BIGINT NOT NULL,
    model_id                BIGINT NOT NULL REFERENCES inference_models(id),
    drift_type              VARCHAR(32),
    baseline_dataset_id     BIGINT,
    target_dataset_id       BIGINT,
    feature_drift_json      JSONB,
    label_drift_json        JSONB,
    concept_drift_json      JSONB,
    overall_score           DECIMAL,
    threshold               DECIMAL,
    is_drift_detected       BOOLEAN DEFAULT FALSE,
    status                  VARCHAR(32),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_model_drift_reports_tenant_id ON model_drift_reports(tenant_id);
CREATE INDEX idx_model_drift_reports_project_id ON model_drift_reports(project_id);
CREATE INDEX idx_model_drift_reports_model_id ON model_drift_reports(model_id);
CREATE INDEX idx_model_drift_reports_created_at ON model_drift_reports(created_at);

CREATE TABLE model_evaluations (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    project_id              BIGINT NOT NULL,
    model_id                BIGINT NOT NULL REFERENCES inference_models(id),
    dataset_id              BIGINT,
    evaluation_type         VARCHAR(32),
    metrics_json            JSONB,
    confusion_matrix_json   JSONB,
    roc_curve_json          JSONB,
    feature_importance_json JSONB,
    sample_predictions_json JSONB,
    summary_json            JSONB,
    status                  VARCHAR(32),
    started_at              TIMESTAMP,
    finished_at             TIMESTAMP,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_model_evaluations_tenant_id ON model_evaluations(tenant_id);
CREATE INDEX idx_model_evaluations_project_id ON model_evaluations(project_id);
CREATE INDEX idx_model_evaluations_model_id ON model_evaluations(model_id);
CREATE INDEX idx_model_evaluations_status ON model_evaluations(status);
