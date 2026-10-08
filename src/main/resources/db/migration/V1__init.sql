CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    tier VARCHAR(16) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    base_url VARCHAR(512) NOT NULL,
    endpoint_type VARCHAR(32) NOT NULL,
    endpoint_path VARCHAR(256) NOT NULL,
    http_method VARCHAR(16) NOT NULL DEFAULT 'POST',
    request_body_template TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE proxy (
    id BIGSERIAL PRIMARY KEY,
    host VARCHAR(256) NOT NULL,
    port INT NOT NULL,
    username VARCHAR(128),
    password VARCHAR(128),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE spam_campaigns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    service_id BIGINT NOT NULL REFERENCES services(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    target_email VARCHAR(256) NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_requests INT NOT NULL,
    success_count INT NOT NULL DEFAULT 0,
    fail_count INT NOT NULL DEFAULT 0,
    rate_per_second INT NOT NULL,
    concurrency INT NOT NULL,
    started_at TIMESTAMPTZ,
    stopped_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE spam_log (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES spam_campaigns(id),
    service_id BIGINT NOT NULL REFERENCES services(id),
    user_id BIGINT,
    status_code INT NOT NULL,
    response_time_ms BIGINT,
    error_message VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE banned_targets (
    id BIGSERIAL PRIMARY KEY,
    target VARCHAR(256) NOT NULL UNIQUE,
    reason VARCHAR(512) NOT NULL,
    banned_by_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_spam_log_campaign ON spam_log(campaign_id, created_at DESC);
CREATE INDEX idx_spam_campaigns_user ON spam_campaigns(user_id, created_at DESC);
