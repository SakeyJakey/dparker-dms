-- Documents with application scoping
CREATE TABLE documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID NOT NULL,
    name                VARCHAR(500) NOT NULL,
    classification      VARCHAR(30) NOT NULL,
    pci_relevant        BOOLEAN DEFAULT FALSE,
    gdpr_data_categories TEXT[],
    retention_until     TIMESTAMPTZ,
    blob_url            TEXT,
    version             INTEGER DEFAULT 1,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by           UUID,
    updated_by           UUID
);

-- Index for application-scoped queries
CREATE INDEX idx_documents_application ON documents(application_id);
CREATE INDEX idx_documents_classification ON documents(classification);
CREATE INDEX idx_documents_created_at ON documents(created_at);
