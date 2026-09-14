CREATE TABLE IF NOT EXISTS course_bookmarks (
    id BIGSERIAL PRIMARY KEY,
    firebase_uid VARCHAR(128) NOT NULL,
    route_key VARCHAR(255) NOT NULL,
    region VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    spots_json TEXT NOT NULL,
    total_distance_meters INTEGER NOT NULL DEFAULT 0,
    total_duration_seconds INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_bookmark_user_route UNIQUE (firebase_uid, route_key)
);

CREATE INDEX IF NOT EXISTS idx_course_bookmarks_popular
    ON course_bookmarks (route_key, created_at);
