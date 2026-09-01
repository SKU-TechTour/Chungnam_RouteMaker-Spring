-- 기존 개발 DB의 users 테이블을 Firebase Authentication 사용자와 연결합니다.
-- 신규 DB에서는 Hibernate가 User 엔티티의 firebase_uid 컬럼을 생성합니다.
DO $$
BEGIN
    IF to_regclass('public.users') IS NOT NULL THEN
        ALTER TABLE users
            ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(128);

        CREATE UNIQUE INDEX IF NOT EXISTS uk_users_firebase_uid
            ON users (firebase_uid)
            WHERE firebase_uid IS NOT NULL;
    END IF;
END
$$;
