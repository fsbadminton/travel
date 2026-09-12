-- Demo places for the second user (for example, the niuniu account).
-- Each insert is guarded so the migration is safe when re-run manually.

INSERT INTO places (
  user_id,
  name,
  country,
  province,
  city,
  address,
  longitude,
  latitude,
  visit_date,
  status,
  guide_content
)
SELECT
  2,
  '南京夫子庙',
  '中国',
  '江苏省',
  '南京市',
  '南京市秦淮区夫子庙步行街',
  118.7879,
  32.0232,
  '2025-04-12',
  'published',
  '傍晚到夫子庙最有氛围，先逛秦淮河两岸，再乘画舫夜游。小吃可安排在步行街，节假日人流较大，建议提前预约游船。'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2)
  AND NOT EXISTS (SELECT 1 FROM places WHERE user_id = 2 AND name = '南京夫子庙');

INSERT INTO places (
  user_id,
  name,
  country,
  province,
  city,
  address,
  longitude,
  latitude,
  visit_date,
  status,
  guide_content
)
SELECT
  2,
  '中山陵',
  '中国',
  '江苏省',
  '南京市',
  '南京市玄武区石象路7号',
  118.8564,
  32.0603,
  '2025-04-13',
  'published',
  '建议上午前往，沿 392 级台阶登顶视野最好。景区步行距离较长，可乘接驳车到达核心区域，穿舒适的鞋并预留半天时间。'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2)
  AND NOT EXISTS (SELECT 1 FROM places WHERE user_id = 2 AND name = '中山陵');

INSERT INTO places (
  user_id,
  name,
  country,
  province,
  city,
  address,
  longitude,
  latitude,
  visit_date,
  status,
  guide_content
)
SELECT
  2,
  '西湖',
  '中国',
  '浙江省',
  '杭州市',
  '杭州市西湖区西湖风景名胜区',
  120.1480,
  30.2420,
  '2025-05-02',
  'published',
  '环湖路线可从断桥开始，经过白堤、孤山和曲院风荷。建议早晨或日落时拍照，公共自行车和地铁接驳都很方便。'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2)
  AND NOT EXISTS (SELECT 1 FROM places WHERE user_id = 2 AND name = '西湖');

INSERT INTO places (
  user_id,
  name,
  country,
  province,
  city,
  address,
  longitude,
  latitude,
  visit_date,
  status,
  guide_content
)
SELECT
  2,
  '外滩',
  '中国',
  '上海市',
  '上海市',
  '上海市黄浦区中山东一路',
  121.4903,
  31.2397,
  '2025-05-18',
  'published',
  '傍晚沿外滩步行欣赏万国建筑群，天黑后可同时看到陆家嘴灯光。周末人流较多，建议从南京东路地铁站步行前往。'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2)
  AND NOT EXISTS (SELECT 1 FROM places WHERE user_id = 2 AND name = '外滩');

INSERT INTO places (
  user_id,
  name,
  country,
  province,
  city,
  address,
  longitude,
  latitude,
  visit_date,
  status,
  guide_content
)
SELECT
  2,
  '滕王阁',
  '中国',
  '江西省',
  '南昌市',
  '南昌市东湖区仿古街58号',
  115.8840,
  28.6832,
  '2025-06-08',
  'published',
  '建议下午先参观展厅，日落前登阁看赣江景色，晚上可观看实景演出。景区附近步行街适合安排晚餐。'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2)
  AND NOT EXISTS (SELECT 1 FROM places WHERE user_id = 2 AND name = '滕王阁');
