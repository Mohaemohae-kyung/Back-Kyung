아래 내용으로 `docs/db/db-structure.md` 전체를 다시 넣으면 됩니다.
`MATCHES` 제거 후 기준이고, **테이블 목록 포함**되어 있습니다.

````md
# DB Structure 초안

## 1. 목적

본 문서는 숨고 유사 전문가 매칭 서비스의 초기 DB Structure를 정리하기 위한 문서입니다.

팀원들이 각자 기능 개발을 시작하기 전에 공통으로 참고할 수 있는 테이블 목록, 도메인 분류, 주요 관계를 정리하는 것을 목적으로 합니다.

현재 DB 설계는 Oracle DB 기준으로 작성되었으며, Spring Boot JPA Entity 구조와 연결됩니다.

---

## 2. 기술 기준

| 항목 | 내용 |
|---|---|
| Backend | Spring Boot 3.2.3 |
| Java | Java 17 |
| ORM | JPA / Hibernate |
| DB | Oracle Database Free |
| ID 전략 | Oracle Sequence |
| ID 타입 | Long |
| 테이블명 | 대문자 스네이크 케이스 |
| 컬럼명 | 대문자 스네이크 케이스 |
| 날짜/시간 타입 | TIMESTAMP |
| 금액 타입 | NUMBER(12,2) |
| Boolean 성격 | CHAR(1) 또는 String 매핑 |

---

## 3. 서비스 흐름 변경 요약

기존에는 견적 요청 이후 `MATCHES` 테이블을 통해 매칭 제안을 관리하는 구조를 고려했습니다.

