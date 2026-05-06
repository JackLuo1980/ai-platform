CREATE TABLE sc_variables (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    dataset_id      BIGINT,
    name            VARCHAR(255) NOT NULL,
    dtype           VARCHAR(32),
    iv_value        DECIMAL,
    woe_json        JSONB,
    missing_rate    DECIMAL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sc_variables_tenant_id ON sc_variables(tenant_id);
CREATE INDEX idx_sc_variables_project_id ON sc_variables(project_id);
CREATE INDEX idx_sc_variables_dataset_id ON sc_variables(dataset_id);

CREATE TABLE sc_binning_results (
    id              BIGSERIAL PRIMARY KEY,
    variable_id     BIGINT NOT NULL REFERENCES sc_variables(id),
    method          VARCHAR(32),
    bins_json       JSONB,
    woe_json        JSONB,
    iv_value        DECIMAL
);

CREATE INDEX idx_sc_binning_results_variable_id ON sc_binning_results(variable_id);

CREATE TABLE sc_models (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    project_id              BIGINT NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    dataset_id              BIGINT,
    selected_variables_json JSONB,
    binning_config_json     JSONB,
    coefficients_json       JSONB,
    intercept               DECIMAL,
    ks_value                DECIMAL,
    auc_value               DECIMAL,
    gini_value              DECIMAL,
    psi_value               DECIMAL,
    report_uri              VARCHAR(512),
    status                  VARCHAR(32),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sc_models_tenant_id ON sc_models(tenant_id);
CREATE INDEX idx_sc_models_project_id ON sc_models(project_id);
CREATE INDEX idx_sc_models_status ON sc_models(status);

CREATE TABLE sc_scoring_rules (
    id                  BIGSERIAL PRIMARY KEY,
    model_id            BIGINT NOT NULL REFERENCES sc_models(id),
    variable_rules_json JSONB,
    base_score          DECIMAL,
    pdo                 DECIMAL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sc_scoring_rules_model_id ON sc_scoring_rules(model_id);

CREATE TABLE sc_scoring_results (
    id              BIGSERIAL PRIMARY KEY,
    model_id        BIGINT NOT NULL REFERENCES sc_models(id),
    score           DECIMAL,
    input_json      JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sc_scoring_results_model_id ON sc_scoring_results(model_id);
