CREATE TABLE clusters (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    api_server_url  VARCHAR(512),
    token_secret    VARCHAR(255),
    status          VARCHAR(32),
    node_info_json  JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clusters_status ON clusters(status);

CREATE TABLE resource_pools (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(32),
    total_capacity  JSONB,
    used_capacity   JSONB,
    cluster_id      BIGINT REFERENCES clusters(id),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resource_pools_cluster_id ON resource_pools(cluster_id);

CREATE TABLE resource_quotas (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT,
    project_id      BIGINT,
    resource_pool_id BIGINT NOT NULL REFERENCES resource_pools(id),
    cpu_limit       DECIMAL,
    memory_limit    DECIMAL,
    gpu_limit       DECIMAL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resource_quotas_tenant_id ON resource_quotas(tenant_id);
CREATE INDEX idx_resource_quotas_project_id ON resource_quotas(project_id);
CREATE INDEX idx_resource_quotas_resource_pool_id ON resource_quotas(resource_pool_id);

CREATE TABLE images (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(32),
    runtime         VARCHAR(64),
    base_image      VARCHAR(512),
    description     TEXT,
    version         VARCHAR(64),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_images_name ON images(name);
CREATE INDEX idx_images_type ON images(type);

CREATE TABLE environments (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    image_id        BIGINT NOT NULL REFERENCES images(id),
    packages_json   JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_environments_image_id ON environments(image_id);
