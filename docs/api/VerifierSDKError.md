# Verifier SDK — Error Codes

> **Version**: 2.0.0  
> **Enum**: `org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode`  
> **Exception**: `org.omnione.did.verifier.v1.exception.VerifierSdkException`

All SDK errors are thrown as `VerifierSdkException`, which wraps a `VerifierSdkErrorCode`. Error codes use the prefix **`SSDKVRF`**.

---

## Error Code Structure

```
SSDKVRF  NNN  XXX
  │       │    │
  │       │    └─ Sequence number (3 digits)
  │       └────── Category (3 digits)
  └────────────── SDK prefix
```

| Category | Range | Description |
|----------|-------|-------------|
| Validation | `001xxx` | Input / format validation errors |
| Protocol | `002xxx` | Protocol flow and transaction errors |
| Cryptography | `003xxx` | Encryption, signature, E2E errors |
| Storage / Blockchain | `004xxx` | DID resolution, VC metadata, storage errors |
| Configuration | `005xxx` | Policy and provider configuration errors |
| Provider | `006xxx` | SPI provider lookup errors |
| General | `009xxx` | Unexpected / unknown errors |

---

## 1. Validation Errors (`001xxx`)

| Code | Constant | HTTP | Message |
|------|----------|------|---------|
| `SSDKVRF001001` | `SDK_INVALID_DID` | 400 | Invalid DID format. |
| `SSDKVRF001002` | `SDK_INVALID_PROOF` | 400 | Invalid proof. |
| `SSDKVRF001003` | `SDK_INVALID_SIGNATURE` | 400 | Invalid signature. |
| `SSDKVRF001004` | `SDK_INVALID_MULTIBASE` | 400 | Invalid multibase encoding. |
| `SSDKVRF001005` | `SDK_FIELD_REQUIRED` | 400 | Required field is missing. |
| `SSDKVRF001006` | `SDK_INVALID_VP` | 400 | Invalid VP format or verification failed. |
| `SSDKVRF001007` | `SDK_INVALID_VC` | 400 | Invalid VC format or verification failed. |
| `SSDKVRF001008` | `SDK_INVALID_NONCE` | 400 | Invalid nonce. |
| `SSDKVRF001009` | `SDK_INVALID_FILTER` | 400 | Invalid filter configuration. |
| `SSDKVRF001010` | `SDK_UNAUTHORIZED_CLAIM` | 400 | Unauthorized claim detected. |
| `SSDKVRF001011` | `SDK_INVALID_AUTH_TYPE` | 400 | Invalid authentication type. |

---

## 2. Protocol Errors (`002xxx`)

| Code | Constant | HTTP | Message |
|------|----------|------|---------|
| `SSDKVRF002001` | `SDK_INVALID_PROTOCOL_STEP` | 400 | Invalid protocol step. |
| `SSDKVRF002002` | `SDK_INVALID_TRANSACTION_STATUS` | 400 | Invalid transaction status. |
| `SSDKVRF002003` | `SDK_INVALID_SUB_TRANSACTION_STATUS` | 400 | Invalid sub-transaction status. |
| `SSDKVRF002004` | `SDK_TRANSACTION_EXPIRED` | 400 | Transaction has expired. |
| `SSDKVRF002005` | `SDK_TRANSACTION_NOT_FOUND` | 404 | Transaction not found. |
| `SSDKVRF002006` | `SDK_INVALID_OFFER` | 400 | Invalid offer payload. |
| `SSDKVRF002007` | `SDK_INVALID_PROFILE` | 400 | Invalid verification profile. |
| `SSDKVRF002008` | `SDK_POLICY_NOT_FOUND` | 404 | Policy not found. |

---

## 3. Cryptography Errors (`003xxx`)

