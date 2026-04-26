-- liquibase formatted sql
-- changeset author:init

CREATE TABLE chat (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE link (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(2048) UNIQUE NOT NULL,
    last_updated TIMESTAMP WITH TIME ZONE,
    last_checked_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE subscription (
    chat_id BIGINT REFERENCES chat(id) ON DELETE CASCADE,
    link_id BIGINT REFERENCES link(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, link_id)
);

CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE subscription_tag (
    chat_id BIGINT,
    link_id BIGINT,
    tag_id BIGINT REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, link_id, tag_id),
    FOREIGN KEY (chat_id, link_id) REFERENCES subscription(chat_id, link_id) ON DELETE CASCADE
);
CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    link_id BIGINT NOT NULL,
    url VARCHAR(2048) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE outbox_event_chat_ids (
    outbox_event_id BIGINT REFERENCES outbox_event(id) ON DELETE CASCADE,
    chat_id BIGINT NOT NULL
);

CREATE INDEX idx_outbox_created_at ON outbox_event(created_at);
CREATE INDEX IF NOT EXISTS idx_link_url ON link(url);
CREATE INDEX IF NOT EXISTS idx_link_last_updated ON link(last_updated);
CREATE INDEX IF NOT EXISTS idx_subscription_chat_id ON subscription(chat_id);
CREATE INDEX IF NOT EXISTS idx_subscription_link_id ON subscription(link_id);
CREATE INDEX IF NOT EXISTS idx_subscription_tag_chat_id ON subscription_tag(chat_id);

