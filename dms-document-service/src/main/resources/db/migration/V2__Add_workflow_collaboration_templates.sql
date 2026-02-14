-- Document workflows (state machine)
CREATE TABLE document_workflows (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id     UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    status          VARCHAR(30) NOT NULL,
    previous_status VARCHAR(30),
    assigned_to     UUID,
    assigned_by     UUID,
    comments        TEXT,
    due_date        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Document favorites
CREATE TABLE document_favorites (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    document_id     UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, document_id)
);

-- Document comments
CREATE TABLE document_comments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id     UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL,
    username        VARCHAR(255),
    content         TEXT NOT NULL,
    parent_id       UUID REFERENCES document_comments(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Document shares
CREATE TABLE document_shares (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id         UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    shared_with_user_id UUID,
    shared_by_user_id   UUID NOT NULL,
    permission          VARCHAR(20) NOT NULL DEFAULT 'VIEW',
    expires_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Document templates
CREATE TABLE document_templates (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                    VARCHAR(255) NOT NULL,
    description             TEXT,
    default_classification  VARCHAR(30) NOT NULL,
    content_template        TEXT,
    metadata_schema         TEXT,
    application_id          UUID,
    created_by              UUID,
    active                  BOOLEAN DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Add tags column to documents
ALTER TABLE documents ADD COLUMN IF NOT EXISTS tags TEXT[];

-- Indexes
CREATE INDEX idx_workflow_document_id ON document_workflows(document_id);
CREATE INDEX idx_workflow_status ON document_workflows(status);
CREATE INDEX idx_favorites_user_id ON document_favorites(user_id);
CREATE INDEX idx_comments_document_id ON document_comments(document_id);
CREATE INDEX idx_shares_document_id ON document_shares(document_id);
CREATE INDEX idx_templates_application_id ON document_templates(application_id);

-- Full-text search index on document name
CREATE INDEX idx_documents_name_search ON documents USING gin(to_tsvector('english', name));
