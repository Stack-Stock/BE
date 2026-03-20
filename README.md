### 3️⃣ 주식 시장 시뮬레이션

#### 구조

- 30개 종목 (10개 산업 × 3개 기업)
- 뉴스 기반 이벤트가 주가 변동 트리거

#### 동작 방식

```text
GameCase 조회
 → up_down_json 파싱
 → 종목별 변동률 적용
 → 다음날 가격 계산
```

#### 특징

- 뉴스 연관 종목 → 큰 변동
- 비연관 종목 → 랜덤 변동

---

### 4️⃣ 자산 및 투자 로직

#### 자산 구성

- 현금 (즉시 사용 가능)
- 주식 (3일 후 현금화)

#### 처리 로직

- 매수 → 즉시 반영
- 매도 → 3일 후 반영 (스케줄링)
- 하루 종료 시 평가금액 계산

---

### 5️⃣ 행동력(AP) 시스템

- 기본 AP: 2
- 행동별 소모:
  - 투자: 1
  - 공부: 1
  - TV: 1
  - 신문: 2
  - 휴대폰: 0

- 공부 3회 → 번뜩임 +1 (AP 증가)

---

### 6️⃣ 이벤트 시스템

#### 이벤트 종류

- 플러스 이벤트
- 마이너스 이벤트
- 랜덤 이벤트

#### 특징

- 확률 기반 또는 날짜 기반 발생
- 중복 방지 (flag 관리)

---

## 🧠 시나리오 & 뉴스 연동 구조

### 데이터 흐름

```text
뉴스 데이터
 → 임베딩 (Python 서버)
 → Claude API 가공
 → Economic Event 생성
 → DB 저장
 → 게임에서 호출
```

### Scenario 구조 예시

```json
{
  "event_id": "EV-202512-045",
  "target_sector": "IT",
  "impact_rate": 0.075,
  "direction": "UP"
}
```

---

## 🗂 주요 도메인 구조

### Core Entities

- `User`
- `GameRun`
- `ScenarioDay`
- `GameCase`
- `Stock`
- `StockPrice`
- `Portfolio`
- `Transaction`
- `Event`
- `Snapshot`

---

## ⚙️ 기술 스택

### Backend

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security (Session 기반)

### Database

- PostgreSQL

### AI 연동

- Python (FastAPI)
- Claude API
- Vector DB

### Infra

- Jenkins (CI/CD)

---

## 🔁 핵심 처리 로직

### 1. 게임 시작 시

```text
GameRun 생성
 → 80일 ScenarioDay 생성
 → 초기 주가 데이터 생성
```

---

### 2. 하루 종료 시

```text
GameCase 적용
 → 종목별 변동률 계산
 → 다음날 주가 생성
 → 자산 평가
```

---

### 3. 매도 처리 (지연 로직)

```text
매도 요청
 → Pending 상태 저장
 → 3일 후 스케줄러 실행
 → 현금 반영
```

---


## 🧪 향후 개선 방향

### 1. 이벤트 기반 아키텍처

- Kafka 도입 가능
- 예:
  - "day_finished"
  - "stock_updated"

---

### 2. 분산 처리

- Spark 활용
- 뉴스 클러스터링 / 유사도 계산

---

### 3. AI 고도화

- 개인 맞춤 시나리오 추천
- 플레이 스타일 분석

---

## 🎮 게임 목표

- 80일 내 500만원 달성
- 다양한 엔딩 분기 존재

---

## 👥 팀

| 역할 | 담당 |
|------|------|
| Backend | 게임 로직, 상태 관리 | 이혜림 |
| AI | 뉴스 임베딩, 시나리오 생성 | 강진석, 김서형 |
| Frontend | UI 및 인터랙션 | 조재봉 |

---

## 💡 핵심 가치

- 뉴스 → 행동으로 연결
- 실패 없는 경제 학습
- 데이터 기반 의사결정 경험

