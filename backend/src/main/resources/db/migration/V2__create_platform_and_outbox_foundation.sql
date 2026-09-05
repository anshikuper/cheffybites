CREATE TABLE platform.data_scopes (
    id UUID NOT NULL PRIMARY KEY,
    environment_key TEXT NOT NULL,
    record_mode TEXT NOT NULL,
    resettable BOOLEAN NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (environment_key, id),
    CHECK (record_mode IN ('DEMO', 'REAL')),
    CHECK (resettable = FALSE OR record_mode = 'DEMO')
);

CREATE TABLE platform.pilot_stage_state (
    singleton_id SMALLINT NOT NULL PRIMARY KEY,
    stage TEXT NOT NULL,
    version INTEGER NOT NULL,
    changed_by_user_id UUID NULL,
    change_reason TEXT NULL,
    changed_at TIMESTAMPTZ NOT NULL,
    CHECK (singleton_id = 1),
    CHECK (stage IN ('PRE_PILOT', 'CONTROLLED_PILOT'))
);

CREATE TABLE platform.pilot_stage_history (
    id UUID NOT NULL PRIMARY KEY,
    from_stage TEXT NULL,
    to_stage TEXT NOT NULL,
    changed_by_user_id UUID NULL,
    change_reason TEXT NULL,
    changed_at TIMESTAMPTZ NOT NULL,
    CHECK (from_stage IS NULL OR from_stage IN ('PRE_PILOT', 'CONTROLLED_PILOT')),
    CHECK (to_stage IN ('PRE_PILOT', 'CONTROLLED_PILOT'))
);

CREATE TABLE outbox.outbox_events (
    id UUID NOT NULL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    event_version INT NOT NULL DEFAULT 1,
    correlation_id UUID NULL,
    causation_id UUID NULL,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT NULL,
    next_attempt_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_unpublished
    ON outbox.outbox_events (published_at, next_attempt_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_outbox_aggregate
    ON outbox.outbox_events (aggregate_type, aggregate_id);

CREATE INDEX idx_outbox_correlation
    ON outbox.outbox_events (correlation_id)
    WHERE correlation_id IS NOT NULL;
