# Verifier SDK — Server API Reference

> **Version**: 2.0.0  
> **Package**: `org.omnione.did.verifier.v1`

This document describes every public method exposed by `VerifierService`, the SDK facade interface. All methods are accessed through an instance obtained from `VerifierSdkBuilder`.

---

## Table of Contents

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
2. [SPI Interfaces](#2-spi-interfaces)
   - [VerificationConfigProvider](#21-verificationconfigprovider)
   - [VerifierInfoProvider](#22-verifierinfoprovider)
   - [EcdhSessionProvider](#23-ecdhsessionprovider)
   - [StorageProvider](#24-storageprovider)
   - [TransactionProvider](#25-transactionprovider)
   - [NonceGenerator](#26-noncegenerator)
   - [CryptoHelper](#27-cryptohelper)
3. [Data Models](#3-data-models)

---

## 1. VerifierService (Facade)

**Interface**: `org.omnione.did.verifier.v1.protocol.VerifierService`  
**Default Implementation**: `org.omnione.did.verifier.v1.core.VerifierServiceImpl`

Entry point for all VP and ZKP verification operations. Obtain an instance via `VerifierSdkBuilder`:

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

Generates a VP Offer payload for a **dynamic QR code** flow.

```java
VpOfferPayload requestVpOffer(
    String policyId,
    String device,
    String service,
    boolean locked
)
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `policyId` | `String` | ✅ | Verification policy identifier |
| `device` | `String` | ✅ | Terminal/device identifier |
| `service` | `String` | ✅ | Service identifier |
| `locked` | `boolean` | ✅ | Whether the offer is locked to a specific holder |

**Returns**: `VpOfferPayload` — includes `offerId` for the dynamic QR flow.

**Throws**: `VerifierSdkException` (`SSDKVRF002008`) if the policy is not found.

---

### 1.2 `requestStaticVpOffer`

Generates a VP Offer payload for a **static QR code** flow. No `offerId` is included.

```java
VpOfferPayload requestStaticVpOffer(
    String policyId,
    String device,
    String service,
    boolean locked
)
```

Parameters are identical to `requestVpOffer`. The returned `VpOfferPayload` does not contain an `offerId`.

---

### 1.3 `requestVerifyProfile`

Creates a `VerifyProfile` describing the verification requirements for a given policy.

```java
VerifyProfile requestVerifyProfile(
    String policyId,
    String profileId,
    ReqE2e reqE2e
)
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `policyId` | `String` | ✅ | Verification policy identifier |
| `profileId` | `String` | ✅ | Unique profile ID (UUID recommended) |
| `reqE2e` | `ReqE2e` | ✅ | Holder's E2E key exchange request |

**Returns**: `VerifyProfile` without a Proof. The calling application must sign the profile before returning it to the holder.

**Throws**: `VerifierSdkException` (`SSDKVRF002008`) if the policy is not found.

---

### 1.4 `verifyPresentation`

Performs the full VP verification pipeline: E2E decryption → signature verification → VC status check.

```java
String verifyPresentation(VpVerificationRequest request)
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `txId` | `String` | ✅ | Transaction ID |
| `vp` | `String` | Conditional | Plain VP JSON (when not encrypted) |
| `encrypted` | `Boolean` | ✅ | `true` if the VP is E2E-encrypted |
| `encVp` | `String` | Conditional | Encrypted VP (when `encrypted=true`) |
| `iv` | `String` | Conditional | AES IV (when `encrypted=true`) |
| `encHolderPublicKey` | `String` | Conditional | Encrypted holder public key (when `encrypted=true`) |
| `verifierNonce` | `String` | ✅ | Nonce included in the profile |
| `requiredAuthType` | `Integer` | Optional | Bitmask of required authentication types |
| `filter` | `Filter` | Optional | Claim filter from the policy |

**Returns**: Verified VP as a JSON string.

**Throws**:
- `SSDKVRF001006` — VP validation failed
- `SSDKVRF001007` — VC validation failed
- `SSDKVRF003005` — Decryption failed
- `SSDKVRF002004` — Transaction expired

---

### 1.5 `decryptVp`

Decrypts an E2E-encrypted VP independently of the full verification flow.

```java
String decryptVp(
    String txId,
    String encHolderPublicKey,
    String encVp,
    String iv
)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `txId` | `String` | Transaction ID for E2E session lookup |
| `encHolderPublicKey` | `String` | Holder's public key encrypted with the verifier's key |
| `encVp` | `String` | AES-256-CBC encrypted VP |
| `iv` | `String` | AES initial vector |

**Returns**: Decrypted VP as a JSON string.

**Throws**: `SSDKVRF003005` if decryption fails.

---

### 1.6 `confirmVerification`

Extracts claims from a verified VP and records the verification result.

**Overload 1** — VP only:

```java
VerificationConfirmResult confirmVerification(
    String txId,
    String vpJson,
    boolean verified
)
```

**Overload 2** — VP or ZKP Proof with explicit offer type:

```java
VerificationConfirmResult confirmVerification(
    String txId,
    String vpOrProofJson,
    boolean verified,
    OfferType offerType
)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `txId` | `String` | Transaction ID |
| `vpJson` / `vpOrProofJson` | `String` | Verified VP or Proof JSON |
| `verified` | `boolean` | Verification success flag |
| `offerType` | `OfferType` | `VerifyOffer` or `VerifyProofOffer` |

**Returns**: `VerificationConfirmResult`

| Field | Type | Description |
|-------|------|-------------|
| `txId` | `String` | Transaction ID |
| `verified` | `Boolean` | Whether verification succeeded |
| `holderDid` | `String` | Holder's DID |
| `submittedVcs` | `Map<String, Object>` | Submitted VCs (VC ID → VC object) |
| `extractedClaims` | `Map<String, Object>` | Extracted claims (code → value) |
| `verifiedAt` | `Instant` | Verification timestamp |
| `errorMessage` | `String` | Failure reason (optional) |
| `errorCode` | `String` | Error code (optional) |

---

### 1.7 `requestZkpProofRequestProfile`

Creates a `ProofRequestProfile` defining the ZKP verification request.

```java
ProofRequestProfile requestZkpProofRequestProfile(
    ProofRequestProfileRequest request,
    ZkpPolicy policy
)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `request` | `ProofRequestProfileRequest` | Profile request metadata |
| `policy` | `ZkpPolicy` | ZKP policy configuration |

**Returns**: `ProofRequestProfile` with Proof included (no additional signing required).

---

### 1.8 `verifyZkpProof`

Verifies a submitted ZKP Proof.

```java
ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request)
```

**Returns**: `ZkpVerificationResult`

| Field | Type | Description |
|-------|------|-------------|
| `verified` | `Boolean` | Whether ZKP proof is valid |
| `revealedAttributes` | `Map<String, Object>` | Disclosed attribute values |
| `holderDid` | `String` | Holder DID (if revealed) |

---

### 1.9 `decryptZkpProof`

Decrypts an E2E-encrypted ZKP Proof.

```java
Proof decryptZkpProof(
    String txId,
    String encProof,
    String iv,
    ZkpVerificationRequest.AccE2e accE2e
)
```

**Returns**: Decrypted `Proof` object (`org.omnione.did.zkp.datamodel.proof.Proof`).

---

## 2. SPI Interfaces

All interfaces are in package `org.omnione.did.verifier.v1.provider`.

### 2.1 `VerificationConfigProvider`

Supplies verification policies and configuration.

```java
public interface VerificationConfigProvider {
    VerificationPolicy getPolicy(String policyId);
    List<String> getAllPolicyIds();
}
```

### 2.2 `VerifierInfoProvider`

Provides verifier identity metadata (DID, keys).

```java
public interface VerifierInfoProvider {
    String getVerifierDid();
    KeyPairInfo getKeyPairInfo(String keyId);
}
```

### 2.3 `EcdhSessionProvider`

Manages E2E encryption session state (ECDH key pairs).

```java
public interface EcdhSessionProvider {
    void saveSession(String txId, KeyPairInfo keyPair);
    KeyPairInfo getSession(String txId);
    void deleteSession(String txId);
}
```

### 2.4 `StorageProvider`

Persists VP and VC data.

```java
public interface StorageProvider {
    void saveVp(String txId, String vpJson);
    String getVp(String txId);
    void saveVcMeta(String vcId, Object vcMeta);
    Object getVcMeta(String vcId);
}
```

### 2.5 `TransactionProvider`

Manages transaction lifecycle.

```java
public interface TransactionProvider {
    void createTransaction(String txId, String policyId, OfferType offerType);
    TransactionInfo getTransaction(String txId);
    void updateTransactionStatus(String txId, TransactionStatus status);
    void expireTransaction(String txId);
}
```

### 2.6 `NonceGenerator`

Generates cryptographic nonces.

```java
public interface NonceGenerator {
    String generate();
}
```

### 2.7 `CryptoHelper`

Provides cryptographic utilities (signatures, ECDH).

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

## 3. Data Models

### VpOfferPayload

| Field | Type | Description |
|-------|------|-------------|
| `offerId` | `String` | Offer ID (dynamic QR only) |
| `type` | `OfferType` | `VerifyOffer` \| `VerifyProofOffer` |
| `mode` | `PresentMode` | `Direct` \| `Indirect` \| `Proxy` |
| `device` | `String` | Device identifier |
| `service` | `String` | Service identifier |
| `expiresAt` | `Instant` | Offer expiration time |

### ReqE2e

| Field | Type | Description |
|-------|------|-------------|
| `nonce` | `String` | Holder-generated nonce |
| `curve` | `String` | Key curve (e.g. `secp256r1`) |
| `publicKey` | `String` | Holder's ephemeral public key (multibase) |
| `cipher` | `String` | Cipher algorithm (e.g. `AES-256-CBC`) |
| `padding` | `String` | Padding scheme (e.g. `PKCS5`) |

### OfferType

| Value | Description |
|-------|-------------|
| `VerifyOffer` | Standard VP verification |
| `VerifyProofOffer` | ZKP proof verification |
