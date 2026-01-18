-- Application registration for multi-tenant isolation
CREATE TABLE registered_applications (
    id                      UUID PRIMARY KEY,
    entra_app_id            VARCHAR(255) UNIQUE NOT NULL,
    application_name        VARCHAR(255) UNIQUE NOT NULL,
    storage_container_name  VARCHAR(255) NOT NULL,
    encryption_key_name     VARCHAR(255) NOT NULL,
    status                  VARCHAR(30) NOT NULL,
    configuration           JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Documents with application scoping
CREATE TABLE documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID NOT NULL REFERENCES registered_applications(id),
    name                VARCHAR(500) NOT NULL,
    classification      VARCHAR(30) NOT NULL,
    pci_relevant        BOOLEAN DEFAULT FALSE,
    gdpr_data_categories TEXT[],
    retention_until     TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by           UUID,
    updated_by           UUID,
    
    CONSTRAINT fk_application FOREIGN KEY (application_id) 
        REFERENCES registered_applications(id)
);

-- Index for application-scoped queries
CREATE INDEX idx_documents_application ON documents(application_id);
CREATE INDEX idx_documents_classification ON documents(classification);
CREATE INDEX idx_documents_created_at ON documents(created_at);

-- Audit logs table with partitioning
CREATE TABLE audit_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id            VARCHAR(50) NOT NULL,
    event_type          VARCHAR(50) NOT NULL,
    event_category      VARCHAR(30) NOT NULL,
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Actor Information
    user_id             UUID,
    username            VARCHAR(255),
    user_roles          JSONB,
    application_id      UUID,
    application_name    VARCHAR(255),
    
    -- Request Context  
    ip_address          INET,
    user_agent          TEXT,
    request_id          UUID,
    correlation_id      UUID,
    
    -- Resource Information
    resource_type       VARCHAR(50),
    resource_id         UUID,
    resource_name       VARCHAR(500),
    
    -- Event Details
    action              VARCHAR(50) NOT NULL,
    result              VARCHAR(20) NOT NULL,
    details             JSONB,
    previous_state      JSONB,
    new_state           JSONB,
    
    -- Compliance Markers
    pci_relevant        BOOLEAN DEFAULT FALSE,
    gdpr_relevant       BOOLEAN DEFAULT FALSE,
    contains_pii        BOOLEAN DEFAULT FALSE,
    
    -- Integrity
    checksum            VARCHAR(64) NOT NULL
) PARTITION BY RANGE (timestamp);

-- Create initial partition for current month
CREATE TABLE audit_logs_2026_01 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

-- Create immutable audit log policy
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
CREATE POLICY audit_no_delete ON audit_logs FOR DELETE USING (FALSE);
CREATE POLICY audit_no_update ON audit_logs FOR UPDATE USING (FALSE);

-- Indexes for audit logs
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_application_id ON audit_logs(application_id);
CREATE INDEX idx_audit_logs_correlation_id ON audit_logs(correlation_id);

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
    role_id             UUID NOT NULL REFERENCES roles(id),
    permission_id       UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

-- User-Role junction table
CREATE TABLE user_roles (
    user_id             UUID NOT NULL REFERENCES users(id),
    role_id             UUID NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Document permissions junction table
CREATE TABLE document_permissions (
    document_id         UUID NOT NULL REFERENCES documents(id),
    user_id             UUID REFERENCES users(id),
    role_id             UUID REFERENCES roles(id),
    permission          VARCHAR(50) NOT NULL,
    granted_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (document_id, COALESCE(user_id, '00000000-0000-0000-0000-000000000000'::UUID), 
                 COALESCE(role_id, '00000000-0000-0000-0000-000000000000'::UUID), permission)
);
