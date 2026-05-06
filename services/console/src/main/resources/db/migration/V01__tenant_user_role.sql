CREATE TABLE tenants (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(32) DEFAULT 'ACTIVE',
    quota_json      JSONB,
    product_auth_json JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenants_status ON tenants(status);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES tenants(id),
    username        VARCHAR(128) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(32),
    status          VARCHAR(32) DEFAULT 'ACTIVE',
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE roles (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(128) NOT NULL,
    description     TEXT,
    type            VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_tenant_id ON roles(tenant_id);

CREATE TABLE user_roles (
    user_id         BIGINT NOT NULL REFERENCES users(id),
    role_id         BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    id              BIGSERIAL PRIMARY KEY,
    role_id         BIGINT NOT NULL REFERENCES roles(id),
    permission_key  VARCHAR(255) NOT NULL,
    resource_type   VARCHAR(32),
    actions_json    JSONB
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_key ON role_permissions(permission_key);
