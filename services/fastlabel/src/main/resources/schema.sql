CREATE TABLE IF NOT EXISTS label_dataset (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    source VARCHAR(20),
    source_dataset_id BIGINT,
    item_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS label_task (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT REFERENCES label_dataset(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'CREATED',
    assigned_to VARCHAR(100),
    total_items INTEGER DEFAULT 0,
    labeled_items INTEGER DEFAULT 0,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS label_item (
    id BIGINT PRIMARY KEY,
    task_id BIGINT REFERENCES label_task(id),
    dataset_id BIGINT REFERENCES label_dataset(id),
    data_path TEXT,
    data_content TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    annotation_json TEXT,
    assigned_to VARCHAR(100),
    reviewed_by VARCHAR(100),
    review_comment TEXT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS label_export (
    id BIGINT PRIMARY KEY,
    task_id BIGINT REFERENCES label_task(id),
    dataset_id BIGINT REFERENCES label_dataset(id),
    name VARCHAR(255),
    format VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    file_path TEXT,
    item_count INTEGER DEFAULT 0,
    exported_by VARCHAR(100),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_label_item_task_id ON label_item(task_id);
CREATE INDEX idx_label_item_dataset_id ON label_item(dataset_id);
CREATE INDEX idx_label_item_status ON label_item(status);
CREATE INDEX idx_label_task_dataset_id ON label_task(dataset_id);
CREATE INDEX idx_label_task_status ON label_task(status);
CREATE INDEX idx_label_export_task_id ON label_export(task_id);
