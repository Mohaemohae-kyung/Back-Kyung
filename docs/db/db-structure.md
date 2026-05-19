맞아. `PURCHASES`를 제거하면 `TRANSACTIONS`에서는 `Purchase` import, `purchase` 필드, `UK_TRANSACTIONS_PURCHASE` 제약조건, `PURCHASE_ID` 관계를 전부 제거하면 됩니다. 현재 `db-structure.md`에도 `PURCHASES`, “마켓 상품 구매”, `STORE_PRODUCTS → PURCHASES → TRANSACTIONS → PAYMENTS` 흐름이 남아 있어서 같이 정리해야 합니다.

## 1. Transaction.java 수정본

```java
package kyung.kung_backend.domain.transaction.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "TRANSACTIONS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TRANSACTIONS_REQUEST", columnNames = "REQUEST_ID"),
                @UniqueConstraint(name = "UK_TRANSACTIONS_BOOKING", columnNames = "BOOKING_ID")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "TRANSACTIONS_SEQ_GENERATOR",
        sequenceName = "TRANSACTIONS_SEQ",
        allocationSize = 1
)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRANSACTIONS_SEQ_GENERATOR")
    @Column(name = "TRANSACTION_ID", nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", unique = true)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BUYER_ID", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SELLER_ID")
    private User seller;

    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 30)
    private String transactionType;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "DISCOUNT_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "FINAL_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
}
```

`TRANSACTION_TYPE`은 이제 아래 2개만 기준으로 잡으면 됩니다.

```text
SERVICE_REQUEST
BOOKING
```

---

## 2. db-structure.md 수정본

````md
# DB Structure 초안

## 1. 목적

본 문서는 숨고 유사 전문가 매칭 서비스의 DB Structure 초안을 정리하기 위한 문서입니다.

팀원들이 기능 개발을 시작하기 전에 공통으로 참고할 수 있도록 주요 테이블 목록, 도메인별 분류, 핵심 관계를 정리합니다.

현재 DB 설계는 Oracle DB 기준이며, Spring Boot JPA Entity 구조와 연결됩니다.

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

---

## 3. 서비스 흐름 요약

기존에는 견적 요청 이후 `MATCHES` 테이블을 통해 매칭 제안을 관리하는 구조를 고려했습니다.

