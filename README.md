# Verifier SDK

> **버전**: 1.0.0
> **상태**: Production Ready ✅
> **최종 업데이트**: 2026-02-13

DID 기반 VP(Verifiable Presentation) 및 ZKP(Zero-Knowledge Proof) 검증을 위한 Java SDK입니다.

## 개요

Verifier SDK는 OpenDID 생태계에서 VP/ZKP 검증 프로토콜을 구현하기 위한 핵심 라이브러리입니다. SPI(Service Provider Interface) 패턴을 사용하여 다양한 환경에 유연하게 적용할 수 있습니다.

## 주요 기능

### VP 검증
- **VP Offer 생성**: QR 코드 기반 VP 요청 (Dynamic/Static)
- **Verify Profile 생성**: 검증 요구사항 정의
- **VP 검증**: E2E 복호화, 서명 검증, VC 상태 확인
- **검증 확인**: 클레임 추출 및 결과 반환

### ZKP 검증 ✨
- **ProofRequestProfile 생성**: ZKP 검증 요청 정의
- **ZKP Proof 검증**: 영지식 증명 검증 (익명 검증)
- **Revealed Attributes 추출**: 공개된 속성 반환

## 요구사항

- Java 21+
- Spring Boot 3.2.x (권장)

## 설치

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

## 빠른 시작

### 1. 인터페이스 구현

SDK를 사용하려면 7개의 SPI 인터페이스를 구현해야 합니다:

| 인터페이스 | 역할 |
|-----------|------|
| `VerificationConfigProvider` | 검증 정책 제공 |
| `VerifierInfoProvider` | Verifier 메타정보 제공 |
| `E2eSessionProvider` | E2E 암호화 세션 관리 |
| `StorageService` | VP/VC 저장소 |
| `TransactionManager` | 트랜잭션 관리 |
| `NonceGenerator` | Nonce 생성 |
| `CryptoHelper` | 암호화 유틸리티 |

### 2. VerifierService 생성

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

### 3. API 사용

#### VP 검증 예제

```java
@Service
public class MyVerificationService {

    private final VerifierService verifierService;

    // 1. VP Offer 생성
    public VpOfferPayload createOffer(String policyId) {
        return verifierService.createVpOfferPayload(
            policyId, "device-001", "login-service", false
        );
    }

    // 2. Verify Profile 생성
    public VerificationProfile createProfile(String policyId, ReqE2e reqE2e) {
        return verifierService.createVerifyProfile(
            policyId, UUID.randomUUID().toString(), reqE2e
        );
    }

    // 3. VP 검증
    public String verifyVp(VpVerificationRequest request) {
        return verifierService.verifyPresentation(request);
    }

    // 4. 검증 확인
    public VerificationConfirmResult confirm(String txId, String vpJson) {
        return verifierService.confirmVerification(txId, vpJson, true);
    }
}
```

#### ZKP 검증 예제 ✨

```java
@Service
public class MyZkpVerificationService {

    private final VerifierService verifierService;

    // 1. ZKP ProofRequestProfile 생성
    public ProofRequestProfile createZkpProfile(ProofRequestProfileRequest request) {
        return verifierService.createZkpProofRequestProfile(request);
    }

    // 2. ZKP Proof 검증
    public ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request) {
        return verifierService.verifyZkpProof(request);
    }

    // 3. ZKP Proof 복호화
    public String decryptZkpProof(String encProof, String txId, AccE2e accE2e) {
        return verifierService.decryptZkpProof(encProof, txId, accE2e);
    }
}
```

## 문서

상세한 사용 가이드는 다음 문서를 참조하세요:

- [SDK Guide](SDK_GUIDE.md) - 상세 사용 가이드
- [Verifier Architecture](../VERIFIER_ARCHITECTURE.md) - 전체 아키텍처
- [Progress Report](../../../PROGRESS_REPORT.md) - 개발 진행 상황

### 문서 내용

- ✅ 아키텍처 다이어그램
- ✅ VP/ZKP 시퀀스 다이어그램
- ✅ 인터페이스별 상세 구현 가이드
- ✅ DTO 레퍼런스
- ✅ 예외 처리 가이드
- ✅ 통합 테스트 예제 (13개)

## 프로젝트 구조

```
verifier-sdk/
├── src/main/java/org/omnione/did/verifier/v1/
│   ├── api/          # SPI 인터페이스 (7개)
│   │   ├── E2eSessionProvider.java
│   │   ├── StorageService.java
│   │   ├── CryptoHelper.java
│   │   ├── TransactionManager.java
│   │   ├── VerificationConfigProvider.java
│   │   ├── VerifierInfoProvider.java
│   │   └── NonceGenerator.java
│   ├── service/      # 핵심 서비스 (6개)
│   │   ├── VerifierService.java             # Facade
│   │   ├── VpOfferService.java
│   │   ├── VpProfileService.java
│   │   ├── VpVerificationService.java
│   │   ├── VerificationConfirmService.java
│   │   └── ZkpProofVerificationService.java  # ZKP 검증 ✨
│   ├── dto/          # 데이터 전송 객체 (16개)
│   └── exception/    # 예외 클래스 (7개)
├── docs/
│   └── SDK_GUIDE.md  # 상세 가이드
└── build.gradle      # Gradle 빌드 설정
```

### 통계

| 항목 | 수량 |
|------|------|
| Java 파일 | 36개 |
| 코드 라인 | 3,220 lines |
| SPI 인터페이스 | 7개 |
| 핵심 서비스 | 6개 (VP 4개 + ZKP 2개) |
| DTO | 16개 |
| Exception | 7개 |

## 실전 적용 사례

이 SDK는 **OpenDID Verifier Server**에서 프로덕션 환경에 적용되어 있습니다:

- **Application**: `ApplicationVerifierServiceImpl` (기본 구현)
- **Adapters**: 7개 Adapter 구현으로 SDK 연동
- **Test Coverage**: 13개 통합 테스트 100% 통과
- **성능**: VP 검증 약 100-200ms, ZKP 검증 약 300-500ms

```java
// ApplicationVerifierServiceImpl.java 예제
@Service
public class ApplicationVerifierServiceImpl implements ApplicationVerifierService {

    private final VerifierService verifierService;  // SDK Facade

    // SDK를 사용한 VP 검증
    public RequestVerifyResDto requestVerify(RequestVerifyReqDto request) {
        VpVerificationRequest sdkRequest = buildVpVerificationRequest(request);
        String vpJson = verifierService.verifyPresentation(sdkRequest);
        // ...
    }
}
```

## 라이선스

이 프로젝트는 [Apache License 2.0](LICENSE) 하에 배포됩니다.

## 기여

버그 리포트 및 기능 제안은 [GitHub Issues](https://github.com/OmniOneID/did-verifier-server/issues)를 통해 제출해 주세요.

---

**버전**: 1.0.0
**최종 업데이트**: 2026-02-13
**상태**: ✅ Production Ready
