-- Account state (#3) and token invalidation (#5) both live on the user row:
-- the JWT filter reloads the user on every request anyway, so a change here
-- takes effect immediately for already-issued tokens.
ALTER TABLE users ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN locked_until TIMESTAMP;
ALTER TABLE users ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0;
-- Bumped on logout, deactivation and unlock; a token carrying an older
-- version is rejected even though it has not expired yet.
ALTER TABLE users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