| Code | Constant | HTTP | Message |
|------|----------|------|---------|
| `SSDKVRF003001` | `SDK_SIGNATURE_GENERATION_FAILED` | 500 | Failed to generate signature. |
| `SSDKVRF003002` | `SDK_SIGNATURE_VERIFICATION_FAILED` | 400 | Failed to verify signature. |
| `SSDKVRF003003` | `SDK_ECDH_KEY_AGREEMENT_FAILED` | 500 | ECDH key agreement failed. |
| `SSDKVRF003004` | `SDK_ENCRYPTION_FAILED` | 500 | Encryption failed. |
| `SSDKVRF003005` | `SDK_DECRYPTION_FAILED` | 500 | Decryption failed. |
| `SSDKVRF003006` | `SDK_E2E_SESSION_NOT_FOUND` | 404 | E2E session not found. |
| `SSDKVRF003007` | `SDK_KEY_GENERATION_FAILED` | 500 | Failed to generate key. |
| `SSDKVRF003008` | `SDK_NONCE_GENERATION_FAILED` | 500 | Failed to generate nonce. |

---

## 4. Storage / Blockchain Errors (`004xxx`)

| Code | Constant | HTTP | Message |
|------|----------|------|---------|
| `SSDKVRF004001` | `SDK_DID_DOCUMENT_NOT_FOUND` | 404 | DID Document not found. |
| `SSDKVRF004002` | `SDK_DID_DOCUMENT_REGISTRATION_FAILED` | 500 | Failed to register DID Document. |
| `SSDKVRF004003` | `SDK_VC_META_NOT_FOUND` | 404 | VC Metadata not found. |
| `SSDKVRF004004` | `SDK_BLOCKCHAIN_CONNECTION_FAILED` | 500 | Blockchain connection failed. |
| `SSDKVRF004005` | `SDK_STORAGE_ERROR` | 500 | Storage access error. |
| `SSDKVRF004006` | `SDK_ZKP_CREDENTIAL_NOT_FOUND` | 404 | ZKP Credential Schema not found. |
| `SSDKVRF004007` | `SDK_ZKP_CREDENTIAL_DEF_NOT_FOUND` | 404 | ZKP Credential Definition not found. |

---

## 5. Configuration Errors (`005xxx`)

| Code | Constant | HTTP | Message |
|------|----------|------|---------|
| `SSDKVRF005001` | `SDK_CONFIGURATION_ERROR` | 500 | Configuration error. |
| `SSDKVRF005002` | `SDK_INVALID_POLICY_CONFIGURATION` | 500 | Invalid policy configuration. |
| `SSDKVRF005003` | `SDK_INVALID_PROFILE_CONFIGURATION` | 500 | Invalid profile configuration. |
| `SSDKVRF005004` | `SDK_MISSING_VERIFIER_INFO` | 500 | Verifier information not found. |

---

## 6. Provider Errors (`006xxx`)

| Code | Constant | HTTP | Message |
|------|----------|------|---------|
| `SSDKVRF006001` | `SDK_VERIFIER_INFO_NOT_FOUND` | 404 | Verifier information not found. |
| `SSDKVRF006002` | `SDK_CONFIG_NOT_FOUND` | 404 | Configuration not found. |
| `SSDKVRF006003` | `SDK_SESSION_NOT_FOUND` | 404 | Session not found. |

---

## 7. General Errors (`009xxx`)

| Code | Constant | HTTP | Message |
|------|----------|------|---------|
| `SSDKVRF009999` | `SDK_UNKNOWN_ERROR` | 500 | Unknown SDK error occurred. |

---

## Handling Exceptions

```java
try {
    VerificationConfirmResult result = verifierService.confirmVerification(txId, vpJson, true);
} catch (VerifierSdkException e) {
    String code = e.getErrorCode().getCode();         // e.g. "SSDKVRF001006"
    String message = e.getErrorCode().getMessage();   // e.g. "Invalid VP format or verification failed."
    int httpStatus = e.getErrorCode().getHttpStatus(); // e.g. 400

    // Map to application-level error response
    throw new ApplicationException(code, message, httpStatus);
}
```

---

## Programmatic Lookup

```java
// Look up a message by error code string
String message = VerifierSdkErrorCode.getMessageByCode("SSDKVRF001006");
// → "Invalid VP format or verification failed."
```
