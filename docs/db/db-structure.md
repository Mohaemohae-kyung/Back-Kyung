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