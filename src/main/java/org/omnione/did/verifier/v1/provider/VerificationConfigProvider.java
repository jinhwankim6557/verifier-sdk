package org.omnione.did.verifier.v1.provider;

import org.omnione.did.verifier.v1.model.policy.VerificationPolicy;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;

/**
 * Verification 설정 제공자 인터페이스
 * 
 * 목적:
 * - Policy, Profile, Filter, Process 정보를 제공
 * - Admin Server, YAML 파일, 메모리 등 다양한 구현 가능
 * 
 * 구현 예시:
 * - AdminApiConfigProvider: Admin API 호출
 * - YamlFileConfigProvider: YAML 파일 로드
 * - MemoryConfigProvider: 메모리 캐시
 */
public interface VerificationConfigProvider {
    
    /**
     * Policy ID로 Verification Policy 조회
     *
     * @param policyId Policy ID
     * @return VerificationPolicy (Policy + Profile + Filter + Process)
     * @throws VerifierSdkException Policy 미존재 시 (SDK_POLICY_NOT_FOUND) 또는 조회 실패 시 (SDK_CONFIGURATION_ERROR)
     */
    VerificationPolicy getPolicy(String policyId);
    
    /**
     * Policy 존재 여부 확인
     * 
     * @param policyId Policy ID
     * @return true: 존재, false: 미존재
     */
    boolean existsPolicy(String policyId);
}
