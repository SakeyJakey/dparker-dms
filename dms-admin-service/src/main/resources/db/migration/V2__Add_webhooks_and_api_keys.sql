-- Webhooks for event subscriptions
CREATE TABLE webhooks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    url             VARCHAR(500) NOT NULL,
    secret_key      VARCHAR(255),
    event_types     VARCHAR(500) NOT NULL,
    application_id  UUID,
    active          BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- API keys for programmatic access
CREATE TABLE api_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    key_hash        VARCHAR(128) UNIQUE NOT NULL,
    key_prefix      VARCHAR(10) NOT NULL,
    application_id  UUID,
    user_id         UUID,
    scopes          VARCHAR(500) NOT NULL DEFAULT 'read',
    active          BOOLEAN DEFAULT TRUE,
    expires_at      TIMESTAMPTZ,
    last_used_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Add updated_at columns that were missing
ALTER TABLE roles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- Indexes
CREATE INDEX idx_webhooks_application_id ON webhooks(application_id);
CREATE INDEX idx_webhooks_active ON webhooks(active);
CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);
CREATE INDEX idx_api_keys_application_id ON api_keys(application_id);
