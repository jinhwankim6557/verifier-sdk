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

| Version | Date | Changes |
|---------|------|---------|
| v2.0.0 | 2026-04-29 | Initial release |


<div style="page-break-after: always;"></div>

# Table of Contents
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
- [2. Interfaces](#2-interfaces)
    - [2.1 VerificationConfigProvider](#21-verificationconfigprovider)
    - [2.2 VerifierInfoProvider](#22-verifierinfoprovider)
    - [2.3 EcdhSessionProvider](#23-ecdhsessionprovider)
    - [2.4 StorageProvider](#24-storageprovider)
    - [2.5 TransactionProvider](#25-transactionprovider)
    - [2.6 NonceGenerator](#26-noncegenerator)
    - [2.7 CryptoHelper](#27-cryptohelper)
- [3. Data Classes](#3-data-classes)
    - [3.1 VpOfferPayload](#31-vpofferpayload)
    - [3.2 VpVerificationRequest](#32-vpverificationrequest)
    - [3.3 VerificationConfirmResult](#33-verificationconfirmresult)
    - [3.4 ZkpVerificationResult](#34-zkpverificationresult)
    - [3.5 ReqE2e](#35-reqe2e)

<div style="page-break-after: always;"></div>

## Overview

This document defines the Server SDK API for implementing the **VP (Verifiable Presentation) and ZKP (Zero-Knowledge Proof) verification** protocol in the OpenDID ecosystem.

### Key Features
- VP Offer generation for Dynamic/Static QR code flows
- VerifyProfile creation with E2E key exchange support
- VP verification pipeline: E2E decryption → signature verification → VC status check
- Claim extraction and verification result recording
- ZKP anonymous proof request and verification

<br>

<div style="page-break-after: always;"></div>

# 1. APIs

## 1.1 VerifierService

The `VerifierService` interface is the SDK facade. Obtain an instance via `VerifierSdkBuilder`.

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
`Generates a VP Offer Payload for a Dynamic QR code flow. The returned payload includes an offerId used to correlate the holder's response.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| policyId | String | Verification policy identifier | M | |
| device | String | Terminal/device identifier | M | |
| service | String | Service identifier | M | |
| locked | boolean | Whether the offer is locked to a specific holder | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VpOfferPayload | VP Offer payload | M | Includes offerId. [Link](#31-vpofferpayload) |

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
String offerId = offer.getOfferId(); // embed in QR code
```

<br>

### 1.1.2 requestStaticVpOffer

### Class Name
`VerifierService`

### Function Name
`requestStaticVpOffer`

### Function Introduction
`Generates a VP Offer Payload for a Static QR code flow. No offerId is included; the same QR code is reused across multiple sessions.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| policyId | String | Verification policy identifier | M | |
| device | String | Terminal/device identifier | M | |
| service | String | Service identifier | M | |
| locked | boolean | Whether the offer is locked to a specific holder | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VpOfferPayload | VP Offer payload | M | offerId is null. [Link](#31-vpofferpayload) |

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
`Creates a VerifyProfile that describes the verification requirements for a given policy. The calling application must sign the returned profile before sending it to the holder.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| policyId | String | Verification policy identifier | M | |
| profileId | String | Unique profile identifier | M | UUID recommended |
| reqE2e | ReqE2e | Holder's E2E key exchange request | M | [Link](#35-reqe2e) |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VerifyProfile | Verification profile without Proof | M | Application must add Proof |

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
// Sign profile.getCore() and attach Proof before returning to holder
```

<br>

### 1.1.4 verifyPresentation

### Class Name
`VerifierService`

### Function Name
`verifyPresentation`

### Function Introduction
`Performs the full VP verification pipeline: E2E decryption → signature verification → VC status check. Accepts both encrypted and plain VP submissions.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| request | VpVerificationRequest | VP verification request | M | [Link](#32-vpverificationrequest) |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| String | Verified VP JSON string | M | |

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
`Decrypts an E2E-encrypted VP independently of the full verification flow. Use when decryption and verification are handled in separate steps.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | Transaction ID for E2E session lookup | M | |
| encHolderPublicKey | String | Holder's public key encrypted with verifier's key | M | |
| encVp | String | AES-256-CBC encrypted VP | M | |
| iv | String | AES initial vector | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| String | Decrypted VP JSON string | M | |

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
`Extracts claims from a verified VP and records the verification result. Use this overload for standard VP verification flows.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | Transaction ID | M | |
| vpJson | String | Verified VP JSON string | M | |
| verified | boolean | Whether verification succeeded | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VerificationConfirmResult | Verification confirmation result | M | [Link](#33-verificationconfirmresult) |

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
`Extracts claims from a verified VP or ZKP Proof and records the verification result. Use this overload when the offer type must be specified explicitly.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | Transaction ID | M | |
| vpOrProofJson | String | Verified VP or Proof JSON string | M | |
| verified | boolean | Whether verification succeeded | M | |
| offerType | OfferType | Offer type | M | `VerifyOffer` or `VerifyProofOffer` |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| VerificationConfirmResult | Verification confirmation result | M | [Link](#33-verificationconfirmresult) |

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
`Creates a ProofRequestProfile defining the ZKP verification request. The returned profile already includes a Proof — no additional signing is required.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| request | ProofRequestProfileRequest | Profile request metadata | M | |
| policy | ZkpPolicy | ZKP policy configuration | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| ProofRequestProfile | ZKP proof request profile | M | Includes Proof |

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
`Verifies a submitted ZKP Proof and extracts revealed attributes.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| request | ZkpVerificationRequest | ZKP verification request | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| ZkpVerificationResult | ZKP verification result | M | [Link](#34-zkpverificationresult) |

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
`Decrypts an E2E-encrypted ZKP Proof independently of the full verification flow.`

### Input Parameters

| Parameter | Type | Description | **M/O** | **Note** |
|-----------|------|-------------|---------|----------|
| txId | String | Transaction ID for E2E session lookup | M | |
| encProof | String | Encrypted Proof | M | |
| iv | String | AES initial vector | M | |
| accE2e | ZkpVerificationRequest.AccE2e | E2E accumulator information | M | |

### Output Parameters

| Type | Description | **M/O** | **Note** |
|------|-------------|---------|----------|
| Proof | Decrypted Proof object | M | `org.omnione.did.zkp.datamodel.proof.Proof` |

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

# 2. Interfaces

## 2.1 VerificationConfigProvider

### Declaration

```java
public interface VerificationConfigProvider {
    VerificationPolicy getPolicy(String policyId);
    List<String> getAllPolicyIds();
}
```

### Description
`Supplies verification policies by policy ID. Implement this interface to load policies from a database, configuration file, or remote service.`

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
`Provides verifier identity metadata (DID and key pairs). Implement to supply the verifier's DID and signing keys.`

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
`Manages E2E encryption session state (ephemeral ECDH key pairs). A default in-memory implementation is provided; custom implementations (Redis, DB) are supported.`

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
`Persists VP and VC metadata. Implement to store submitted VPs and VC metadata for audit and claim extraction.`

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
`Manages transaction lifecycle. Each VP/ZKP verification flow is tracked by a transaction ID from offer creation through confirmation.`

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
`A functional interface for generating cryptographic nonces. The generated nonce is embedded in VerifyProfile and validated during VP submission.`

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
`Provides cryptographic utilities used during VP verification. Implement to integrate with your key management system (KMS, HSM, or local keystore).`

<br>

# 3. Data Classes

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
| offerId | String | Offer identifier | O | Null for static QR |
| type | OfferType | Offer type | M | `VerifyOffer` \| `VerifyProofOffer` |
| mode | PresentMode | Presentation mode | M | `Direct` \| `Indirect` \| `Proxy` |
| device | String | Device identifier | M | |
| service | String | Service identifier | M | |
| expiresAt | Instant | Offer expiration time | M | |

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
| txId | String | Transaction identifier | M | |
| vp | String | Plain VP JSON | O | Required when `encrypted=false` |
| encrypted | Boolean | E2E encryption flag | M | Default: false |
| encVp | String | Encrypted VP | O | Required when `encrypted=true` |
| iv | String | AES initial vector | O | Required when `encrypted=true` |
| encHolderPublicKey | String | Holder public key encrypted with verifier's key | O | Required when `encrypted=true` |
| verifierNonce | String | Nonce from VerifyProfile | M | |
| serverToken | String | Authentication token | O | |
| requiredAuthType | Integer | Required auth type bitmask | O | 0x00000002=PIN, 0x00000004=BIO |
| filter | Filter | Claim filter from policy | O | |

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
| txId | String | Transaction identifier | M | |
| verified | Boolean | Verification success flag | M | |
| holderDid | String | Holder's DID | O | |
| submittedVcs | Map\<String, Object\> | Submitted VCs (VC ID → VC object) | O | |
| extractedClaims | Map\<String, Object\> | Extracted claims (code → value) | O | |
| verifiedAt | Instant | Verification timestamp | M | |
| errorMessage | String | Failure reason | O | Present when `verified=false` |
| errorCode | String | Error code | O | Present when `verified=false` |

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
| verified | Boolean | ZKP proof validity | M | |
| revealedAttributes | Map\<String, Object\> | Disclosed attribute values | O | |
| holderDid | String | Holder's DID | O | Present only if disclosed |

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
| nonce | String | Holder-generated nonce | M | |
| curve | String | Key curve | M | e.g. `secp256r1` |
| publicKey | String | Holder's ephemeral public key | M | Multibase encoded |
| cipher | String | Cipher algorithm | M | e.g. `AES-256-CBC` |
| padding | String | Padding scheme | M | e.g. `PKCS5` |
