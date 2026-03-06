package org.omnione.did.verifier.v1.provider;

import org.omnione.did.data.model.provider.ProviderDetail;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;

/**
 * Verifier 정보 제공자 인터페이스
 *
 * 목적:
 * - Verifier의 DID, 이름, 참조 URL 등 기본 정보 제공
 * - DID Document 조회
 *
 * 설계 원칙:
 * - Core 라이브러리 타입(ProviderDetail)을 직접 반환
 * - SDK 고유 DTO 사용 금지
 *
 * 구현 예시:
 * - VerifierInfoProviderAdapter: application.yml에서 로드
 */
public interface VerifierInfoProvider {

    /**
     * Verifier 기본 정보 조회
     *
     * @return ProviderDetail (Core, DID, 이름, certVcRef, 참조 URL 등)
     * @throws VerifierSdkException 조회 실패 시 (SDK_CONFIGURATION_ERROR)
     */
    ProviderDetail getVerifierInfo();

    /**
     * Verifier DID Document 조회
     *
     * @return DID Document (JSON 문자열)
     * @throws VerifierSdkException 조회 실패 시 (SDK_STORAGE_ERROR, SDK_DID_DOCUMENT_NOT_FOUND 등)
     */
    String getVerifierDidDocument();

    /**
     * Verifier DID 조회
     *
     * @return Verifier DID
     */
    String getVerifierDid();
}