```text
SERVICE_REQUESTS → MATCHES → BOOKINGS
````

현재는 서비스 흐름을 단순화하여 `MATCHES` 테이블을 제거하고, 견적 요청 상태값과 채팅방 생성 흐름으로 처리합니다.

```text
사용자 견적 요청 생성
→ 고수가 견적 요청 확인
→ 고수가 승인하면 SERVICE_REQUESTS 상태를 CHATTING으로 변경
→ CHAT_ROOMS 생성
→ 채팅에서 일정/금액/진행 방식 협의
→ 결제 또는 거래 진행
→ 완료 시 SERVICE_REQUESTS 상태를 COMPLETED로 변경
```

견적 요청 기반 결제는 `TRANSACTIONS`를 통해 처리합니다.

```text
SERVICE_REQUESTS → TRANSACTIONS → PAYMENTS
```

마켓 상품은 일반 물품 구매가 아니라 예약형 상품으로 관리합니다.

```text
마켓 상품 예약:
STORE_PRODUCTS → BOOKINGS → TRANSACTIONS → PAYMENTS
```

---

## 4. 전체 테이블 목록

| 번호 | 테이블명                  | 설명                     |
| -: | --------------------- | ---------------------- |
|  1 | USERS                 | 회원 정보                  |
|  2 | EXPERT_PROFILES       | 고수 프로필                 |
|  3 | SERVICE_CATEGORIES    | 서비스 카테고리               |
|  4 | LOCATIONS             | 지역 정보                  |
|  5 | EXPERT_SERVICES       | 고수 프로필과 서비스 카테고리 연결 정보 |
|  6 | SERVICE_REQUESTS      | 사용자의 견적 요청             |
|  7 | CHAT_ROOMS            | 채팅방                    |
|  8 | CHAT_MESSAGES         | 채팅 메시지                 |
|  9 | STORE_PRODUCTS        | 고수가 등록한 예약형 마켓 상품      |
| 10 | BOOKINGS              | 마켓 상품 예약               |
| 11 | TRANSACTIONS          | 거래 내역                  |
| 12 | PAYMENTS              | 결제 내역                  |
| 13 | SERVICE_PAY_ACCOUNTS  | 숨고페이 계정                |
| 14 | SERVICE_PAY_HISTORIES | 숨고페이 사용/충전 이력          |
| 15 | FAVORITE_EXPERTS      | 찜한 고수                  |
| 16 | COMMUNITY_POSTS       | 커뮤니티 게시글               |
| 17 | COMMUNITY_COMMENTS    | 커뮤니티 댓글                |
| 18 | NOTIFICATIONS         | 알림                     |
| 19 | FILE_UPLOADS          | 파일 업로드                 |
| 20 | ADMIN_ACTIONS         | 관리자 조치 이력              |
| 21 | NOTICES               | 공지사항                   |
| 22 | REVIEWS               | 후기                     |
| 23 | COUPONS               | 쿠폰                     |
| 24 | USER_COUPONS          | 사용자 보유 쿠폰              |

> `MATCHES` 테이블은 제거합니다.
> `PURCHASES` 테이블은 제거합니다.
> 마켓 상품은 구매형 상품이 아니라 예약형 상품으로 관리합니다.

---

## 5. 도메인별 테이블 분류

### 회원 / 인증

| 테이블명  | 설명                    |
| ----- | --------------------- |
| USERS | 일반 사용자, 고수, 관리자 계정 관리 |

### 고수 / 서비스

| 테이블명               | 설명                        |
| ------------------ | ------------------------- |
| EXPERT_PROFILES    | 고수 프로필 정보                 |
| EXPERT_SERVICES    | 고수가 제공 가능한 서비스 카테고리 연결 정보 |
| SERVICE_CATEGORIES | 서비스 카테고리                  |
| LOCATIONS          | 고수 대표 활동 지역 또는 사용자 검색 지역  |
| FAVORITE_EXPERTS   | 사용자가 찜한 고수 정보             |

### 견적 요청 / 채팅

| 테이블명             | 설명                  |
| ---------------- | ------------------- |
| SERVICE_REQUESTS | 일반 사용자의 견적 요청       |
| CHAT_ROOMS       | 견적 요청 승인 후 생성되는 채팅방 |
| CHAT_MESSAGES    | 채팅방 내 메시지           |

### 마켓 / 예약

| 테이블명           | 설명                |
| -------------- | ----------------- |
| STORE_PRODUCTS | 고수가 등록한 예약형 마켓 상품 |
| BOOKINGS       | 마켓 상품 예약          |

### 거래 / 결제

| 테이블명                  | 설명                       |
| --------------------- | ------------------------ |
| TRANSACTIONS          | 견적 요청 또는 마켓 예약에 대한 거래 단위 |
| PAYMENTS              | 실제 결제 시도 및 결과            |
| SERVICE_PAY_ACCOUNTS  | 사용자별 숨고페이 계정             |
| SERVICE_PAY_HISTORIES | 숨고페이 충전, 사용, 환불 이력       |
| COUPONS               | 쿠폰 정책                    |
| USER_COUPONS          | 사용자에게 발급된 쿠폰             |

### 커뮤니티 / 알림 / 공통

| 테이블명               | 설명        |
| ------------------ | --------- |
| COMMUNITY_POSTS    | 커뮤니티 게시글  |
| COMMUNITY_COMMENTS | 커뮤니티 댓글   |
| NOTIFICATIONS      | 사용자 알림    |
| FILE_UPLOADS       | 파일 업로드    |
| ADMIN_ACTIONS      | 관리자 조치 이력 |
| NOTICES            | 공지사항      |
| REVIEWS            | 후기        |

---

## 6. 주요 테이블 역할

### EXPERT_SERVICES

`EXPERT_SERVICES`는 고수가 직접 작성한 서비스 게시글이 아니라, **고수 프로필과 서비스 카테고리를 연결하는 테이블**입니다.

```text
EXPERT_PROFILES ↔ SERVICE_CATEGORIES
```

예시:

```text
고수 A → 자소서 첨삭 제공 가능
고수 A → 면접 코칭 제공 가능
고수 B → 보컬 레슨 제공 가능
```

기준 구조는 다음과 같습니다.

```text
EXPERT_SERVICE_ID
EXPERT_PROFILE_ID
CATEGORY_ID
STATUS
DELETED_AT
```

가격, 제목, 설명이 필요한 상품성 데이터는 `STORE_PRODUCTS`에서 관리합니다.

---

### STORE_PRODUCTS

`STORE_PRODUCTS`는 고수가 등록한 예약형 마켓 상품을 관리합니다.

```text
EXPERT_PROFILES → STORE_PRODUCTS
```

마켓 상품은 가격, 설명, 재고, 상태를 가질 수 있습니다.

```text
STORE_PRODUCT_ID
EXPERT_PROFILE_ID
CATEGORY_ID
TITLE
DESCRIPTION
PRICE
STOCK_QUANTITY
STATUS
DELETED_AT
```

---

### BOOKINGS

`BOOKINGS`는 마켓 상품 예약에 사용합니다.

견적 요청 흐름에서는 `BOOKINGS`를 사용하지 않습니다.

```text
STORE_PRODUCTS → BOOKINGS
```

고수 정보는 `BOOKINGS`에 직접 저장하지 않고, `STORE_PRODUCTS`를 통해 조회합니다.

```text
BOOKINGS → STORE_PRODUCTS → EXPERT_PROFILES
```

예약 상품 흐름은 다음과 같습니다.

```text
상품 선택
→ 날짜/시간 선택
→ 예약 생성
→ 결제 완료
→ 예약 시점에 서비스 이용
→ 완료 또는 취소/환불
```

---

### TRANSACTIONS

`TRANSACTIONS`는 전체 거래 단위입니다.

견적 요청 거래와 마켓 예약 거래를 표현합니다.

```text
SERVICE_REQUESTS → TRANSACTIONS
BOOKINGS → TRANSACTIONS
```

거래 유형은 `TRANSACTION_TYPE`으로 구분합니다.

```text
SERVICE_REQUEST
BOOKING
```

---

### PAYMENTS

`PAYMENTS`는 실제 결제 시도와 결과를 관리합니다.

`PAYMENTS`는 견적 요청 또는 예약을 직접 참조하지 않고 항상 `TRANSACTIONS`를 통해 연결합니다.

```text
TRANSACTIONS → PAYMENTS
```

---

## 7. SERVICE_REQUESTS 상태값

`SERVICE_REQUESTS`는 견적 요청의 전체 흐름을 상태값으로 관리합니다.

| 상태값       | 설명                                  |
| --------- | ----------------------------------- |
| PENDING   | 사용자가 견적 요청을 생성했고, 아직 고수가 응답하지 않은 상태 |
| CHATTING  | 고수가 요청을 승인했고, 채팅방이 생성되어 협의 중인 상태    |
| COMPLETED | 채팅/결제/거래가 최종 완료된 상태                 |
| REJECTED  | 고수가 견적 요청을 거절한 상태                   |
| CANCELLED | 사용자가 요청을 취소한 상태                     |
| DELETED   | 소프트 삭제 처리된 상태                       |

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

## 8. 핵심 서비스 흐름별 테이블 연결

```text
회원가입/로그인:
USERS

