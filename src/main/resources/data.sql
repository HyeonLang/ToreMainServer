-- 사용자 테이블 초기 데이터
INSERT INTO users (username, password, playername, created_at, updated_at) VALUES ('admin', 'password', '관리자', NOW(), NOW()) ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 아이템 정의 테이블 초기 데이터
INSERT INTO item_definitions (id, name, type, base_stats, description, is_stackable, max_stack) 
VALUES 
(1, '체력 물약', 'CONSUMABLE', '{"heal": 50, "duration": 0}', '체력을 50 회복시키는 물약입니다.', true, 99),
(2, '마나 물약', 'CONSUMABLE', '{"mana": 30, "duration": 0}', '마나를 30 회복시키는 물약입니다.', true, 99),
(3, '강화석', 'ETC', '{"enhancement": 1}', '장비 강화에 사용되는 강화석입니다.', true, 999),
(4, '철검', 'EQUIPMENT', '{"attack": 15, "durability": 100}', '기본적인 철검입니다.', false, 1),
(5, '가죽 갑옷', 'EQUIPMENT', '{"defense": 10, "durability": 80}', '가죽으로 만든 갑옷입니다.', false, 1);

-- 사용자 소비 아이템 테이블 초기 데이터
INSERT INTO user_consumable_items (user_id, item_id, quantity, local_item_id) 
VALUES 
(1, 1, 10, 1001), -- admin이 체력 물약 10개
(1, 2, 5, 1002),  -- admin이 마나 물약 5개
(1, 3, 50, 1003); -- admin이 강화석 50개

-- 사용자 장비 아이템 테이블 초기 데이터
INSERT INTO user_equip_items (user_id, item_id, enhancement_data, nft_id, local_item_id) 
VALUES 
(1, 4, '{"level": 1, "enhancement": 0}', NULL, 2001), -- admin의 철검
(1, 5, '{"level": 1, "enhancement": 0}', NULL, 2002); -- admin의 가죽 갑옷

-- NPC 테이블 초기 데이터
INSERT INTO npcs (npc_id, name, npc_info) 
VALUES 
(1, '상인', '{"description": "마을의 상인", "personality": "친근하고 도움이 되는", "role": "상인", "location": "마을 광장"}'),
(2, '대장장이', '{"description": "마을의 대장장이", "personality": "성실하고 정확한", "role": "대장장이", "location": "대장간"}'),
(3, '여관주인', '{"description": "마을의 여관주인", "personality": "따뜻하고 환영하는", "role": "여관주인", "location": "여관"}'); 