```text
SERVICE_REQUESTS → MATCHES → BOOKINGS
````

하지만 현재 프로젝트는 실제 운영 서비스가 아니라 모의해킹/보안 검증 목적의 개발 프로젝트이므로, 매칭/예약 흐름을 단순화합니다.

변경된 흐름은 다음과 같습니다.

```text
사용자 견적 요청 생성
→ 고수가 견적 요청 확인
→ 고수가 승인하면 SERVICE_REQUESTS 상태를 CHATTING으로 변경
→ CHAT_ROOMS 생성
→ 채팅에서 일정/금액/진행 방식 협의
→ 결제 또는 거래 진행
→ 완료 시 SERVICE_REQUESTS 상태를 COMPLETED로 변경
```

따라서 `MATCHES` 테이블은 제거하고, `SERVICE_REQUESTS`와 `CHAT_ROOMS`를 직접 연결합니다.

---

## 4. 전체 테이블 목록

현재 Entity 기준으로 관리하는 주요 테이블은 다음과 같습니다.

| 번호 | 테이블명                  | 설명              |
| -: | --------------------- | --------------- |
|  1 | USERS                 | 회원 정보           |
|  2 | EXPERT_PROFILES       | 고수 프로필          |
|  3 | SERVICE_CATEGORIES    | 서비스 카테고리        |
|  4 | LOCATIONS             | 지역 정보           |
|  5 | EXPERT_SERVICES       | 고수가 등록한 서비스 게시글 |
|  6 | SERVICE_REQUESTS      | 사용자의 견적 요청      |
|  7 | BOOKINGS              | 마켓 상품 예약        |
|  8 | TRANSACTIONS          | 거래 내역           |
|  9 | PAYMENTS              | 결제 내역           |
| 10 | SERVICE_PAY_ACCOUNTS  | 숨고페이 계정         |
| 11 | SERVICE_PAY_HISTORIES | 숨고페이 사용/충전 이력   |
| 12 | FAVORITE_EXPERTS      | 찜한 고수           |
| 13 | STORE_PRODUCTS        | 마켓 상품           |
| 14 | PURCHASES             | 마켓 구매 내역        |
| 15 | COMMUNITY_POSTS       | 커뮤니티 게시글        |
| 16 | COMMUNITY_COMMENTS    | 커뮤니티 댓글         |
| 17 | NOTIFICATIONS         | 알림              |
| 18 | FILE_UPLOADS          | 파일 업로드          |
| 19 | ADMIN_ACTIONS         | 관리자 조치 이력       |
| 20 | NOTICES               | 공지사항            |
| 21 | CHAT_ROOMS            | 채팅방             |
| 22 | CHAT_MESSAGES         | 채팅 메시지          |
| 23 | REVIEWS               | 후기              |
| 24 | COUPONS               | 쿠폰              |
| 25 | USER_COUPONS          | 사용자 보유 쿠폰       |

> `MATCHES` 테이블은 제거합니다.
> 견적 요청 승인/거절 흐름은 `SERVICE_REQUESTS.STATUS`로 관리합니다.

---

## 5. 도메인별 테이블 분류

### 회원 / 인증

| 테이블명  | 설명                        |
| ----- | ------------------------- |
| USERS | 일반 사용자, 고수, 관리자 계정을 통합 관리 |

### 고수 / 서비스

| 테이블명               | 설명              |
| ------------------ | --------------- |
| EXPERT_PROFILES    | 고수 프로필 정보       |
| EXPERT_SERVICES    | 고수가 등록한 서비스 게시글 |
| SERVICE_CATEGORIES | 서비스 대분류/소분류     |
| LOCATIONS          | 지역 대분류/소분류      |
| FAVORITE_EXPERTS   | 사용자가 찜한 고수 정보   |

### 견적 요청 / 채팅

| 테이블명             | 설명                       |
| ---------------- | ------------------------ |
| SERVICE_REQUESTS | 일반 사용자의 견적 요청            |
| CHAT_ROOMS       | 고수가 견적 요청을 승인하면 생성되는 채팅방 |
| CHAT_MESSAGES    | 채팅방 내 메시지                |

### 예약 / 거래 / 결제

| 테이블명                  | 설명                        |
| --------------------- | ------------------------- |
| BOOKINGS              | 마켓 상품에 대한 날짜/시간 예약        |
| TRANSACTIONS          | 서비스 요청 또는 마켓 구매에 대한 거래 단위 |
| PAYMENTS              | 실제 결제 시도 및 결제 결과          |
| SERVICE_PAY_ACCOUNTS  | 사용자별 숨고페이 계정              |
| SERVICE_PAY_HISTORIES | 숨고페이 충전, 사용, 환불 이력        |
| COUPONS               | 쿠폰 정책                     |
| USER_COUPONS          | 사용자에게 발급된 쿠폰              |

### 마켓 / 구매

| 테이블명           | 설명          |
| -------------- | ----------- |
| STORE_PRODUCTS | 마켓 상품       |
| PURCHASES      | 마켓 상품 구매 내역 |
| BOOKINGS       | 마켓 상품 예약    |

### 커뮤니티

| 테이블명               | 설명            |
| ------------------ | ------------- |
| COMMUNITY_POSTS    | 커뮤니티 게시글      |
| COMMUNITY_COMMENTS | 커뮤니티 댓글 및 대댓글 |

### 알림

| 테이블명          | 설명     |
| ------------- | ------ |
| NOTIFICATIONS | 사용자 알림 |

### 파일 / 관리자 / 공지

| 테이블명          | 설명                      |
| ------------- | ----------------------- |
| FILE_UPLOADS  | 프로필, 게시글, 채팅 등에 사용되는 파일 |
| ADMIN_ACTIONS | 관리자 조치 이력               |
| NOTICES       | 공지사항                    |

---

## 6. SERVICE_REQUESTS 상태값

`SERVICE_REQUESTS`는 견적 요청의 전체 흐름을 상태값으로 관리합니다.

| 상태값       | 설명                                  |
| --------- | ----------------------------------- |
| PENDING   | 사용자가 견적 요청을 생성했고, 아직 고수가 응답하지 않은 상태 |
| CHATTING  | 고수가 요청을 승인했고, 채팅방이 생성되어 협의 중인 상태    |
| COMPLETED | 채팅/결제/거래가 최종 완료된 상태                 |
| REJECTED  | 고수가 견적 요청을 거절한 상태                   |
| CANCELLED | 사용자가 요청을 취소한 상태                     |
| DELETED   | 관리 또는 소프트 삭제 처리된 상태                 |

상태 흐름은 다음과 같습니다.

```text
PENDING
 ├─ 고수 승인 → CHATTING
 │              └─ 거래 완료 → COMPLETED
 │
 ├─ 고수 거절 → REJECTED
 │
 └─ 사용자 취소 → CANCELLED
```

---

## 7. 핵심 서비스 흐름별 테이블 연결

```text
회원가입/로그인:
USERS

고수 등록:
USERS → EXPERT_PROFILES → EXPERT_SERVICES

고수 검색:
SERVICE_CATEGORIES + LOCATIONS + EXPERT_PROFILES + EXPERT_SERVICES + REVIEWS + FAVORITE_EXPERTS

견적 요청:
USERS → SERVICE_REQUESTS

