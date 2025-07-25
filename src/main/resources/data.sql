-- 사용자 테이블 초기 데이터
INSERT INTO users (username, password, created_at, updated_at) VALUES ('admin', 'password', NOW(), NOW()) ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 아이템 테이블 초기 데이터 (admin 사용자의 아이템들)
INSERT INTO items (name, description, price, user_id, quantity) 
VALUES 
('테스트 아이템 1', '첫 번째 테스트 아이템입니다.', 1000.0, 1, 1),
('테스트 아이템 2', '두 번째 테스트 아이템입니다.', 2000.0, 1, 1),
('테스트 아이템 3', '세 번째 테스트 아이템입니다.', 3000.0, 1, 1); 