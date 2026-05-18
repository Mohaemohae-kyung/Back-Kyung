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

## 3. 전체 테이블 목록

현재 Entity 기준으로 생성되는 주요 테이블은 다음과 같습니다.

| 번호 | 테이블명 | 설명 |
|---:|---|---|
| 1 | USERS | 회원 정보 |
| 2 | EXPERT_PROFILES | 고수 프로필 |
| 3 | SERVICE_CATEGORIES | 서비스 카테고리 |
| 4 | LOCATIONS | 지역 정보 |
| 5 | EXPERT_SERVICES | 고수 서비스 게시글 |
| 6 | SERVICE_REQUESTS | 견적 요청 |
| 7 | MATCHES | 요청-고수 매칭 |
| 8 | BOOKINGS | 일정 예약 |
| 9 | TRANSACTIONS | 거래 내역 |
| 10 | PAYMENTS | 결제 내역 |
| 11 | SERVICE_PAY_ACCOUNTS | 숨고페이 계정 |
| 12 | SERVICE_PAY_HISTORIES | 숨고페이 사용/충전 이력 |
| 13 | FAVORITE_EXPERTS | 찜한 고수 |
| 14 | STORE_PRODUCTS | 마켓 상품 |
| 15 | PURCHASES | 마켓 구매 내역 |
| 16 | COMMUNITY_POSTS | 커뮤니티 게시글 |
| 17 | COMMUNITY_COMMENTS | 커뮤니티 댓글 |
| 18 | NOTIFICATIONS | 알림 |
| 19 | FILE_UPLOADS | 파일 업로드 |
| 20 | ADMIN_ACTIONS | 관리자 조치 이력 |
| 21 | NOTICES | 공지사항 |
| 22 | CHAT_ROOMS | 채팅방 |
| 23 | CHAT_MESSAGES | 채팅 메시지 |
| 24 | REVIEWS | 후기 |
| 25 | COUPONS | 쿠폰 |
| 26 | USER_COUPONS | 사용자 보유 쿠폰 |

---

## 4. 도메인별 테이블 분류

### 회원 / 인증

| 테이블명 | 설명 |
|---|---|
| USERS | 일반 사용자, 고수, 관리자 계정을 통합 관리 |

### 고수 / 서비스

| 테이블명 | 설명 |
|---|---|
| EXPERT_PROFILES | 고수 프로필 정보 |
| EXPERT_SERVICES | 고수가 등록한 서비스 게시글 |
| SERVICE_CATEGORIES | 서비스 대분류/소분류 |
| LOCATIONS | 지역 대분류/소분류 |
| FAVORITE_EXPERTS | 사용자가 찜한 고수 정보 |

### 견적 / 매칭 / 예약

| 테이블명 | 설명 |
|---|---|
| SERVICE_REQUESTS | 일반 사용자의 견적 요청 |
| MATCHES | 견적 요청에 대한 고수 매칭 |
| BOOKINGS | 매칭 이후 확정된 일정 예약 |
| REVIEWS | 예약 완료 후 작성하는 후기 |

### 결제 / 거래 / 숨고페이

| 테이블명 | 설명 |
|---|---|
| TRANSACTIONS | 서비스 예약 또는 마켓 구매에 대한 거래 단위 |
| PAYMENTS | 실제 결제 시도 및 결제 결과 |
| SERVICE_PAY_ACCOUNTS | 사용자별 숨고페이 계정 |
| SERVICE_PAY_HISTORIES | 숨고페이 충전, 사용, 환불 이력 |
| COUPONS | 쿠폰 정책 |
| USER_COUPONS | 사용자에게 발급된 쿠폰 |

### 마켓 / 구매

| 테이블명 | 설명 |
|---|---|
| STORE_PRODUCTS | 마켓 상품 |
| PURCHASES | 마켓 상품 구매 내역 |

### 커뮤니티

| 테이블명 | 설명 |
|---|---|
| COMMUNITY_POSTS | 커뮤니티 게시글 |
| COMMUNITY_COMMENTS | 커뮤니티 댓글 및 대댓글 |

### 채팅 / 알림

| 테이블명 | 설명 |
|---|---|
| CHAT_ROOMS | 사용자와 고수 간 채팅방 |
| CHAT_MESSAGES | 채팅 메시지 |
| NOTIFICATIONS | 사용자 알림 |

### 파일 / 관리자 / 공지

| 테이블명 | 설명 |
|---|---|
| FILE_UPLOADS | 프로필, 게시글, 채팅 등에 사용되는 파일 |
| ADMIN_ACTIONS | 관리자 조치 이력 |
| NOTICES | 공지사항 |

---

## 5. 핵심 서비스 흐름별 테이블 연결

```text
회원가입/로그인:
USERS

고수 등록:
USERS → EXPERT_PROFILES → EXPERT_SERVICES

고수 검색:
SERVICE_CATEGORIES + LOCATIONS + EXPERT_PROFILES + EXPERT_SERVICES + REVIEWS + FAVORITE_EXPERTS

견적/매칭/예약:
USERS → SERVICE_REQUESTS → MATCHES → BOOKINGS

결제/거래:
BOOKINGS → TRANSACTIONS → PAYMENTS

숨고페이:
USERS → SERVICE_PAY_ACCOUNTS → SERVICE_PAY_HISTORIES

마켓/구매:
STORE_PRODUCTS → PURCHASES → TRANSACTIONS → PAYMENTS

커뮤니티:
USERS → COMMUNITY_POSTS → COMMUNITY_COMMENTS

채팅:
USERS + EXPERT_PROFILES + MATCHES → CHAT_ROOMS → CHAT_MESSAGES

후기:
BOOKINGS → REVIEWS

쿠폰:
COUPONS → USER_COUPONS → PAYMENTS

알림:
USERS → NOTIFICATIONS

관리자:
USERS → ADMIN_ACTIONS
USERS → NOTICES