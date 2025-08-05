# ToreMainServer

Spring Boot 기반의 REST API 서버입니다.

## 기능

- 사용자 인증 (로그인/회원가입)
- 아이템 관리 (CRUD)
- NPC 관리 및 대화 시스템
- 게임 이벤트 처리
- AI 연동 게이트웨이 기능

## API 엔드포인트

### 인증 API
- `POST /api/login` - 로그인
- `POST /api/register` - 회원가입

### 아이템 API
- `POST /api/item?userId={user_id}` - 아이템 추가
- `GET /api/items` - 모든 아이템 조회
- `GET /api/items/{user_id}` - 특정 사용자의 모든 아이템 조회
- `GET /api/item/{id}` - 특정 아이템 조회
- `PATCH /api/item/{id}` - 아이템 수정
- `DELETE /api/item/{id}` - 아이템 삭제

### 게임 이벤트 API
- `POST /api/npc` - NPC 대화 요청
- `POST /api/material` - 질감/색 문장 → 이미지 변환 요청

## 데이터베이스 설정

### MySQL 설정
1. MySQL 서버를 설치하고 실행합니다.
2. 데이터베이스를 생성합니다:
   ```sql
   CREATE DATABASE tore_main_server CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. `src/main/resources/application.properties`에서 데이터베이스 연결 정보를 수정합니다:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### 데이터베이스 스키마
애플리케이션 시작 시 자동으로 다음 테이블들이 생성됩니다:

- **users**: 사용자 정보 (id, username, password, playername, created_at, updated_at)
- **npcs**: NPC 정보 (npc_id, name, npc_info JSON)
- **item_definitions**: 아이템 정의 (id, name, description, type, base_stats JSON, is_stackable, max_stack)
- **user_consumable_items**: 사용자 소비 아이템 (user_id, item_id, quantity, local_item_id)
- **user_equip_items**: 사용자 장비 아이템 (id, user_id, item_id, local_item_id, nft_id, enhancement_data JSON)

### 초기 데이터
애플리케이션 시작 시 다음 초기 데이터가 자동으로 생성됩니다:
- 기본 관리자 사용자 (username: admin, password: password)
- 테스트용 NPC 및 아이템 데이터

## 실행 방법

1. MySQL 서버가 실행 중인지 확인합니다.
2. 프로젝트 루트 디렉토리에서 다음 명령을 실행합니다:
   ```bash
   ./gradlew bootRun
   ```
3. 애플리케이션이 `http://localhost:8080`에서 실행됩니다.

## 테스트

### 기본 사용자 정보
- 사용자명: `admin`
- 비밀번호: `password`

### 데이터베이스 연결 테스트
애플리케이션 시작 시 자동으로 다음 테스트가 실행됩니다:
- User 테스트: 기본 관리자 사용자 조회
- NPC 테스트: NPC 생성 및 조회
- ItemDefinition 테스트: 아이템 정의 생성 및 조회
- UserConsumableItem 테스트: 소비 아이템 생성 및 조회
- UserEquipItem 테스트: 장비 아이템 생성 및 조회

### API 테스트 예시

#### 로그인
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

#### 아이템 추가
```bash
curl -X POST "http://localhost:8080/api/item?userId=user1" \
  -H "Content-Type: application/json" \
  -d '{"name":"새 아이템","description":"새로운 아이템입니다","price":1500.0}'
```

#### 모든 아이템 조회
```bash
curl -X GET http://localhost:8080/api/items
```

#### 사용자별 아이템 조회
```bash
curl -X GET http://localhost:8080/api/items/user1
```

## 기술 스택

- Spring Boot 3.5.3
- Spring Data JPA
- MySQL
- Gradle
- Java 17
- Hibernate (JPA 구현체)
- HikariCP (커넥션 풀)

# AI 연동 API 사용법

## 1. AI NPC 대화
- **엔드포인트:** `POST /api.ai/npc`
- **요청 형식:** JSON
- **요청 예시:**
```json
{
  "npcId": 1,
  "prompt": "안녕?",
  "history": ["안녕하세요.", "무엇을 도와드릴까요?"]
}
```
- **응답 예시:**
```json
{
  "answer": "안녕하세요, 무엇을 도와드릴까요?"
}
```
- **설명:**
  - npcId: 대화할 NPC의 고유 ID
  - prompt: 사용자가 NPC에게 보낼 현재 질문/명령
  - history: 이전 대화 기록(문자열 배열)
  - answer: AI가 생성한 NPC의 답변

