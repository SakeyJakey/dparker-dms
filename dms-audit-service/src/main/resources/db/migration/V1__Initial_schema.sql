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
