# Verifier SDK

> **Version**: 1.0.0  
> **Status**: Production Ready ✅  
> **Last Updated**: 2026-02-13

A Java SDK for VP (Verifiable Presentation) and ZKP (Zero-Knowledge Proof) verification based on Decentralized Identifiers (DID).

## Overview

Verifier SDK is a core library for implementing VP/ZKP verification protocols within the OpenDID ecosystem. It uses the SPI (Service Provider Interface) pattern to allow flexible integration across different environments.

## Features

### VP Verification
- **VP Offer Generation**: QR code-based VP requests (Dynamic/Static)
- **Verify Profile Generation**: Define verification requirements
- **VP Verification**: E2E decryption, signature verification, VC status check
- **Verification Confirmation**: Claim extraction and result return

### ZKP Verification ✨
- **ProofRequestProfile Generation**: Define ZKP verification requests
- **ZKP Proof Verification**: Zero-knowledge proof verification (anonymous verification)
- **Revealed Attributes Extraction**: Return disclosed attributes

## Requirements

- Java 21+
- Spring Boot 3.2.x (recommended)

## Installation

### Gradle

```groovy
// settings.gradle
includeBuild('verifier-sdk')

// build.gradle
dependencies {
    implementation 'org.omnione.did:verifier-sdk:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>org.omnione.did</groupId>
    <artifactId>verifier-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Implement the SPI Interfaces

To use the SDK, you need to implement 7 SPI interfaces:

| Interface | Role |
|-----------|------|
| `VerificationConfigProvider` | Provides verification policies |
| `VerifierInfoProvider` | Provides Verifier metadata |
| `E2eSessionProvider` | Manages E2E encryption sessions |
| `StorageService` | VP/VC storage |
| `TransactionManager` | Transaction management |
| `NonceGenerator` | Nonce generation |
| `CryptoHelper` | Cryptographic utilities |

### 2. Create VerifierService

```java
@Configuration
public class VerifierSdkConfig {

    @Bean
    public VerifierService verifierService(
            VerificationConfigProvider configProvider,
            VerifierInfoProvider verifierInfoProvider,
            E2eSessionProvider sessionProvider,
            StorageService storageService,
            TransactionManager transactionManager,
            NonceGenerator nonceGenerator,
            CryptoHelper cryptoHelper
    ) {
        return new VerifierService(
            configProvider,
            verifierInfoProvider,
            sessionProvider,
            storageService,
            transactionManager,
            nonceGenerator,
            cryptoHelper
        );
    }
}
```

### 3. Using the API

#### VP Verification Example

```java
@Service
public class MyVerificationService {

    private final VerifierService verifierService;

    // 1. Create VP Offer
    public VpOfferPayload createOffer(String policyId) {
        return verifierService.createVpOfferPayload(
            policyId, "device-001", "login-service", false
        );
    }

    // 2. Create Verify Profile
    public VerificationProfile createProfile(String policyId, ReqE2e reqE2e) {
        return verifierService.createVerifyProfile(
            policyId, UUID.randomUUID().toString(), reqE2e
        );
    }

    // 3. Verify VP
    public String verifyVp(VpVerificationRequest request) {
        return verifierService.verifyPresentation(request);
    }

    // 4. Confirm Verification
    public VerificationConfirmResult confirm(String txId, String vpJson) {
        return verifierService.confirmVerification(txId, vpJson, true);
    }
}
```

#### ZKP Verification Example ✨

```java
@Service
public class MyZkpVerificationService {

    private final VerifierService verifierService;

    // 1. Create ZKP ProofRequestProfile
    public ProofRequestProfile createZkpProfile(ProofRequestProfileRequest request) {
        return verifierService.createZkpProofRequestProfile(request);
    }

    // 2. Verify ZKP Proof
    public ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request) {
        return verifierService.verifyZkpProof(request);
    }

    // 3. Decrypt ZKP Proof
    public String decryptZkpProof(String encProof, String txId, AccE2e accE2e) {
        return verifierService.decryptZkpProof(encProof, txId, accE2e);
    }
}
```

## Documentation

- [SDK Guide (Korean)](docs/api/VERIFIER_SDK-SDK_GUIDE_ko.md)
- [Server API Reference](docs/api/VERIFIER_SDK-SERVER_API.md)
- [Server API Reference (Korean)](docs/api/VERIFIER_SDK-SERVER_API_ko.md)
- [Error Codes](docs/api/VerifierSDKError.md)
- [README (Korean)](README_ko.md)

## Project Structure

```
verifier-sdk/
├── src/main/java/org/omnione/did/verifier/v1/
│   ├── api/          # SPI Interfaces (7)
│   │   ├── E2eSessionProvider.java
│   │   ├── StorageService.java
│   │   ├── CryptoHelper.java
│   │   ├── TransactionManager.java
│   │   ├── VerificationConfigProvider.java
│   │   ├── VerifierInfoProvider.java
│   │   └── NonceGenerator.java
│   ├── service/      # Core Services (6)
│   │   ├── VerifierService.java             # Facade
│   │   ├── VpOfferService.java
│   │   ├── VpProfileService.java
│   │   ├── VpVerificationService.java
│   │   ├── VerificationConfirmService.java
│   │   └── ZkpProofVerificationService.java  # ZKP ✨
│   ├── dto/          # Data Transfer Objects (16)
│   └── exception/    # Exception Classes (7)
├── docs/
│   └── SDK_GUIDE.md
└── build.gradle
```

### Statistics

| Item | Count |
|------|-------|
| Java Files | 36 |
| Lines of Code | 3,220 |
| SPI Interfaces | 7 |
| Core Services | 6 (VP ×4 + ZKP ×2) |
| DTOs | 16 |
| Exceptions | 7 |

## Production Usage

This SDK is used in production within the **OpenDID Verifier Server**:

- **Application**: `ApplicationVerifierServiceImpl` (default implementation)
- **Adapters**: 7 Adapter implementations for SDK integration
- **Test Coverage**: 13 integration tests passing at 100%
- **Performance**: VP verification ~100–200ms, ZKP verification ~300–500ms

```java
// ApplicationVerifierServiceImpl.java example
@Service
public class ApplicationVerifierServiceImpl implements ApplicationVerifierService {

    private final VerifierService verifierService;  // SDK Facade

    public RequestVerifyResDto requestVerify(RequestVerifyReqDto request) {
        VpVerificationRequest sdkRequest = buildVpVerificationRequest(request);
        String vpJson = verifierService.verifyPresentation(sdkRequest);
        // ...
    }
}
```

## License

This project is distributed under the [Apache License 2.0](LICENSE).

## Contributing

Bug reports and feature requests can be submitted via [GitHub Issues](https://github.com/jinhwankim6557/verifier-sdk/issues).

---

**Version**: 1.0.0  
**Last Updated**: 2026-02-13  
**Status**: ✅ Production Ready
