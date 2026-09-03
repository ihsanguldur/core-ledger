CREATE TABLE accounts
(
    account_id UUID PRIMARY KEY,
    currency   CHAR(3)        NOT NULL,
    balance    NUMERIC(19, 4) NOT NULL DEFAULT 0,
    version    BIGINT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entries
(
    entry_id       UUID PRIMARY KEY,
    account_id     UUID           NOT NULL REFERENCES accounts (account_id),
    direction      VARCHAR(6)     NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    amount         NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency       CHAR(3)        NOT NULL,
    transaction_id UUID           NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_ledger_entries_account_created ON ledger_entries (account_id, created_at);
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries (transaction_id);

CREATE TABLE idempotency_keys
(
    idempotency_key UUID PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE outbox
(
    id           UUID PRIMARY KEY,
    aggregate_id UUID         NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    payload      JSONB        NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox (created_at) WHERE published_at IS NULL;