견적 승인/채팅:
SERVICE_REQUESTS → CHAT_ROOMS → CHAT_MESSAGES

견적 기반 거래/결제:
SERVICE_REQUESTS → TRANSACTIONS → PAYMENTS

숨고페이:
USERS → SERVICE_PAY_ACCOUNTS → SERVICE_PAY_HISTORIES

마켓/예약/구매:
STORE_PRODUCTS → BOOKINGS
STORE_PRODUCTS → PURCHASES → TRANSACTIONS → PAYMENTS

커뮤니티:
USERS → COMMUNITY_POSTS → COMMUNITY_COMMENTS

후기:
USERS → REVIEWS
EXPERT_PROFILES → REVIEWS

쿠폰:
COUPONS → USER_COUPONS → PAYMENTS

알림:
USERS → NOTIFICATIONS

관리자:
USERS → ADMIN_ACTIONS
USERS → NOTICES
```

---

## 8. 주요 관계 요약

| 관계                                             | 설명                            |
| ---------------------------------------------- | ----------------------------- |
| USERS 1:1 EXPERT_PROFILES                      | 고수 회원은 고수 프로필을 가질 수 있음        |
| USERS 1:N SERVICE_REQUESTS                     | 사용자는 여러 견적 요청을 작성할 수 있음       |
| EXPERT_PROFILES 1:N EXPERT_SERVICES            | 고수는 여러 서비스를 등록할 수 있음          |
| SERVICE_CATEGORIES 1:N EXPERT_SERVICES         | 하나의 카테고리에 여러 서비스가 속함          |
| LOCATIONS 1:N EXPERT_SERVICES                  | 하나의 지역에 여러 서비스가 연결될 수 있음      |
| EXPERT_SERVICES 1:N SERVICE_REQUESTS           | 특정 고수 서비스를 보고 견적 요청을 생성할 수 있음 |
| SERVICE_REQUESTS 1:1 CHAT_ROOMS                | 고수가 요청을 승인하면 채팅방이 생성됨         |
| CHAT_ROOMS 1:N CHAT_MESSAGES                   | 채팅방은 여러 메시지를 가짐               |
| SERVICE_REQUESTS 1:N TRANSACTIONS              | 견적 요청 기반 거래가 생성될 수 있음         |
| STORE_PRODUCTS 1:N BOOKINGS                    | 마켓 상품은 여러 예약을 가질 수 있음         |
| STORE_PRODUCTS 1:N PURCHASES                   | 마켓 상품은 여러 구매 내역을 가질 수 있음      |
| PURCHASES 1:1 TRANSACTIONS                     | 마켓 구매는 거래로 연결될 수 있음           |
| TRANSACTIONS 1:N PAYMENTS                      | 하나의 거래에 여러 결제 시도가 있을 수 있음     |
| USERS 1:1 SERVICE_PAY_ACCOUNTS                 | 사용자는 숨고페이 계정을 가질 수 있음         |
| SERVICE_PAY_ACCOUNTS 1:N SERVICE_PAY_HISTORIES | 숨고페이 계정은 여러 이력을 가짐            |
| USERS N:M EXPERT_PROFILES                      | FAVORITE_EXPERTS로 찜 관계 표현     |
| COMMUNITY_POSTS 1:N COMMUNITY_COMMENTS         | 게시글은 여러 댓글을 가짐                |
| COUPONS 1:N USER_COUPONS                       | 쿠폰은 여러 사용자에게 발급될 수 있음         |

---

## 9. Match 제거에 따른 ERD 수정 사항

### 제거 대상

| 제거 대상     | 설명                            |
| --------- | ----------------------------- |
| MATCHES   | 매칭 제안 테이블 제거                  |
| MATCH_ID  | 다른 테이블에서 참조하던 Match FK 제거     |
| Match API | Service Request 승인/거절 API로 대체 |

### 수정 대상

| 기존 구조                                 | 변경 구조                         |
| ------------------------------------- | ----------------------------- |
| SERVICE_REQUESTS → MATCHES → BOOKINGS | SERVICE_REQUESTS → CHAT_ROOMS |
| CHAT_ROOMS.MATCH_ID                   | CHAT_ROOMS.REQUEST_ID         |
| BOOKINGS.MATCH_ID                     | 제거                            |
| BOOKINGS                              | 마켓 상품 예약 전용으로 사용              |

### CHAT_ROOMS 수정 방향

`CHAT_ROOMS`는 `MATCH_ID` 대신 `REQUEST_ID`를 가집니다.

```text
CHAT_ROOMS
- CHAT_ROOM_ID
- REQUEST_ID
- USER_ID
- EXPERT_PROFILE_ID
- STATUS
- CLOSED_AT
- CREATED_AT
- UPDATED_AT
```

관계:

```text
SERVICE_REQUESTS 1:1 CHAT_ROOMS
USERS 1:N CHAT_ROOMS
EXPERT_PROFILES 1:N CHAT_ROOMS
CHAT_ROOMS 1:N CHAT_MESSAGES
```

---

## 10. Booking 사용 기준

`BOOKINGS`는 더 이상 견적 요청 흐름에서 사용하지 않습니다.

기존 견적 흐름:

```text
SERVICE_REQUESTS → MATCHES → BOOKINGS
```

변경 후 견적 흐름:

```text
SERVICE_REQUESTS → CHAT_ROOMS → TRANSACTIONS/PAYMENTS
```

`BOOKINGS`는 마켓 상품 예약 기능에서 사용합니다.

```text
마켓 상품 상세
→ 날짜/시간 선택
→ 예약하기
→ BOOKINGS 생성
→ 결제 진행
```

추천 관계:

```text
STORE_PRODUCTS → BOOKINGS → TRANSACTIONS → PAYMENTS
```

---

## 11. 결제/거래 단순화 방향

견적 요청 기반 거래는 다음처럼 단순화합니다.

```text
채팅에서 금액 협의
→ 결제 요청 생성
→ TRANSACTIONS 생성
→ PAYMENTS 생성
→ 결제 성공
→ SERVICE_REQUESTS.STATUS = COMPLETED
```

초기 구현에서는 `TRANSACTIONS`를 거래 단위로 사용하고, `PAYMENTS`는 실제 결제 시도/결과를 관리합니다.

추천 연결:

```text
SERVICE_REQUESTS → TRANSACTIONS → PAYMENTS
PURCHASES → TRANSACTIONS → PAYMENTS
BOOKINGS → TRANSACTIONS → PAYMENTS
```

---

## 12. Entity 작성 기준

| 항목          | 기준                                      |
| ----------- | --------------------------------------- |
| ID 타입       | Long                                    |
| ID 생성 전략    | `GenerationType.SEQUENCE`               |
| Sequence 명명 | `{TABLE_NAME}_SEQ`                      |
| 연관관계        | 기본 `@ManyToOne(fetch = FetchType.LAZY)` |
| 양방향 관계      | 초기 구현에서는 지양                             |
| 긴 본문        | `@Lob`                                  |
| 생성/수정 시간    | `BaseEntity`, `BaseCreatedEntity` 사용    |
| 삭제 시간       | 필요한 Entity에 `DELETED_AT` 직접 선언          |
| 상태값         | Enum 또는 String으로 관리                     |
| 견적 요청 상태    | `RequestStatus` Enum 사용                 |

---

## 13. 로컬 DB 확인 기준

로컬 Oracle Database Free 환경에서 Spring Boot 애플리케이션 실행 후 JPA/Hibernate를 통해 테이블 생성 여부를 확인합니다.

확인 명령어:

```text
./gradlew clean build -x test
./gradlew bootRun
```

IntelliJ Database에서 Oracle 스키마를 새로고침하여 테이블 생성 여부를 확인합니다.

---

## 14. 참고 사항

* 현재 DB Structure는 초기 개발을 위한 기준입니다.
* `MATCHES`는 제거하고 `SERVICE_REQUESTS` 상태값과 `CHAT_ROOMS`로 흐름을 단순화합니다.
* `BOOKINGS`는 견적 요청 흐름이 아니라 마켓 상품 예약 기능에서 사용합니다.
* 실제 기능 구현 과정에서 컬럼, 제약조건, 인덱스는 변경될 수 있습니다.
* 관리자, 결제, 채팅, 파일 업로드 등은 실제 기능 구현 시 세부 정책에 따라 보완이 필요합니다.
* 보안 검증 및 취약점 매핑은 별도 문서에서 다룹니다.

```

참고로 기존 문서에서 `SERVICE_REQUESTS → MATCHES → BOOKINGS` 코드블록을 열고 닫지 않으면 그 아래 표가 전부 코드블록 안으로 먹혀서 “테이블 목록이 사라진 것처럼” 보일 수 있어요. 위 버전처럼 코드블록을 반드시 닫아주세요.
```
