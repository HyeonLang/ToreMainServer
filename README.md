# ToreMainServer

Spring Boot 기반의 REST API 서버입니다.

## 기능

- 사용자 인증 (로그인/회원가입)
- 아이템 관리 (CRUD)

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