---

## 2. 질감/색 문장 → 이미지 변환
- **엔드포인트:** `POST /api.ai/material`
- **요청 형식:** JSON
- **요청 예시:**
```json
{
  "description": "거친 돌 질감, 파란색"
}
```
- **응답 형식:** multipart/form-data (PNG 파일 2개)
  - normalMap: normalMap.png (image/png)
  - baseColor: baseColor.png (image/png)
- **설명:**
  - description: 질감/색에 대한 자연어 설명
  - normalMap: 생성된 노멀맵 이미지 (PNG)
  - baseColor: 생성된 베이스컬러 이미지 (PNG)

---

## 전체 흐름 요약
1. 클라이언트가 JSON body로 요청을 보냄
2. 서버가 요청을 받아 AI 처리(Service에서 구현)
3. 결과를 JSON 또는 multipart/form-data로 응답

# UE5 연동 게이트웨이 서버 설명

## 개요
이 서버는 UE5(게임 서버)와 파이썬 AI 서버 사이의 **게이트웨이** 역할만 수행합니다.
- UE5에서 오는 요청을 받아, 파이썬 AI 서버(`http://localhost:8000`)로 정보를 전달하고,
- 파이썬 AI 서버의 응답을 UE5로 그대로 반환합니다.
- AI 관련 실제 처리는 모두 파이썬 서버에서 수행합니다.

---

## 전체 서버 흐름

1. **UE5(클라이언트)가 Spring 서버의 엔드포인트로 요청을 보냄**
   - 예) `/api/npc`, `/api/material`
2. **GameEventController가 요청을 받아 GameEventService로 전달**
3. **GameEventService가 파이썬 AI 서버(`http://localhost:8000`)로 POST 요청을 보냄**
   - `/api.ai/npc`, `/api.ai/material` 엔드포인트로 전달
4. **파이썬 AI 서버의 응답을 Spring 서버가 받아서 UE5로 그대로 반환**

---

## 엔드포인트별 상세 흐름

### 1) NPC 대화 요청
- **엔드포인트:** `POST /api/npc`
- **요청 예시:**
```json
{
  "npcId": 1,
  "history": ["안녕", "무엇을 도와줄까?"]
}
```
- **처리 흐름:**
  1. UE5가 `/api/npc`로 요청
  2. GameEventController → GameEventService로 전달
  3. GameEventService가 파이썬 AI 서버(`http://localhost:8000/api.ai/npc`)로 POST 요청
  4. 파이썬 AI 서버의 응답(JSON)을 UE5로 그대로 반환

### 2) 질감/색 문장 요청
- **엔드포인트:** `POST /api/material`
- **요청 예시:**
```json
{
  "description": "거친 돌 질감, 파란색"
}
```
- **처리 흐름:**
  1. UE5가 `/api/material`로 요청
  2. GameEventController → GameEventService로 전달
  3. GameEventService가 파이썬 AI 서버(`http://localhost:8000/api.ai/material`)로 POST 요청
  4. 파이썬 AI 서버의 응답(multipart/form-data)을 UE5로 그대로 반환

---

## 주요 함수 설명

- `GameEventService.forwardNpcRequest(Map<String, Object> body)`
  - UE5에서 받은 NPC 대화 정보를 파이썬 AI 서버로 POST 요청
  - 파이썬 AI 서버의 응답(JSON)을 그대로 반환

- `GameEventService.forwardMaterialRequest(Map<String, Object> body)`
  - UE5에서 받은 질감/색 정보를 파이썬 AI 서버로 POST 요청
  - 파이썬 AI 서버의 응답(multipart/form-data)을 그대로 반환

---

## 요약
- 이 서버는 **비즈니스 로직 없이 게이트웨이 역할**만 수행합니다.
- 모든 AI 관련 처리는 파이썬 서버에서 담당합니다.

## 최근 수정사항

### 2025-08-05
- **데이터베이스 엔티티 수정**: `UserConsumableItem`과 `UserEquipItem` 엔티티의 `local_item_id` 필드가 null이 될 수 없도록 설정되어 있어, 테스트 코드에서 해당 값을 추가로 전달하도록 수정
- **테스트 코드 개선**: 데이터베이스 연결 테스트에서 `localItemId` 값을 포함하여 엔티티 생성하도록 수정
- **로그 출력 개선**: 생성 성공 메시지에 `LocalItemID` 정보를 추가하여 디버깅 용이성 향상