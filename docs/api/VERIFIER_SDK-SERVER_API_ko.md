# Verifier SDK — Server API 레퍼런스

> **버전**: 2.0.0  
> **패키지**: `org.omnione.did.verifier.v1`

이 문서는 SDK Facade 인터페이스인 `VerifierService`가 노출하는 모든 공개 메서드를 설명합니다. 인스턴스는 `VerifierSdkBuilder`를 통해 획득합니다.

---

## 목차

1. [VerifierService (Facade)](#1-verifierservice-facade)
   - [requestVpOffer](#11-requestvpoffer)
   - [requestStaticVpOffer](#12-requeststaticvpoffer)
   - [requestVerifyProfile](#13-requestverifyprofile)
   - [verifyPresentation](#14-verifypresentation)
   - [decryptVp](#15-decryptvp)
   - [confirmVerification](#16-confirmverification)
   - [requestZkpProofRequestProfile](#17-requestzkpproofrequestprofile)
   - [verifyZkpProof](#18-verifyzkpproof)
   - [decryptZkpProof](#19-decryptzkpproof)
2. [SPI 인터페이스](#2-spi-인터페이스)
   - [VerificationConfigProvider](#21-verificationconfigprovider)
   - [VerifierInfoProvider](#22-verifierinfoprovider)
   - [EcdhSessionProvider](#23-ecdhsessionprovider)
   - [StorageProvider](#24-storageprovider)
   - [TransactionProvider](#25-transactionprovider)
   - [NonceGenerator](#26-noncegenerator)
   - [CryptoHelper](#27-cryptohelper)
3. [데이터 모델](#3-데이터-모델)

---

## 1. VerifierService (Facade)

**인터페이스**: `org.omnione.did.verifier.v1.protocol.VerifierService`  
**기본 구현체**: `org.omnione.did.verifier.v1.core.VerifierServiceImpl`

VP/ZKP 검증 작업의 통합 진입점입니다. `VerifierSdkBuilder`를 통해 인스턴스를 획득합니다:

```java
VerifierService verifierService = VerifierSdkBuilder.builder()
    .configProvider(configProvider)
    .verifierInfoProvider(verifierInfoProvider)
    .sessionProvider(sessionProvider)
    .storageProvider(storageProvider)
    .transactionProvider(transactionProvider)
    .nonceGenerator(nonceGenerator)
    .cryptoHelper(cryptoHelper)
    .build();
```

---

### 1.1 `requestVpOffer`

**Dynamic QR 코드** 흐름을 위한 VP Offer Payload를 생성합니다.

```java
VpOfferPayload requestVpOffer(
    String policyId,
    String device,
    String service,
    boolean locked
)
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `policyId` | `String` | ✅ | 검증 정책 식별자 |
| `device` | `String` | ✅ | 응대장치 식별자 |
| `service` | `String` | ✅ | 서비스 식별자 |
| `locked` | `boolean` | ✅ | Offer 잠김 여부 (특정 Holder 전용) |

**반환값**: `VpOfferPayload` — Dynamic QR 흐름을 위한 `offerId` 포함.

**예외**: 정책 미발견 시 `VerifierSdkException` (`SSDKVRF002008`).

---

### 1.2 `requestStaticVpOffer`

**Static QR 코드** 흐름을 위한 VP Offer Payload를 생성합니다. `offerId` 미포함.

```java
VpOfferPayload requestStaticVpOffer(
    String policyId,
    String device,
    String service,
    boolean locked
)
```

파라미터는 `requestVpOffer`와 동일합니다. 반환된 `VpOfferPayload`에 `offerId`가 없습니다.

---

### 1.3 `requestVerifyProfile`

주어진 정책에 대한 검증 요구사항을 기술하는 `VerifyProfile`을 생성합니다.

```java
VerifyProfile requestVerifyProfile(
    String policyId,
    String profileId,
    ReqE2e reqE2e
)
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `policyId` | `String` | ✅ | 검증 정책 식별자 |
| `profileId` | `String` | ✅ | 고유 Profile ID (UUID 권장) |
| `reqE2e` | `ReqE2e` | ✅ | Holder의 E2E 키 교환 요청 |

**반환값**: Proof가 없는 `VerifyProfile`. 호출 애플리케이션에서 Holder에게 반환하기 전에 서명을 추가해야 합니다.

**예외**: 정책 미발견 시 `SSDKVRF002008`.

---

### 1.4 `verifyPresentation`

VP 검증 전체 파이프라인(E2E 복호화 → 서명 검증 → VC 상태 확인)을 수행합니다.

```java
String verifyPresentation(VpVerificationRequest request)
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| `txId` | `String` | ✅ | 트랜잭션 ID |
| `vp` | `String` | 조건부 | 평문 VP JSON (비암호화 시) |
| `encrypted` | `Boolean` | ✅ | VP가 E2E 암호화된 경우 `true` |
| `encVp` | `String` | 조건부 | 암호화된 VP (`encrypted=true` 시) |
| `iv` | `String` | 조건부 | AES IV (`encrypted=true` 시) |
| `encHolderPublicKey` | `String` | 조건부 | 암호화된 Holder 공개키 (`encrypted=true` 시) |
| `verifierNonce` | `String` | ✅ | Profile에 포함된 Nonce |
| `requiredAuthType` | `Integer` | 선택 | 요구 인증 타입 비트마스크 |
| `filter` | `Filter` | 선택 | 정책에서 가져온 클레임 필터 |

**반환값**: 검증된 VP JSON 문자열.

**예외**:
- `SSDKVRF001006` — VP 검증 실패
- `SSDKVRF001007` — VC 검증 실패
- `SSDKVRF003005` — 복호화 실패
- `SSDKVRF002004` — 트랜잭션 만료

---

### 1.5 `decryptVp`

전체 검증 흐름과 독립적으로 E2E 암호화된 VP를 복호화합니다.

```java
String decryptVp(
    String txId,
    String encHolderPublicKey,
    String encVp,
    String iv
)
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `txId` | `String` | E2E 세션 조회용 트랜잭션 ID |
| `encHolderPublicKey` | `String` | Verifier 키로 암호화된 Holder 공개키 |
| `encVp` | `String` | AES-256-CBC 암호화된 VP |
| `iv` | `String` | AES 초기화 벡터 |

**반환값**: 복호화된 VP JSON 문자열.

**예외**: 복호화 실패 시 `SSDKVRF003005`.

---

### 1.6 `confirmVerification`

검증된 VP에서 클레임을 추출하고 검증 결과를 기록합니다.

**오버로드 1** — VP 전용:

```java
VerificationConfirmResult confirmVerification(
    String txId,
    String vpJson,
    boolean verified
)
```

**오버로드 2** — 명시적 Offer 타입 포함 (VP 또는 ZKP Proof):

```java
VerificationConfirmResult confirmVerification(
    String txId,
    String vpOrProofJson,
    boolean verified,
    OfferType offerType
)
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `txId` | `String` | 트랜잭션 ID |
| `vpJson` / `vpOrProofJson` | `String` | 검증된 VP 또는 Proof JSON |
| `verified` | `boolean` | 검증 성공 여부 |
| `offerType` | `OfferType` | `VerifyOffer` 또는 `VerifyProofOffer` |

**반환값**: `VerificationConfirmResult`

| 필드 | 타입 | 설명 |
|-----|------|------|
| `txId` | `String` | 트랜잭션 ID |
| `verified` | `Boolean` | 검증 성공 여부 |
| `holderDid` | `String` | Holder DID |
| `submittedVcs` | `Map<String, Object>` | 제출된 VC 목록 (VC ID → VC 객체) |
| `extractedClaims` | `Map<String, Object>` | 추출된 클레임 (코드 → 값) |
| `verifiedAt` | `Instant` | 검증 일시 |
| `errorMessage` | `String` | 실패 사유 (선택) |
| `errorCode` | `String` | 에러 코드 (선택) |

---

### 1.7 `requestZkpProofRequestProfile`

ZKP 검증 요청을 정의하는 `ProofRequestProfile`을 생성합니다.

```java
ProofRequestProfile requestZkpProofRequestProfile(
    ProofRequestProfileRequest request,
    ZkpPolicy policy
)
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `request` | `ProofRequestProfileRequest` | Profile 요청 메타데이터 |
| `policy` | `ZkpPolicy` | ZKP 정책 설정 |

**반환값**: Proof가 포함된 `ProofRequestProfile` (추가 서명 불필요).

---

### 1.8 `verifyZkpProof`

제출된 ZKP Proof를 검증합니다.

```java
ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request)
```

**반환값**: `ZkpVerificationResult`

| 필드 | 타입 | 설명 |
|-----|------|------|
| `verified` | `Boolean` | ZKP Proof 유효 여부 |
| `revealedAttributes` | `Map<String, Object>` | 공개된 속성 값 |
| `holderDid` | `String` | Holder DID (공개된 경우) |

---

### 1.9 `decryptZkpProof`

E2E 암호화된 ZKP Proof를 복호화합니다.

```java
Proof decryptZkpProof(
    String txId,
    String encProof,
    String iv,
    ZkpVerificationRequest.AccE2e accE2e
)
```

**반환값**: 복호화된 `Proof` 객체 (`org.omnione.did.zkp.datamodel.proof.Proof`).

---

## 2. SPI 인터페이스

모든 인터페이스는 `org.omnione.did.verifier.v1.provider` 패키지에 있습니다.

### 2.1 `VerificationConfigProvider`

검증 정책 및 설정을 제공합니다.

```java
public interface VerificationConfigProvider {
    VerificationPolicy getPolicy(String policyId);
    List<String> getAllPolicyIds();
}
```

### 2.2 `VerifierInfoProvider`

Verifier 식별 메타데이터(DID, 키)를 제공합니다.

```java
public interface VerifierInfoProvider {
    String getVerifierDid();
    KeyPairInfo getKeyPairInfo(String keyId);
}
```

### 2.3 `EcdhSessionProvider`

E2E 암호화 세션 상태(ECDH 키쌍)를 관리합니다.

```java
public interface EcdhSessionProvider {
    void saveSession(String txId, KeyPairInfo keyPair);
    KeyPairInfo getSession(String txId);
    void deleteSession(String txId);
}
```

### 2.4 `StorageProvider`

VP 및 VC 데이터를 영속화합니다.

```java
public interface StorageProvider {
    void saveVp(String txId, String vpJson);
    String getVp(String txId);
    void saveVcMeta(String vcId, Object vcMeta);
    Object getVcMeta(String vcId);
}
```

### 2.5 `TransactionProvider`

트랜잭션 생명주기를 관리합니다.

```java
public interface TransactionProvider {
    void createTransaction(String txId, String policyId, OfferType offerType);
    TransactionInfo getTransaction(String txId);
    void updateTransactionStatus(String txId, TransactionStatus status);
    void expireTransaction(String txId);
}
```

### 2.6 `NonceGenerator`

암호학적 Nonce를 생성합니다.

```java
public interface NonceGenerator {
    String generate();
}
```

### 2.7 `CryptoHelper`

암호화 유틸리티(서명, ECDH)를 제공합니다.

```java
public interface CryptoHelper {
    byte[] sign(String keyId, byte[] data);
    boolean verify(String did, String keyId, byte[] data, byte[] signature);
    byte[] ecdhKeyAgreement(byte[] privateKey, byte[] publicKey);
    byte[] encrypt(byte[] key, byte[] iv, byte[] data);
    byte[] decrypt(byte[] key, byte[] iv, byte[] encData);
}
```

---

## 3. 데이터 모델

### VpOfferPayload

| 필드 | 타입 | 설명 |
|-----|------|------|
| `offerId` | `String` | Offer ID (Dynamic QR 전용) |
| `type` | `OfferType` | `VerifyOffer` \| `VerifyProofOffer` |
| `mode` | `PresentMode` | `Direct` \| `Indirect` \| `Proxy` |
| `device` | `String` | 응대장치 식별자 |
| `service` | `String` | 서비스 식별자 |
| `expiresAt` | `Instant` | Offer 만료 시각 |

### ReqE2e

| 필드 | 타입 | 설명 |
|-----|------|------|
| `nonce` | `String` | Holder가 생성한 Nonce |
| `curve` | `String` | 키 곡선 (예: `secp256r1`) |
| `publicKey` | `String` | Holder의 임시 공개키 (multibase 인코딩) |
| `cipher` | `String` | 암호화 알고리즘 (예: `AES-256-CBC`) |
| `padding` | `String` | 패딩 방식 (예: `PKCS5`) |

### OfferType

| 값 | 설명 |
|----|------|
| `VerifyOffer` | 표준 VP 검증 |
| `VerifyProofOffer` | ZKP Proof 검증 |
