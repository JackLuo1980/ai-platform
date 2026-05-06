CREATE TABLE label_datasets (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    project_id          BIGINT NOT NULL,
    name                VARCHAR(255) NOT NULL,
    type                VARCHAR(32),
    source_dataset_id   BIGINT,
    item_count          INT,
    status              VARCHAR(32),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_label_datasets_tenant_id ON label_datasets(tenant_id);
CREATE INDEX idx_label_datasets_project_id ON label_datasets(project_id);

CREATE TABLE label_tasks (
    id              BIGSERIAL PRIMARY KEY,
    dataset_id      BIGINT NOT NULL REFERENCES label_datasets(id),
    name            VARCHAR(255) NOT NULL,
    assignee_id     BIGINT,
    reviewer_id     BIGINT,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_label_tasks_dataset_id ON label_tasks(dataset_id);
CREATE INDEX idx_label_tasks_assignee_id ON label_tasks(assignee_id);
CREATE INDEX idx_label_tasks_status ON label_tasks(status);

CREATE TABLE label_items (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL REFERENCES label_tasks(id),
    data_path       VARCHAR(512),
    annotation_json JSONB,
    status          VARCHAR(32),
    labeler_id      BIGINT,
    reviewer_id     BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_label_items_task_id ON label_items(task_id);
CREATE INDEX idx_label_items_status ON label_items(status);

CREATE TABLE label_exports (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL REFERENCES label_tasks(id),
    format          VARCHAR(32),
    storage_path    VARCHAR(512),
    item_count      INT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_label_exports_task_id ON label_exports(task_id);
