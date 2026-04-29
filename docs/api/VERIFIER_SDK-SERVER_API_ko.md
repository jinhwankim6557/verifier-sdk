---
puppeteer:
    pdf:
        format: A4
        displayHeaderFooter: true
        landscape: false
        scale: 0.8
        margin:
            top: 1.2cm
            right: 1cm
            bottom: 1cm
            left: 1cm
---

Verifier SDK Server API
==

- Subject: Verifier SDK Server API
- Author: OpenDID
- Date: 2026-04-29
- Version: v2.0.0

| 버전 | 날짜 | 변경 내용 |
|------|------|---------|
| v2.0.0 | 2026-04-29 | 최초 작성 |


<div style="page-break-after: always;"></div>

# 목차
- [1. APIs](#1-apis)
    - [1.1 VerifierService](#11-verifierservice)
        - [1.1.1 requestVpOffer](#111-requestvpoffer)
        - [1.1.2 requestStaticVpOffer](#112-requeststaticvpoffer)
        - [1.1.3 requestVerifyProfile](#113-requestverifyprofile)
        - [1.1.4 verifyPresentation](#114-verifypresentation)
        - [1.1.5 decryptVp](#115-decryptvp)
        - [1.1.6 confirmVerification (VP)](#116-confirmverification-vp)
        - [1.1.7 confirmVerification (VP or Proof)](#117-confirmverification-vp-or-proof)
        - [1.1.8 requestZkpProofRequestProfile](#118-requestzkpproofrequestprofile)
        - [1.1.9 verifyZkpProof](#119-verifyzkpproof)
        - [1.1.10 decryptZkpProof](#1110-decryptzkpproof)
- [2. 인터페이스](#2-인터페이스)
    - [2.1 VerificationConfigProvider](#21-verificationconfigprovider)
    - [2.2 VerifierInfoProvider](#22-verifierinfoprovider)
    - [2.3 EcdhSessionProvider](#23-ecdhsessionprovider)
    - [2.4 StorageProvider](#24-storageprovider)
    - [2.5 TransactionProvider](#25-transactionprovider)
    - [2.6 NonceGenerator](#26-noncegenerator)
    - [2.7 CryptoHelper](#27-cryptohelper)
- [3. 데이터 클래스](#3-데이터-클래스)
    - [3.1 VpOfferPayload](#31-vpofferpayload)
    - [3.2 VpVerificationRequest](#32-vpverificationrequest)
    - [3.3 VerificationConfirmResult](#33-verificationconfirmresult)
    - [3.4 ZkpVerificationResult](#34-zkpverificationresult)
    - [3.5 ReqE2e](#35-reqe2e)

<div style="page-break-after: always;"></div>

## 개요

이 문서는 OpenDID 생태계에서 **VP(Verifiable Presentation) 및 ZKP(Zero-Knowledge Proof) 검증** 프로토콜을 구현하기 위한 Server SDK API를 정의합니다.

### 주요 기능
- Dynamic/Static QR 코드 흐름을 위한 VP Offer 생성
- E2E 키 교환이 포함된 VerifyProfile 생성
- VP 검증 파이프라인: E2E 복호화 → 서명 검증 → VC 상태 확인
- 클레임 추출 및 검증 결과 기록
- ZKP 익명 증명 요청 및 검증

<br>

<div style="page-break-after: always;"></div>

# 1. APIs

## 1.1 VerifierService

`VerifierService` 인터페이스는 SDK Facade입니다. `VerifierSdkBuilder`를 통해 인스턴스를 획득합니다.

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

<br>

### 1.1.1 requestVpOffer

### Class Name
`VerifierService`

### Function Name
`requestVpOffer`

### Function Introduction
`Dynamic QR 코드 흐름을 위한 VP Offer Payload를 생성합니다. 반환된 페이로드에는 Holder의 응답과 매칭하기 위한 offerId가 포함됩니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| policyId | String | 검증 정책 식별자 | M | |
| device | String | 응대장치 식별자 | M | |
| service | String | 서비스 식별자 | M | |
| locked | boolean | 특정 Holder 전용 잠김 여부 | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VpOfferPayload | VP Offer 페이로드 | M | offerId 포함. [Link](#31-vpofferpayload) |

### Function Declaration

```java
VpOfferPayload requestVpOffer(
    String policyId,
    String device,
    String service,
    boolean locked
)
```

### Function Usage
```java
VpOfferPayload offer = verifierService.requestVpOffer(
    "policy-login",
    "kiosk-01",
    "login-service",
    false
);
String offerId = offer.getOfferId(); // QR 코드에 포함
```

<br>

### 1.1.2 requestStaticVpOffer

### Class Name
`VerifierService`

### Function Name
`requestStaticVpOffer`

### Function Introduction
`Static QR 코드 흐름을 위한 VP Offer Payload를 생성합니다. offerId가 포함되지 않으므로 동일한 QR 코드를 여러 세션에 재사용할 수 있습니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| policyId | String | 검증 정책 식별자 | M | |
| device | String | 응대장치 식별자 | M | |
| service | String | 서비스 식별자 | M | |
| locked | boolean | 특정 Holder 전용 잠김 여부 | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VpOfferPayload | VP Offer 페이로드 | M | offerId는 null. [Link](#31-vpofferpayload) |

### Function Declaration

```java
VpOfferPayload requestStaticVpOffer(
    String policyId,
    String device,
    String service,
    boolean locked
)
```

<br>

### 1.1.3 requestVerifyProfile

### Class Name
`VerifierService`

### Function Name
`requestVerifyProfile`

### Function Introduction
`주어진 정책에 대한 검증 요구사항을 기술하는 VerifyProfile을 생성합니다. 호출 애플리케이션은 반환된 프로파일에 서명을 추가한 뒤 Holder에게 전달해야 합니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| policyId | String | 검증 정책 식별자 | M | |
| profileId | String | 고유 프로파일 식별자 | M | UUID 권장 |
| reqE2e | ReqE2e | Holder의 E2E 키 교환 요청 | M | [Link](#35-reqe2e) |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VerifyProfile | Proof가 없는 검증 프로파일 | M | 애플리케이션에서 Proof 추가 필요 |

### Function Declaration

```java
VerifyProfile requestVerifyProfile(
    String policyId,
    String profileId,
    ReqE2e reqE2e
)
```

### Function Usage
```java
ReqE2e reqE2e = ReqE2e.builder()
    .nonce(holderNonce)
    .curve("secp256r1")
    .publicKey(holderPublicKeyMultibase)
    .cipher("AES-256-CBC")
    .padding("PKCS5")
    .build();

VerifyProfile profile = verifierService.requestVerifyProfile(
    "policy-login",
    UUID.randomUUID().toString(),
    reqE2e
);
// profile.getCore()에 서명 후 Proof를 추가하여 Holder에 반환
```

<br>

### 1.1.4 verifyPresentation

### Class Name
`VerifierService`

### Function Name
`verifyPresentation`

### Function Introduction
`VP 검증 전체 파이프라인(E2E 복호화 → 서명 검증 → VC 상태 확인)을 수행합니다. 암호화된 VP와 평문 VP 모두 지원합니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| request | VpVerificationRequest | VP 검증 요청 | M | [Link](#32-vpverificationrequest) |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| String | 검증된 VP JSON 문자열 | M | |

### Function Declaration

```java
String verifyPresentation(VpVerificationRequest request)
```

### Function Usage
```java
VpVerificationRequest request = VpVerificationRequest.builder()
    .txId(txId)
    .encrypted(true)
    .encVp(encVp)
    .iv(iv)
    .encHolderPublicKey(encHolderPublicKey)
    .verifierNonce(verifierNonce)
    .filter(policyFilter)
    .build();

String vpJson = verifierService.verifyPresentation(request);
```

<br>

### 1.1.5 decryptVp

### Class Name
`VerifierService`

### Function Name
`decryptVp`

### Function Introduction
`전체 검증 흐름과 독립적으로 E2E 암호화된 VP를 복호화합니다. 복호화와 검증을 별도 단계로 처리할 때 사용합니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | E2E 세션 조회용 트랜잭션 ID | M | |
| encHolderPublicKey | String | Verifier 키로 암호화된 Holder 공개키 | M | |
| encVp | String | AES-256-CBC 암호화된 VP | M | |
| iv | String | AES 초기화 벡터 | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| String | 복호화된 VP JSON 문자열 | M | |

### Function Declaration

```java
String decryptVp(
    String txId,
    String encHolderPublicKey,
    String encVp,
    String iv
)
```

<br>

### 1.1.6 confirmVerification (VP)

### Class Name
`VerifierService`

### Function Name
`confirmVerification`

### Function Introduction
`검증된 VP에서 클레임을 추출하고 검증 결과를 기록합니다. 표준 VP 검증 흐름에서 사용하는 오버로드입니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | 트랜잭션 ID | M | |
| vpJson | String | 검증된 VP JSON 문자열 | M | |
| verified | boolean | 검증 성공 여부 | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VerificationConfirmResult | 검증 확인 결과 | M | [Link](#33-verificationconfirmresult) |

### Function Declaration

```java
VerificationConfirmResult confirmVerification(
    String txId,
    String vpJson,
    boolean verified
)
```

### Function Usage
```java
VerificationConfirmResult result = verifierService.confirmVerification(txId, vpJson, true);

String holderDid = result.getHolderDid();
Map<String, Object> claims = result.getExtractedClaims();
```

<br>

### 1.1.7 confirmVerification (VP or Proof)

### Class Name
`VerifierService`

### Function Name
`confirmVerification`

### Function Introduction
`검증된 VP 또는 ZKP Proof에서 클레임을 추출하고 검증 결과를 기록합니다. Offer 타입을 명시적으로 지정해야 할 때 사용하는 오버로드입니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | 트랜잭션 ID | M | |
| vpOrProofJson | String | 검증된 VP 또는 Proof JSON 문자열 | M | |
| verified | boolean | 검증 성공 여부 | M | |
| offerType | OfferType | Offer 타입 | M | `VerifyOffer` 또는 `VerifyProofOffer` |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VerificationConfirmResult | 검증 확인 결과 | M | [Link](#33-verificationconfirmresult) |

### Function Declaration

```java
VerificationConfirmResult confirmVerification(
    String txId,
    String vpOrProofJson,
    boolean verified,
    OfferType offerType
)
```

<br>

### 1.1.8 requestZkpProofRequestProfile

### Class Name
`VerifierService`

### Function Name
`requestZkpProofRequestProfile`

### Function Introduction
`ZKP 검증 요청을 정의하는 ProofRequestProfile을 생성합니다. 반환된 프로파일에는 이미 Proof가 포함되어 있어 추가 서명이 필요하지 않습니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| request | ProofRequestProfileRequest | 프로파일 요청 메타데이터 | M | |
| policy | ZkpPolicy | ZKP 정책 설정 | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| ProofRequestProfile | ZKP 증명 요청 프로파일 | M | Proof 포함 |

### Function Declaration

```java
ProofRequestProfile requestZkpProofRequestProfile(
    ProofRequestProfileRequest request,
    ZkpPolicy policy
)
```

<br>

### 1.1.9 verifyZkpProof

### Class Name
`VerifierService`

### Function Name
`verifyZkpProof`

### Function Introduction
`제출된 ZKP Proof를 검증하고 공개된 속성을 추출합니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| request | ZkpVerificationRequest | ZKP 검증 요청 | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| ZkpVerificationResult | ZKP 검증 결과 | M | [Link](#34-zkpverificationresult) |

### Function Declaration

```java
ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request)
```

<br>

### 1.1.10 decryptZkpProof

### Class Name
`VerifierService`

### Function Name
`decryptZkpProof`

### Function Introduction
`전체 검증 흐름과 독립적으로 E2E 암호화된 ZKP Proof를 복호화합니다.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | E2E 세션 조회용 트랜잭션 ID | M | |
| encProof | String | 암호화된 Proof | M | |
| iv | String | AES 초기화 벡터 | M | |
| accE2e | ZkpVerificationRequest.AccE2e | E2E 누산기 정보 | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| Proof | 복호화된 Proof 객체 | M | `org.omnione.did.zkp.datamodel.proof.Proof` |

### Function Declaration

```java
Proof decryptZkpProof(
    String txId,
    String encProof,
    String iv,
    ZkpVerificationRequest.AccE2e accE2e
)
```

<br>

# 2. 인터페이스

## 2.1 VerificationConfigProvider

### Declaration

```java
public interface VerificationConfigProvider {
    VerificationPolicy getPolicy(String policyId);
    List<String> getAllPolicyIds();
}
```

### Description
`정책 ID로 검증 정책을 제공합니다. 데이터베이스, 설정 파일, 원격 서비스 등에서 정책을 로드하도록 구현합니다.`

### Usage
```java
@Component
public class DbVerificationConfigProvider implements VerificationConfigProvider {

    @Override
    public VerificationPolicy getPolicy(String policyId) {
        return policyRepository.findById(policyId)
            .map(this::toVerificationPolicy)
            .orElseThrow(() -> new VerifierSdkException(SDK_POLICY_NOT_FOUND));
    }

    @Override
    public List<String> getAllPolicyIds() {
        return policyRepository.findAllIds();
    }
}
```

<br>

## 2.2 VerifierInfoProvider

### Declaration

```java
public interface VerifierInfoProvider {
    String getVerifierDid();
    KeyPairInfo getKeyPairInfo(String keyId);
}
```

### Description
`Verifier 식별 메타데이터(DID 및 키쌍)를 제공합니다. Verifier의 DID와 서명 키를 공급하도록 구현합니다.`

<br>

## 2.3 EcdhSessionProvider

### Declaration

```java
public interface EcdhSessionProvider {
    void saveSession(String txId, KeyPairInfo keyPair);
    KeyPairInfo getSession(String txId);
    void deleteSession(String txId);
}
```

### Description
`E2E 암호화 세션 상태(임시 ECDH 키쌍)를 관리합니다. 기본 인메모리 구현이 제공되며, Redis/DB 등 커스텀 구현도 지원합니다.`

<br>

## 2.4 StorageProvider

### Declaration

```java
public interface StorageProvider {
    void saveVp(String txId, String vpJson);
    String getVp(String txId);
    void saveVcMeta(String vcId, Object vcMeta);
    Object getVcMeta(String vcId);
}
```

### Description
`VP 및 VC 메타데이터를 영속화합니다. 제출된 VP와 VC 메타데이터를 감사 및 클레임 추출을 위해 저장하도록 구현합니다.`

<br>

## 2.5 TransactionProvider

### Declaration

```java
public interface TransactionProvider {
    void createTransaction(String txId, String policyId, OfferType offerType);
    TransactionInfo getTransaction(String txId);
    void updateTransactionStatus(String txId, TransactionStatus status);
    void expireTransaction(String txId);
}
```

### Description
`트랜잭션 생명주기를 관리합니다. 각 VP/ZKP 검증 흐름은 Offer 생성부터 확인 완료까지 트랜잭션 ID로 추적됩니다.`

<br>

## 2.6 NonceGenerator

### Declaration

```java
@FunctionalInterface
public interface NonceGenerator {
    String generate();
}
```

### Description
`암호학적 Nonce를 생성하는 함수형 인터페이스입니다. 생성된 Nonce는 VerifyProfile에 포함되어 VP 제출 시 검증됩니다.`

### Usage
```java
NonceGenerator nonceGenerator = () -> {
    byte[] bytes = new byte[16];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
};
```

<br>

## 2.7 CryptoHelper

### Declaration

```java
public interface CryptoHelper {
    byte[] sign(String keyId, byte[] data);
    boolean verify(String did, String keyId, byte[] data, byte[] signature);
    byte[] ecdhKeyAgreement(byte[] privateKey, byte[] publicKey);
    byte[] encrypt(byte[] key, byte[] iv, byte[] data);
    byte[] decrypt(byte[] key, byte[] iv, byte[] encData);
}
```

### Description
`VP 검증 중 사용되는 암호화 유틸리티를 제공합니다. KMS, HSM, 로컬 키스토어 등 키 관리 시스템과 연동하도록 구현합니다.`

<br>

# 3. 데이터 클래스

## 3.1 VpOfferPayload

### Declaration

```java
@Getter
@Builder
public class VpOfferPayload {
    private String offerId;
    private OfferType type;
    private PresentMode mode;
    private String device;
    private String service;
    private Instant expiresAt;
}
```

### Property

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| offerId | String | Offer 식별자 | O | Static QR인 경우 null |
| type | OfferType | Offer 타입 | M | `VerifyOffer` \| `VerifyProofOffer` |
| mode | PresentMode | 제출 모드 | M | `Direct` \| `Indirect` \| `Proxy` |
| device | String | 응대장치 식별자 | M | |
| service | String | 서비스 식별자 | M | |
| expiresAt | Instant | Offer 만료 시각 | M | |

<br>

## 3.2 VpVerificationRequest

### Declaration

```java
@Getter
@Builder
public class VpVerificationRequest {
    private String vp;
    @Builder.Default
    private Boolean encrypted = false;
    private String encVp;
    private String iv;
    private String encHolderPublicKey;
    private String txId;
    private String serverToken;
    private String verifierNonce;
    private Integer requiredAuthType;
    private Filter filter;
}
```

### Property

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | 트랜잭션 식별자 | M | |
| vp | String | 평문 VP JSON | O | `encrypted=false` 시 필수 |
| encrypted | Boolean | E2E 암호화 여부 | M | 기본값: false |
| encVp | String | 암호화된 VP | O | `encrypted=true` 시 필수 |
| iv | String | AES 초기화 벡터 | O | `encrypted=true` 시 필수 |
| encHolderPublicKey | String | Verifier 키로 암호화된 Holder 공개키 | O | `encrypted=true` 시 필수 |
| verifierNonce | String | VerifyProfile에 포함된 Nonce | M | |
| serverToken | String | 인증 토큰 | O | |
| requiredAuthType | Integer | 요구 인증 타입 비트마스크 | O | 0x00000002=PIN, 0x00000004=BIO |
| filter | Filter | 정책에서 가져온 클레임 필터 | O | |

<br>

## 3.3 VerificationConfirmResult

### Declaration

```java
@Getter
@Builder
public class VerificationConfirmResult {
    private String txId;
    private Boolean verified;
    private String holderDid;
    private Map<String, Object> submittedVcs;
    private Map<String, Object> extractedClaims;
    private Instant verifiedAt;
    private String errorMessage;
    private String errorCode;
}
```

### Property

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | 트랜잭션 식별자 | M | |
| verified | Boolean | 검증 성공 여부 | M | |
| holderDid | String | Holder DID | O | |
| submittedVcs | Map\<String, Object\> | 제출된 VC 목록 (VC ID → VC 객체) | O | |
| extractedClaims | Map\<String, Object\> | 추출된 클레임 (코드 → 값) | O | |
| verifiedAt | Instant | 검증 일시 | M | |
| errorMessage | String | 실패 사유 | O | `verified=false` 시 존재 |
| errorCode | String | 에러 코드 | O | `verified=false` 시 존재 |

<br>

## 3.4 ZkpVerificationResult

### Declaration

```java
@Getter
@Builder
public class ZkpVerificationResult {
    private Boolean verified;
    private Map<String, Object> revealedAttributes;
    private String holderDid;
}
```

### Property

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| verified | Boolean | ZKP Proof 유효 여부 | M | |
| revealedAttributes | Map\<String, Object\> | 공개된 속성 값 | O | |
| holderDid | String | Holder DID | O | 공개된 경우에만 존재 |

<br>

## 3.5 ReqE2e

### Declaration

```java
@Getter
@Builder
public class ReqE2e {
    private String nonce;
    private String curve;
    private String publicKey;
    private String cipher;
    private String padding;
}
```

### Property

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| nonce | String | Holder가 생성한 Nonce | M | |
| curve | String | 키 곡선 | M | 예: `secp256r1` |
| publicKey | String | Holder의 임시 공개키 | M | Multibase 인코딩 |
| cipher | String | 암호화 알고리즘 | M | 예: `AES-256-CBC` |
| padding | String | 패딩 방식 | M | 예: `PKCS5` |
