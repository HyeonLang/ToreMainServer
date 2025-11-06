-- NFT 판매 주문 테이블
CREATE TABLE IF NOT EXISTS nft_sell_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(255) UNIQUE NOT NULL,
    seller VARCHAR(255) NOT NULL,
    nft_contract VARCHAR(255) NOT NULL,
    token_id VARCHAR(255) NOT NULL,
    price VARCHAR(255) NOT NULL,
    currency VARCHAR(50) NOT NULL,
    nonce BIGINT NOT NULL,
    deadline BIGINT NOT NULL,
    signature TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    buyer VARCHAR(255),
    matched_at BIGINT,
    locked_by VARCHAR(255),
    locked_at BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_seller (seller),
    INDEX idx_status (status),
    INDEX idx_nft_contract_token (nft_contract, token_id),
    INDEX idx_currency (currency),
    INDEX idx_deadline (deadline),
    -- 활성 주문 조회 최적화를 위한 복합 인덱스
    INDEX idx_active_orders (status, created_at),
    INDEX idx_seller_status (seller, status),
    INDEX idx_status_currency (status, currency),
    INDEX idx_status_deadline (status, deadline)
);

-- 아이템 위치 타입 테이블
CREATE TABLE IF NOT EXISTS item_location_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code_name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT
);

-- user_equip_items 테이블에 user_id 외래키 제약조건 추가
-- 참고: JPA가 테이블을 자동 생성하는 경우(ddl-auto=create-drop), 외래키는 자동 생성되지 않을 수 있음
-- 운영 환경에서는 마이그레이션 도구(Flyway, Liquibase 등)를 사용하여 외래키를 추가하는 것을 권장
-- 다음 SQL은 외래키가 이미 존재하지 않는 경우에만 실행됨 (수동 실행 필요)
/*
ALTER TABLE user_equip_items 
ADD CONSTRAINT fk_user_equip_item_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE SET NULL ON UPDATE CASCADE;
*/

