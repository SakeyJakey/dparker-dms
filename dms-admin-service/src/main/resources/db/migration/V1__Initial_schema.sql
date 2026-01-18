-- Users table for user management
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(255) UNIQUE NOT NULL,
    email               VARCHAR(255) UNIQUE NOT NULL,
    display_name        VARCHAR(255),
    enabled             BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Roles table
CREATE TABLE roles (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(100) UNIQUE NOT NULL,
    description         TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Permissions table
CREATE TABLE permissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(100) UNIQUE NOT NULL,
    description         TEXT,
    resource_type       VARCHAR(50),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Role-Permission junction table
CREATE TABLE role_permissions (
    role_id             UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id      UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- User-Role junction table
CREATE TABLE user_roles (
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id             UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Application registration for multi-tenant isolation
CREATE TABLE registered_applications (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entra_app_id            VARCHAR(255) UNIQUE NOT NULL,
    application_name        VARCHAR(255) UNIQUE NOT NULL,
    storage_container_name  VARCHAR(255) NOT NULL,
    encryption_key_name     VARCHAR(255) NOT NULL,
    status                  VARCHAR(30) NOT NULL,
    configuration           JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_role_name ON roles(name);
CREATE INDEX idx_permission_name ON permissions(name);
CREATE INDEX idx_registered_app_entra_id ON registered_applications(entra_app_id);
CREATE INDEX idx_registered_app_name ON registered_applications(application_name);
