CREATE TABLE IF NOT EXISTS agent_profile_memory
(
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id               UUID         NOT NULL,
    memory_key             VARCHAR(256) NOT NULL,
    memory_value           VARCHAR(64)  NOT NULL,
    fact                   VARCHAR(512) NOT NULL,
    confidence             DOUBLE PRECISION NOT NULL DEFAULT 0.7,
    status                 VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    superseded_by_memory_id UUID       NULL,
    source_session_id      UUID         NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP    NOT NULL DEFAULT NOW()
);

ALTER TABLE agent_profile_memory ADD COLUMN IF NOT EXISTS memory_key VARCHAR(256);
ALTER TABLE agent_profile_memory ADD COLUMN IF NOT EXISTS memory_value VARCHAR(64);
ALTER TABLE agent_profile_memory ADD COLUMN IF NOT EXISTS confidence DOUBLE PRECISION DEFAULT 0.7;
ALTER TABLE agent_profile_memory ADD COLUMN IF NOT EXISTS status VARCHAR(32) DEFAULT 'ACTIVE';
ALTER TABLE agent_profile_memory ADD COLUMN IF NOT EXISTS superseded_by_memory_id UUID;

UPDATE agent_profile_memory SET memory_key = COALESCE(memory_key, 'profile.statement.' || md5(fact));
UPDATE agent_profile_memory SET memory_value = COALESCE(memory_value, 'stated');
UPDATE agent_profile_memory SET status = COALESCE(status, 'ACTIVE');
UPDATE agent_profile_memory SET confidence = COALESCE(confidence, 0.7);

ALTER TABLE agent_profile_memory ALTER COLUMN memory_key SET NOT NULL;
ALTER TABLE agent_profile_memory ALTER COLUMN memory_value SET NOT NULL;
ALTER TABLE agent_profile_memory ALTER COLUMN status SET NOT NULL;
ALTER TABLE agent_profile_memory ALTER COLUMN confidence SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_agent_profile_memory_agent_id
    ON agent_profile_memory (agent_id);

CREATE INDEX IF NOT EXISTS idx_agent_profile_memory_agent_key_status
    ON agent_profile_memory (agent_id, memory_key, status);

DROP INDEX IF EXISTS uk_agent_profile_memory_agent_fact;

CREATE UNIQUE INDEX IF NOT EXISTS uk_agent_profile_memory_agent_key_active
    ON agent_profile_memory (agent_id, memory_key)
    WHERE status = 'ACTIVE';
