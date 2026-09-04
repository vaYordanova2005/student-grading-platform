CREATE TABLE calendar_events (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL CHECK (type IN ('TEST', 'HOLIDAY', 'EVENT')),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_calendar_events_start_date ON calendar_events(start_date);
