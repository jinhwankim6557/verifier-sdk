# Verifier SDK Guide

**Version**: 1.0.0
**Last Updated**: 2026-01-26
**Status**: Production Ready ✅

---

## 📋 목차

1. [개요](#개요)
2. [SDK 아키텍처](#sdk-아키텍처)
3. [API 인터페이스](#api-인터페이스)
4. [서비스 계층](#서비스-계층)
5. [데이터 플로우](#데이터-플로우)
6. [DTO 구조](#dto-구조)
7. [사용 예제](#사용-예제)
8. [Phase 2 수정사항](#phase-2-수정사항)

---

## 개요

### SDK란?

Verifier SDK는 **VP(Verifiable Presentation) 검증 프로토콜을 추상화한 재사용 가능한 라이브러리**입니다.

### 주요 목적

- ✅ VP 검증 비즈니스 로직을 Application으로부터 분리
- ✅ 다양한 Application에서 동일한 검증 프로토콜 재사용
- ✅ 인터페이스 기반 설계로 유연한 구현 가능
- ✅ 독립적인 JAR 배포 및 버전 관리

### 핵심 기능

1. **VP Offer 생성** - QR 코드용 Offer Payload 생성
2. **Verify Profile 생성** - VP 요청을 위한 Profile 생성
3. **VP 검증** - E2E 복호화, 서명 검증, VC 검증
4. **검증 확인** - 클레임 추출 및 결과 반환

---

## SDK 아키텍처

### 전체 구조

```mermaid
graph TB
    subgraph "SDK Layer"
        API[API Interfaces<br/>7개 인터페이스]
        Service[Service Layer<br/>5개 서비스]
        DTO[DTO Layer<br/>요청/응답 객체]
        Exception[Exception Layer<br/>커스텀 예외]
    end

    subgraph "Application Layer"
        Adapter[Adapter 구현<br/>7개 Adapter]
        AppService[Application Service]
        DB[(Database)]
    end

    AppService --> Adapter
    Adapter -.implements.-> API
    API --> Service
    Service --> DTO
    Adapter --> DB

    style API fill:#e1f5ff
    style Service fill:#fff4e1
    style Adapter fill:#e8f5e9
```

### 계층별 역할

| 계층 | 역할 | 파일 위치 |
|------|------|----------|
| **API** | 인터페이스 정의 | `src/main/java/org/omnione/did/verifier/v1/api/` |
| **Service** | 비즈니스 로직 구현 | `src/main/java/org/omnione/did/verifier/v1/service/` |
| **DTO** | 데이터 전송 객체 | `src/main/java/org/omnione/did/verifier/v1/dto/` |
| **Exception** | 예외 정의 | `src/main/java/org/omnione/did/verifier/v1/exception/` |

---

## API 인터페이스

### 7개 핵심 인터페이스

```mermaid
classDiagram
    class VerificationConfigProvider {
        +getPolicy(policyId) VerificationPolicy
        +existsPolicy(policyId) boolean
    }

    class TransactionManager {
        +createTransactionId() String
        +getTransactionId(txId) Long
        +saveTransactionState(txId, state)
        +getTransactionState(txId) String
    }

    class E2eSessionProvider {
        +createSession(txId) ReqE2e
        +getSession(txId) ReqE2e
        +decrypt(txId, encHolderPubKey, encVp, iv) String
        +removeSession(txId)
    }

    class VerifierInfoProvider {
        +getVerifierInfo() ProviderDetail
        +getVerifierDidDocument() String
        +getVerifierDid() String
    }

    class StorageService {
        +findDidDocument(did) String
        +getVcMeta(vcId) String
        +existsDidDocument(did) boolean
    }

    class CryptoHelper {
        +verifySignature(pubKey, sig, data) boolean
        +sha256(data) String
        +decodeMultibase(multibase) byte[]
    }

    class NonceGenerator {
        +generateNonce() String
        +generateNonce(length) String
    }

    VerificationConfigProvider --> VerificationPolicy
    E2eSessionProvider --> ReqE2e
    VerifierInfoProvider --> ProviderDetail
```

### 인터페이스 상세

#### 1. VerificationConfigProvider

**목적**: VP 검증 정책 제공

```java
public interface VerificationConfigProvider {
    // Policy 조회
    VerificationPolicy getPolicy(String policyId);

    // Policy 존재 여부 확인
    boolean existsPolicy(String policyId);
}
```

**구현 요구사항**:
- Policy 데이터를 DB 또는 파일에서 조회
- Filter, Process, Endpoint 정보 포함

---

#### 2. TransactionManager

**목적**: Transaction 생명주기 관리

```java
public interface TransactionManager {
    // Transaction ID 생성 (UUID)
    String createTransactionId();

    // Transaction ID → Long PK 변환
    Long getTransactionId(String txId);

    // 상태 저장 (DEPRECATED - Phase 2-2 수정)
    @Deprecated
    void saveTransactionState(String txId, String state);

    // 상태 조회 (DEPRECATED - Phase 2-2 수정)
    @Deprecated
    String getTransactionState(String txId);
}
```

**⚠️ Phase 2-2 수정사항**:
- `saveTransactionState`, `getTransactionState`는 **더 이상 사용되지 않음**
- 메모리 누수 방지를 위해 비활성화됨
- Application에서 DB를 통해 직접 상태 관리

---

#### 3. E2eSessionProvider

**목적**: E2E 암호화 세션 관리

```java
public interface E2eSessionProvider {
    // E2E 세션 생성 (키쌍 + nonce)
    ReqE2e createSession(String txId);

    // E2E 세션 조회
    ReqE2e getSession(String txId);

    // VP 복호화 (Phase 2-1 수정: ECDH 프로토콜 구현)
    String decrypt(String txId, String encHolderPublicKey,
                   String encVp, String iv);

    // E2E 세션 삭제
    void removeSession(String txId);
}
```

**🔴 Phase 2-1 Critical 수정**:
- `decrypt()` 메서드 시그니처 변경 (4개 매개변수)
- ECDH 프로토콜 완전 구현:
  1. `encHolderPublicKey` 복호화 → Holder 공개키 추출
  2. ECDH: Verifier 개인키 + Holder 공개키 → 공유 비밀키
  3. 세션키 = KDF(공유 비밀키 + nonce)
  4. VP 복호화

---

#### 4. VerifierInfoProvider

**목적**: Verifier 정보 제공

```java
public interface VerifierInfoProvider {
    // Verifier 기본 정보
    ProviderDetail getVerifierInfo();

    // Verifier DID Document (JSON)
    String getVerifierDidDocument();

    // Verifier DID
    String getVerifierDid();
}
```

---

#### 5. StorageService

**목적**: DID Document 및 VC Meta 조회

```java
public interface StorageService {
    // DID Document 조회 (JSON)
    String findDidDocument(String did);

    // VC Meta 조회 (JSON)
    String getVcMeta(String vcId);

    // DID Document 존재 여부
    boolean existsDidDocument(String did);
}
```

---

#### 6. CryptoHelper

**목적**: 암호화 유틸리티

```java
public interface CryptoHelper {
    // 서명 검증
    boolean verifySignature(String publicKey, String signature, byte[] data);

    // SHA-256 해시
    String sha256(byte[] data);

    // Multibase 디코딩
    byte[] decodeMultibase(String multibase);
}
```

---

#### 7. NonceGenerator

**목적**: Nonce 생성

```java
public interface NonceGenerator {
    // 기본 nonce 생성 (16 bytes)
    String generateNonce();

    // 커스텀 길이 nonce 생성
    String generateNonce(int length);
}
```

---

## 서비스 계층

### 5개 핵심 서비스

```mermaid
graph LR
    subgraph "VerifierService (Facade)"
        VS[VerifierService]
    end

    subgraph "Service Layer"
        VOS[VpOfferService]
        VPS[VpProfileService]
        VVS[VpVerificationService]
        VCS[VerificationConfirmService]
    end

    VS --> VOS
    VS --> VPS
    VS --> VVS
    VS --> VCS

    style VS fill:#ffe1e1
    style VOS fill:#e1f5ff
    style VPS fill:#e1f5ff
    style VVS fill:#e1f5ff
    style VCS fill:#e1f5ff
```

### 1. VerifierService (Facade)

**역할**: SDK 진입점, 다른 서비스들을 조합

```java
public class VerifierService {
    // VP Offer 생성
    public VpOfferPayload createVpOfferPayload(
        String policyId, String device, String service, boolean locked);

    // Verify Profile 생성
    public VerificationProfile createVerifyProfile(
        String policyId, String profileId, ReqE2e reqE2e);

    // VP 검증
    public String verifyPresentation(VpVerificationRequest request);

    // 검증 확인
    public VerificationConfirmResult confirmVerification(
        String txId, String vpJson, boolean verified);
}
```

---

### 2. VpOfferService

**책임**: VP Offer Payload 생성

```java
public VpOfferPayload createVpOfferPayload(
    String policyId,
    String device,
    String service,
    boolean locked
) {
    // 1. Policy 조회
    VerificationPolicy policy = configProvider.getPolicy(policyId);

    // 2. Offer ID 생성
    String offerId = transactionManager.createTransactionId();

    // 3. ValidUntil 계산
    Instant validUntil = Instant.now()
        .plus(policy.getValidityDuration(), ChronoUnit.SECONDS);

    // 4. Payload 반환
    return VpOfferPayload.builder()
        .offerId(offerId)
        .type("VerifyOffer")
        .mode(policy.getMode())
        .endpoints(policy.getEndpoints())
        .validUntil(validUntil)
        .build();
}
```

---

### 3. VpProfileService

**책임**: Verify Profile 생성

```java
public VerificationProfile createVerifyProfile(
    String policyId,
    String profileId,
    ReqE2e reqE2e
) {
    // 1. Policy 조회
    VerificationPolicy policy = configProvider.getPolicy(policyId);

    // 2. Verifier 정보 조회
    ProviderDetail verifierInfo = verifierInfoProvider.getVerifierInfo();

    // 3. Verifier Nonce 생성
    String verifierNonce = nonceGenerator.generateNonce();

    // 4. Profile 생성 (Proof 제외, Application에서 서명)
    return VerificationProfile.builder()
        .id(profileId)
        .type("VerifyProfile")
        .profile(ProfileContent.builder()
            .verifier(verifierInfo)
            .filter(policy.getFilter())
            .process(policy.getProcess().toBuilder()
                .reqE2e(reqE2e)
                .verifierNonce(verifierNonce)
                .build())
            .build())
        .build();
}
```

---

### 4. VpVerificationService

**책임**: VP 검증 (복호화, 서명, VC 상태)

```java
public String verifyPresentation(VpVerificationRequest request) {
    // 1. VP 복호화 (Phase 2-1 수정)
    String vpJson = decryptVp(
        request.getTxId(),
        request.getEncHolderPublicKey(),  // ← Phase 2-1: 새로 추가
        request.getEncVp(),
        request.getIv()
    );

    // 2. VP 파싱
    JsonObject vpObject = gson.fromJson(vpJson, JsonObject.class);

    // 3. AuthType 검증
    validateAuthType(vpObject, request.getRequiredAuthType());

    // 4. Nonce 검증
    validateNonce(vpObject, request.getVerifierNonce());

    // 5. VP 서명 검증
    validateVpSignature(vpObject);

    // 6. VC 서명 검증
    validateVcSignatures(vpObject);

    // 7. VC 상태 검증
    validateVcStatuses(vpObject);

    return vpJson;
}
```

**🔴 Phase 2-1 Critical 수정**:
- `decryptVp()` 매개변수 추가: `encHolderPublicKey`
- ECDH 프로토콜 4단계 구현
- E2E 복호화 완전 수정

---

### 5. VerificationConfirmService

**책임**: 클레임 추출 및 결과 반환

```java
public VerificationConfirmResult confirmVerification(
    String txId,
    String vpJson,
    boolean verified
) {
    if (!verified) {
        return VerificationConfirmResult.builder()
            .txId(txId)
            .verified(false)
            .errorMessage("VP verification failed")
            .build();
    }

    JsonObject vpObject = gson.fromJson(vpJson, JsonObject.class);

    // 1. Holder DID 추출
    String holderDid = extractHolderDid(vpObject);

    // 2. VC 목록 추출 (Phase 2-4 수정)
    Map<String, Object> submittedVcs = extractSubmittedVcsAsMap(vpObject);

    // 3. 클레임 추출
    Map<String, Object> extractedClaims = extractClaims(vpObject);

    return VerificationConfirmResult.builder()
        .txId(txId)
        .verified(true)
        .holderDid(holderDid)
        .submittedVcs(submittedVcs)  // ← Phase 2-4: Map으로 수정
        .extractedClaims(extractedClaims)
        .build();
}
```

**🔴 Phase 2-4 Critical 수정**:
- `extractSubmittedVcs()`: `List<String>` 반환
- `extractSubmittedVcsAsMap()`: `Map<String, Object>` 반환 (신규)
- 타입 캐스팅 오류 수정

---

## 데이터 플로우

### VP 검증 전체 시퀀스

```mermaid
sequenceDiagram
    participant H as Holder (모바일)
    participant A as Application
    participant S as SDK
    participant BC as Blockchain
    participant DB as Database

    rect rgb(230, 240, 255)
        Note over H,DB: 1. VP Offer 생성
        A->>S: createVpOfferPayload(policyId)
        S->>A: getPolicy(policyId)
        A-->>S: VerificationPolicy
        S->>A: createTransactionId()
        A-->>S: offerId
        S-->>A: VpOfferPayload
        A->>DB: VpOffer 저장
    end

    rect rgb(240, 255, 230)
        Note over H,DB: 2. Profile 생성
        H->>A: QR 스캔, Profile 요청
        A->>S: createSession(txId)
        S->>A: E2E 세션 생성
        A-->>S: ReqE2e (publicKey, nonce, cipher)
        A->>DB: E2e 저장
        A->>S: createVerifyProfile(policyId, profileId, reqE2e)
        S->>A: getPolicy(policyId)
        A-->>S: VerificationPolicy
        S->>A: getVerifierInfo()
        A-->>S: ProviderDetail
        S-->>A: VerificationProfile (Proof 제외)
        A->>A: Proof 서명 생성
        A->>DB: VpProfile 저장
        A-->>H: VerificationProfile
    end

    rect rgb(255, 240, 230)
        Note over H,DB: 3. VP 검증
        H->>H: VP 생성 및 E2E 암호화
        H->>A: VP 제출 (encVp, encHolderPublicKey, iv)
        A->>S: verifyPresentation(VpVerificationRequest)

        S->>A: decrypt(txId, encHolderPubKey, encVp, iv)
        Note over A,S: Phase 2-1: ECDH 프로토콜
        A->>DB: E2e 세션 조회
        A->>A: 1. encHolderPubKey 디코딩<br/>2. ECDH 공유 비밀키 생성<br/>3. 세션키 생성<br/>4. VP 복호화
        A-->>S: vpJson (평문)

        S->>S: VP 파싱 및 검증
        S->>A: findDidDocument(holderDid)
        A->>BC: DID Document 조회
        BC-->>A: DID Document
        A-->>S: DID Document (JSON)
        S->>S: VP 서명 검증

        loop 각 VC마다
            S->>A: findDidDocument(issuerDid)
            A->>BC: DID Document 조회
            BC-->>A: DID Document
            A-->>S: DID Document (JSON)
            S->>S: VC 서명 검증

            S->>A: getVcMeta(vcId)
            A->>BC: VC Meta 조회
            BC-->>A: VC Meta
            A-->>S: VC Meta (JSON)
            S->>S: VC 상태 검증
        end

        S-->>A: vpJson (검증 완료)
        A->>DB: VpSubmit 저장
    end

    rect rgb(255, 230, 240)
        Note over H,DB: 4. 검증 확인
        A->>S: confirmVerification(txId, vpJson, verified)
        S->>S: Holder DID 추출
        S->>S: VC 목록 추출 (Phase 2-4: Map 변환)
        S->>S: 클레임 추출
        S-->>A: VerificationConfirmResult
        A-->>H: 검증 완료
    end
```

---

## DTO 구조

### 주요 DTO 클래스

#### VpVerificationRequest

```java
@Getter
@Builder
public class VpVerificationRequest {
    private String txId;                    // Transaction ID
    private String encHolderPublicKey;      // Phase 2-1: 새로 추가
    private String encVp;                   // 암호화된 VP
    private String iv;                      // IV
    private String verifierNonce;           // Profile에 포함된 nonce
    private Integer requiredAuthType;       // 요구 인증 타입
}
```

---

#### VerificationConfirmResult

```java
@Getter
@Builder
public class VerificationConfirmResult {
    private String txId;                          // Transaction ID
    private Boolean verified;                     // 검증 성공 여부
    private String holderDid;                     // Holder DID
    private Map<String, Object> submittedVcs;     // Phase 2-4: Map으로 수정
    private Map<String, Object> extractedClaims;  // 추출된 클레임
    private Instant verifiedAt;                   // 검증 시간
    private String errorMessage;                  // 오류 메시지
}
```

---

#### VpOfferPayload

```java
@Getter
@Builder
public class VpOfferPayload {
    private String offerId;              // Offer ID
    private String type;                 // "VerifyOffer"
    private String mode;                 // "Direct" | "Indirect"
    private String device;               // 응대장치 식별자
    private String service;              // 서비스 식별자
    private List<String> endpoints;      // API 엔드포인트 목록
    private Instant validUntil;          // 유효기간
    private Boolean locked;              // 잠김 여부
}
```

---

## 사용 예제

### 1. SDK 초기화

```java
// Adapter 구현체 생성
VerificationConfigProvider configProvider =
    new VerificationConfigProviderAdapter(policyRepository);

TransactionManager transactionManager =
    new TransactionManagerAdapter(transactionService);

E2eSessionProvider e2eSessionProvider =
    new E2eSessionProviderAdapter(e2eQueryService, e2eRepository, transactionManager);

// SDK 서비스 생성
VerifierService verifierService = new VerifierService(
    configProvider,
    transactionManager,
    e2eSessionProvider,
    verifierInfoProvider,
    storageService,
    cryptoHelper,
    nonceGenerator
);
```

---

### 2. VP Offer 생성

```java
// SDK 호출
VpOfferPayload payload = verifierService.createVpOfferPayload(
    "policy-001",     // policyId
    "mobile-app",     // device
    "kyc-service",    // service
    false             // locked
);

// Application이 DB 저장
vpOfferRepository.save(VpOffer.builder()
    .transactionId(transactionId)
    .offerId(payload.getOfferId())
    .vpPolicyId(policyId)
    .payload(JsonUtil.serializeToJson(payload))
    .build());
```

---

### 3. VP 검증

```java
// VP 검증 요청 생성 (Phase 2-1 수정)
VpVerificationRequest request = VpVerificationRequest.builder()
    .txId(txId)
    .encHolderPublicKey(accE2e.getPublicKey())  // ← Phase 2-1: 추가
    .encVp(encVp)
    .iv(accE2e.getIv())
    .verifierNonce(serverNonce)
    .requiredAuthType(authType)
    .build();

// SDK 호출
String vpJson = verifierService.verifyPresentation(request);

// Application이 DB 저장
vpSubmitRepository.save(VpSubmit.builder()
    .transactionId(transactionId)
    .vp(vpJson)
    .holderDid(holderDid)
    .build());
```

---

### 4. 검증 확인

```java
// SDK 호출
VerificationConfirmResult result = verifierService.confirmVerification(
    txId,
    vpJson,
    true  // verified
);

// 결과 사용
if (result.getVerified()) {
    String holderDid = result.getHolderDid();
    Map<String, Object> claims = result.getExtractedClaims();

    // 클레임 활용
    String name = (String) claims.get("name");
    String birthDate = (String) claims.get("birthDate");
}
```

---

## Phase 2 수정사항

### 🔴 Critical Issues (수정 완료)

#### Issue 2-1: E2E 복호화 구현 오류

**변경 파일**:
- `E2eSessionProvider.java`
- `VpVerificationService.java`
- `VerifierService.java`

**수정 내용**:
```java
// Before (Phase 2-1)
String decrypt(String txId, String encData, String iv);

// After (Phase 2-1 수정)
String decrypt(String txId, String encHolderPublicKey, String encVp, String iv);
```

**ECDH 프로토콜 구현**:
1. `encHolderPublicKey` 디코딩 → Holder 공개키 추출
2. ECDH: Verifier 개인키 + Holder 공개키 → 공유 비밀키
3. 세션키 = KDF(공유 비밀키 + nonce)
4. VP = Decrypt(encVp, 세션키, iv)

---

#### Issue 2-4: VerificationConfirmService 타입 캐스팅 오류

**변경 파일**:
- `VerificationConfirmService.java`

**수정 내용**:
```java
// Before (타입 불일치)
List<String> submittedVcs = extractSubmittedVcs(vpObject);
.submittedVcs((Map<String, Object>) submittedVcs)  // ClassCastException!

// After (Phase 2-4 수정)
Map<String, Object> submittedVcs = extractSubmittedVcsAsMap(vpObject);
.submittedVcs(submittedVcs)  // ✅ 정상
```

---

### 🟡 High Priority Issues (수정 완료)

#### Issue 2-2: TransactionManager 메모리 누수

**변경 파일**:
- `TransactionManager.java` (인터페이스 - Deprecated 추가)

**수정 내용**:
```java
// DEPRECATED: Phase 2-2 수정으로 비활성화
@Deprecated
void saveTransactionState(String txId, String state);

@Deprecated
String getTransactionState(String txId);
```

**권장사항**:
- Application에서 DB를 통해 직접 상태 관리
- In-Memory 저장소 사용 금지 (메모리 누수 방지)

---

## 배포 가이드

### JAR 빌드

```bash
# SDK만 빌드
./gradlew :verifier-sdk:jar

# 빌드 결과
# build/libs/verifier-sdk-1.0.0.jar
```

### 의존성 추가

```gradle
dependencies {
    implementation 'org.omnione.did:verifier-sdk:1.0.0'
}
```

### 최소 요구사항

- **Java**: 21+
- **Spring Boot**: 3.2.4+
- **Gson**: 2.10+

---

## 라이선스

Apache License 2.0

---

## 참고 문서

- [Application Architecture Guide](../APPLICATION_ARCHITECTURE.md)
- [API Documentation](../docs/api/Verifier_API_ko.md)
- [Phase 2 수정 보고서](../PHASE2_FINAL_REPORT.md)

---

**Last Updated**: 2026-01-26
**Maintainer**: OpenDID Verifier Team