고수 등록:
USERS → EXPERT_PROFILES

고수 제공 서비스 등록:
EXPERT_PROFILES → EXPERT_SERVICES → SERVICE_CATEGORIES

고수 검색:
SERVICE_CATEGORIES + LOCATIONS + EXPERT_PROFILES + EXPERT_SERVICES

견적 요청:
USERS → SERVICE_REQUESTS
EXPERT_SERVICES → SERVICE_REQUESTS

견적 승인/채팅:
SERVICE_REQUESTS → CHAT_ROOMS → CHAT_MESSAGES

견적 기반 거래/결제:
SERVICE_REQUESTS → TRANSACTIONS → PAYMENTS

마켓 상품 등록:
EXPERT_PROFILES → STORE_PRODUCTS

마켓 상품 예약:
STORE_PRODUCTS → BOOKINGS → TRANSACTIONS → PAYMENTS

숨고페이:
USERS → SERVICE_PAY_ACCOUNTS → SERVICE_PAY_HISTORIES

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

## 9. 주요 관계 요약

| 관계                                             | 설명                                |
| ---------------------------------------------- | --------------------------------- |
| USERS 1:1 EXPERT_PROFILES                      | 고수 회원은 고수 프로필을 가질 수 있음            |
| EXPERT_PROFILES 1:N EXPERT_SERVICES            | 고수는 여러 서비스 카테고리를 제공할 수 있음         |
| SERVICE_CATEGORIES 1:N EXPERT_SERVICES         | 하나의 카테고리에 여러 고수 제공 서비스가 연결될 수 있음  |
| EXPERT_PROFILES 1:N STORE_PRODUCTS             | 고수는 여러 예약형 마켓 상품을 등록할 수 있음        |
| STORE_PRODUCTS 1:N BOOKINGS                    | 마켓 상품은 여러 예약을 가질 수 있음             |
| USERS 1:N SERVICE_REQUESTS                     | 사용자는 여러 견적 요청을 작성할 수 있음           |
| EXPERT_SERVICES 1:N SERVICE_REQUESTS           | 특정 고수 제공 서비스를 선택해 견적 요청을 생성할 수 있음 |
| SERVICE_REQUESTS 1:1 CHAT_ROOMS                | 고수가 요청을 승인하면 채팅방이 생성됨             |
| CHAT_ROOMS 1:N CHAT_MESSAGES                   | 채팅방은 여러 메시지를 가짐                   |
| SERVICE_REQUESTS 1:1 TRANSACTIONS              | 견적 요청 기반 거래가 생성될 수 있음             |
| BOOKINGS 1:1 TRANSACTIONS                      | 마켓 예약은 거래로 연결될 수 있음               |
| TRANSACTIONS 1:N PAYMENTS                      | 하나의 거래에 여러 결제 시도가 있을 수 있음         |
| USERS 1:1 SERVICE_PAY_ACCOUNTS                 | 사용자는 숨고페이 계정을 가질 수 있음             |
| SERVICE_PAY_ACCOUNTS 1:N SERVICE_PAY_HISTORIES | 숨고페이 계정은 여러 이력을 가짐                |
| USERS N:M EXPERT_PROFILES                      | FAVORITE_EXPERTS로 찜 관계 표현         |
| COMMUNITY_POSTS 1:N COMMUNITY_COMMENTS         | 게시글은 여러 댓글을 가짐                    |
| COUPONS 1:N USER_COUPONS                       | 쿠폰은 여러 사용자에게 발급될 수 있음             |

