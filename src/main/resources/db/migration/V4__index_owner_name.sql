-- Task E: a new versioned migration added after V1-V3 were already applied
CREATE INDEX idx_accounts_owner_name ON accounts (owner_name);
