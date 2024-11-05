## 실행방법
2차 구현 (main 브랜치)
docker, docker-compose 설치된 환경에서\
`docker-compose up --build` 

1차 구현 - 스프링 부트 서버 한대 (feature/simple 브랜치)
java 17 설치 \
`./gradlew build` \
`java -jar build/libs/assignment-0.0.1-SNAPSHOT.jar`

## 실제 배포된 서버
Kafdrop http://223.130.160.171:9000/  \
어드민 http://223.130.160.171:8081/  \
고객서비스 http://223.130.160.171:8080/  

## 구현한 부분
구현1,2,3 \
구현4 - 상품 단건 수정 

### 엔티티 객체 관계
<img src="./images/엔티티 다이어그램.png" width="200">

고려한 사항: 요구사항을 보니 Category가 나열 순서가 있는 것으로 보여,\
사내에서 카테고리에 대한 나열 순서를 관리하면 좋을 것 같아 마스터 테이블에 sort order 추가.\
따로 추가한 제약 사항: 각 브랜드는 모든 카테고리에 대해 하나의 상품만 꼭 가져야 함.\
(상품 테이블 - 브랜드&카테고리 유니크 인덱스 추가. [schema.sql](admin/src/main/resources/schema.sql) 참고)

## 구조
### 1차 (feature/simple 브랜치)
<img src="./images/1차 서비스들.png" width="500">

### 2차 (feature/advance, main 브랜치)
<img src="./images/2차 구성.png" width="500">

트래픽 증가할 경우 예상하여 메시지 기반 EDA로 구성.\
레디스 캐싱 추가. \
https://github.com/dibtp1221/assignment/pull/2


## 기타 개발에 고려한 사항
### 캐싱 필요 여부 판단하여 부분 업데이트
#### 전체 (브랜드+모든 카테고리 전부) 캐싱
<img src="./images/캐시전체업데이트 중복 줄이기.png" width="800">
브랜드 단위 수정이 빈번할 경우 DB 부하를 줄 수 있는 전체 조회가 계속 이루어지는데
특히 중복되어 이루어지는 경우를 막고자 함

프로듀서에서는 DB 업데이트 하고 레디스 컨슈머 버전 incr하고
그 값을 카프카에 메시지로 넣음. 

카프카에 프로듀서 버전으로 1 ~ 9가 쌓인 경우\
컨슈머는 1을 처리할 때 레디스에 있는 컨슈머 버전을 읽어서 (없으면 0으로 간주)\
메시지 버전 vs. 컨슈머 버전 비교해서\
컨슈머 버전이 더 작은 경우\
레디스 컨슈머 버전 <- 레디스 프로듀서 버전(여기서는 9)로 업데이트 후 캐시 전체 업데이트 진행.\
그럼 이후 2 ~ 9까지는 컨슈머가 skip하게 됨.

#### 상품 단건 수정된 경우
- 상품 단건 수정 시, 해당 상품이 속한 카테고리에 대한 캐시만 업데이트
- 최저가, 최고가 업데이트 필요한지 여부 판단하여 처리

### 테스트
1차에 fake 사용하여 캐싱 처리 서비스 단위 테스트 \
https://github.com/dibtp1221/assignment/pull/1

## 카프카, Redis 실행 화면
<img src="./images/kafdrop.png" width="500">
<img src="./images/레디스캐시1.png" width="500">
<img src="./images/레디스캐시2.png" width="500">
<img src="./images/레디스캐시3.png" width="500">

## API

구현1 GET http://223.130.160.171:8080/categories/lowest-price-items  \
구현2 GET http://223.130.160.171:8080/brands/lowest-total-price/items  \
구현3 GET http://223.130.160.171:8080/categories/상의/extreme-price-items  \
상품가격변경 \
PATCH http://223.130.160.171:8081/items/price
```json
{
    "category": "상의",
    "brand": "H",
    "price": 14000
}
```
PATCH http://223.130.160.171:8081/{itemId}/price\
```json
{
    "price": 14000
}
```

## 유의점
어드민 서버가 시작하면서 초기 데이터 세팅 (TestDataInit) 하고\
카프카에 메시지 전송하는데 무슨 이유에서인지 원활히 컨슈머에서 처리가 안됨..\

테스트 용도 캐시 전체 업데이트 요청 메시지 발행 API 생성\
POST http://223.130.160.171:8081/items/cache-all-cache

