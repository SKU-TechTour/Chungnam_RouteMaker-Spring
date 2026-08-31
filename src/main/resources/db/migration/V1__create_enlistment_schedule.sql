CREATE TABLE IF NOT EXISTS enlistment_schedule (
    id BIGSERIAL PRIMARY KEY,
    schedule_code VARCHAR(40) NOT NULL UNIQUE,
    title VARCHAR(120) NOT NULL,
    military_branch VARCHAR(30) NOT NULL,
    recruitment_type VARCHAR(60) NOT NULL,
    application_start_date DATE NOT NULL,
    application_end_date DATE NOT NULL,
    enlistment_date DATE NOT NULL,
    training_center VARCHAR(120) NOT NULL,
    region VARCHAR(30) NOT NULL,
    source_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_enlistment_schedule_branch
        CHECK (military_branch IN ('ARMY', 'NAVY', 'AIR_FORCE', 'MARINE_CORPS')),
    CONSTRAINT chk_enlistment_schedule_application_period
        CHECK (application_start_date <= application_end_date)
);

CREATE INDEX IF NOT EXISTS idx_enlistment_schedule_enlistment_date
    ON enlistment_schedule (enlistment_date);

CREATE INDEX IF NOT EXISTS idx_enlistment_schedule_branch_date
    ON enlistment_schedule (military_branch, enlistment_date);

-- 병무청 연동 전 화면/API 개발을 위한 수동 초기 데이터입니다.
-- 운영 반영 전 병무청 공식 공고와 날짜를 반드시 대조해야 합니다.
INSERT INTO enlistment_schedule (
    schedule_code,
    title,
    military_branch,
    recruitment_type,
    application_start_date,
    application_end_date,
    enlistment_date,
    training_center,
    region,
    source_url
) VALUES
    ('ARMY-2026-0907', '육군 현역병 9월 1차 입영', 'ARMY', '현역병', '2026-07-01', '2026-07-14', '2026-09-07', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-0914', '육군 현역병 9월 2차 입영', 'ARMY', '현역병', '2026-07-08', '2026-07-21', '2026-09-14', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-0921', '육군 기술행정병 9월 입영', 'ARMY', '기술행정병', '2026-07-15', '2026-07-28', '2026-09-21', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-0928', '육군 현역병 9월 3차 입영', 'ARMY', '현역병', '2026-07-22', '2026-08-04', '2026-09-28', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-1012', '육군 현역병 10월 1차 입영', 'ARMY', '현역병', '2026-08-03', '2026-08-16', '2026-10-12', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-1019', '육군 기술행정병 10월 입영', 'ARMY', '기술행정병', '2026-08-10', '2026-08-23', '2026-10-19', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-1102', '육군 현역병 11월 1차 입영', 'ARMY', '현역병', '2026-09-01', '2026-09-14', '2026-11-02', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-1116', '육군 현역병 11월 2차 입영', 'ARMY', '현역병', '2026-09-15', '2026-09-28', '2026-11-16', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-1207', '육군 기술행정병 12월 입영', 'ARMY', '기술행정병', '2026-10-01', '2026-10-14', '2026-12-07', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('ARMY-2026-1221', '육군 현역병 12월 입영', 'ARMY', '현역병', '2026-10-15', '2026-10-28', '2026-12-21', '육군훈련소', '충남 논산', 'https://www.mma.go.kr'),
    ('NAVY-2026-1005', '해군 일반기술병 10월 입영', 'NAVY', '일반기술병', '2026-07-27', '2026-08-09', '2026-10-05', '해군교육사령부', '경남 창원', 'https://www.mma.go.kr'),
    ('NAVY-2026-1109', '해군 전문기술병 11월 입영', 'NAVY', '전문기술병', '2026-09-01', '2026-09-14', '2026-11-09', '해군교육사령부', '경남 창원', 'https://www.mma.go.kr'),
    ('NAVY-2026-1214', '해군 일반기술병 12월 입영', 'NAVY', '일반기술병', '2026-10-05', '2026-10-18', '2026-12-14', '해군교육사령부', '경남 창원', 'https://www.mma.go.kr'),
    ('AIR-2026-1006', '공군 일반기술병 10월 입영', 'AIR_FORCE', '일반기술병', '2026-07-28', '2026-08-10', '2026-10-06', '공군교육사령부', '경남 진주', 'https://www.mma.go.kr'),
    ('AIR-2026-1110', '공군 전문기술병 11월 입영', 'AIR_FORCE', '전문기술병', '2026-09-02', '2026-09-15', '2026-11-10', '공군교육사령부', '경남 진주', 'https://www.mma.go.kr'),
    ('AIR-2026-1208', '공군 일반기술병 12월 입영', 'AIR_FORCE', '일반기술병', '2026-10-01', '2026-10-14', '2026-12-08', '공군교육사령부', '경남 진주', 'https://www.mma.go.kr'),
    ('MARINE-2026-0928', '해병대 일반기술병 9월 입영', 'MARINE_CORPS', '일반기술병', '2026-07-20', '2026-08-02', '2026-09-28', '해병대교육훈련단', '경북 포항', 'https://www.mma.go.kr'),
    ('MARINE-2026-1026', '해병대 전문기술병 10월 입영', 'MARINE_CORPS', '전문기술병', '2026-08-17', '2026-08-30', '2026-10-26', '해병대교육훈련단', '경북 포항', 'https://www.mma.go.kr'),
    ('MARINE-2026-1123', '해병대 일반기술병 11월 입영', 'MARINE_CORPS', '일반기술병', '2026-09-14', '2026-09-27', '2026-11-23', '해병대교육훈련단', '경북 포항', 'https://www.mma.go.kr'),
    ('MARINE-2026-1221', '해병대 전문기술병 12월 입영', 'MARINE_CORPS', '전문기술병', '2026-10-12', '2026-10-25', '2026-12-21', '해병대교육훈련단', '경북 포항', 'https://www.mma.go.kr')
ON CONFLICT (schedule_code) DO NOTHING;