---

## 10. Entity 수정 기준

이번 구조 정리에서 수정하는 핵심 Entity는 다음과 같습니다.

| Entity        | 수정 방향                  |
| ------------- | ---------------------- |
| ExpertService | 고수-서비스 카테고리 연결 구조로 단순화 |
| StoreProduct  | 고수가 등록한 예약형 마켓 상품으로 정리 |
| Booking       | 마켓 상품 예약 중심으로 정리       |
| Transaction   | 견적 요청/예약 거래를 표현하도록 정리  |

현재 구조 유지 또는 역할만 명확히 하는 Entity는 다음과 같습니다.

| Entity         | 방향                           |
| -------------- | ---------------------------- |
| Payment        | Transaction 기준 결제 구조 유지      |
| ChatRoom       | ServiceRequest 기준 연결 유지      |
| ServiceRequest | ExpertService 기준 견적 요청 구조 유지 |

---

## 11. 참고 사항

* `MATCHES`는 제거하고 `SERVICE_REQUESTS` 상태값과 `CHAT_ROOMS`로 흐름을 단순화합니다.
* `PURCHASES`는 제거합니다.
* `EXPERT_SERVICES`는 고수 프로필과 서비스 카테고리 연결 정보로 사용합니다.
* `STORE_PRODUCTS`는 고수가 등록한 예약형 마켓 상품으로 사용합니다.
* `BOOKINGS`는 마켓 상품 예약 기능에서 사용합니다.
* 견적 요청 기반 결제는 `TRANSACTIONS`를 통해 처리합니다.
* 마켓 상품 예약 결제는 `BOOKINGS → TRANSACTIONS → PAYMENTS`로 처리합니다.
* `PAYMENTS`는 항상 `TRANSACTIONS`를 기준으로 연결합니다.
* 실제 기능 구현 과정에서 컬럼, 제약조건, 인덱스는 변경될 수 있습니다.
* 기존 로컬 DB에 생성된 컬럼은 `ddl-auto: update`만으로 삭제되지 않을 수 있으므로, 필요 시 로컬 테이블 재생성 또는 수동 컬럼 정리가 필요합니다.
