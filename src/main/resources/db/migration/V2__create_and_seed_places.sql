CREATE TABLE IF NOT EXISTS places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    region VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    address VARCHAR(500),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    stroller_accessible BOOLEAN NOT NULL DEFAULT FALSE,
    pet_friendly BOOLEAN NOT NULL DEFAULT FALSE,
    large_parking BOOLEAN NOT NULL DEFAULT FALSE,
    military_discount BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_places_region_category
    ON places (region, category);

-- 좌표가 확인된 실제 장소만 초기 적재합니다.
INSERT INTO places (
    name,
    region,
    category,
    address,
    latitude,
    longitude,
    stroller_accessible,
    pet_friendly,
    large_parking,
    military_discount
)
SELECT seed.*
FROM (VALUES
    ('육군훈련소', 'NONSAN', 'HERITAGE', '충남 논산시 연무읍 득안대로 504', 36.1119731, 127.1083526, TRUE, FALSE, TRUE, TRUE),
    ('관촉사', 'NONSAN', 'HERITAGE', '충남 논산시 관촉로1번길 25', 36.1884477, 127.1130246, FALSE, FALSE, TRUE, FALSE),
    ('탑정호', 'NONSAN', 'HERITAGE', '충남 논산시 부적면 부적로 721-10', 36.1783768, 127.1798397, TRUE, TRUE, TRUE, FALSE),
    ('강경구락부', 'NONSAN', 'CAFE', '충남 논산시 강경읍 옥녀봉로51번길 10', 36.1621613, 127.0156316, FALSE, FALSE, FALSE, FALSE),
    ('공산성', 'GONGJU', 'HERITAGE', '충남 공주시 웅진로 280', 36.4631426, 127.1264411, FALSE, FALSE, TRUE, FALSE),
    ('국립공주박물관', 'GONGJU', 'HERITAGE', '충남 공주시 관광단지길 34', 36.4655287, 127.1122874, TRUE, FALSE, TRUE, FALSE),
    ('공주한옥마을', 'GONGJU', 'ACCOMMODATION', '충남 공주시 관광단지길 12', 36.4647117, 127.1087143, TRUE, FALSE, TRUE, FALSE),
    ('석장리박물관', 'GONGJU', 'HERITAGE', '충남 공주시 금벽로 990', 36.4476254, 127.1895492, TRUE, FALSE, TRUE, FALSE),
    ('계룡산자연사박물관', 'GONGJU', 'HERITAGE', '충남 공주시 반포면 임금봉길 49-25', 36.3664228, 127.2448838, TRUE, FALSE, TRUE, FALSE),
    ('동학사', 'GONGJU', 'HERITAGE', '충남 공주시 반포면 동학사1로 462', 36.3532371, 127.2197807, FALSE, FALSE, TRUE, FALSE),
    ('백제문화단지', 'BUYEO', 'HERITAGE', '충남 부여군 규암면 백제문로 455', 36.3073820, 126.9072410, TRUE, FALSE, TRUE, FALSE),
    ('부소산성', 'BUYEO', 'HERITAGE', '충남 부여군 부여읍 부소로 31', 36.2892424, 126.9151865, FALSE, FALSE, TRUE, FALSE),
    ('국립부여박물관', 'BUYEO', 'HERITAGE', '충남 부여군 부여읍 금성로 5', 36.2763114, 126.9190510, TRUE, FALSE, TRUE, FALSE),
    ('정림사지박물관', 'BUYEO', 'HERITAGE', '충남 부여군 부여읍 정림로 83', 36.2792287, 126.9151939, TRUE, FALSE, TRUE, FALSE),
    ('무량사', 'BUYEO', 'HERITAGE', '충남 부여군 외산면 무량로 203', 36.3170600, 126.6928680, FALSE, FALSE, TRUE, FALSE)
) AS seed(
    name,
    region,
    category,
    address,
    latitude,
    longitude,
    stroller_accessible,
    pet_friendly,
    large_parking,
    military_discount
)
WHERE NOT EXISTS (
    SELECT 1
    FROM places existing
    WHERE existing.region = seed.region
      AND existing.name = seed.name
);
