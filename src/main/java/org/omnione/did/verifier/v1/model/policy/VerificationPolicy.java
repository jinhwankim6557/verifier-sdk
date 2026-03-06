package org.omnione.did.verifier.v1.model.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.omnione.did.data.model.profile.Filter;
import org.omnione.did.data.model.profile.verify.VerifyProcess;
import org.omnione.did.data.model.provider.ProviderDetail;

import java.util.List;

/**
 * Verification Policy DTO
 * Policy, Filter, Process, Verifier 정보를 통합한 DTO
 *
 * 설계 원칙:
 * - SDK API 인터페이스는 인프라 개념 없음 (DB 스키마 DTO 금지)
 * - 모든 필드 타입은 Core 라이브러리 타입 사용
 * - SDK 고유 중간 DTO(VerificationProfile, FilterInfo 등) 사용 금지
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationPolicy {
    /**
     * Policy ID
     */
    private String policyId;

    /**
     * Policy 이름 (VerifyProfile의 title로 사용)
     */
    private String policyName;

    /**
     * Policy 설명
     */
    private String description;

    /**
     * Logo 이미지 정보 (Optional)
     */
    private Object logo;

    /**
     * 언어 코드 (예: "ko", "en")
     */
    private String language;

    /**
     * VP 제출 모드 ("Direct", "Indirect")
     */
    private String mode;

    /**
     * API Endpoint 목록
     */
    private List<String> endpoints;

    /**
     * 유효기간 (초 단위)
     */
    private int validityDuration;

    /**
     * Filter 정보 (Core 타입)
     * VP 제출 시 적용할 필터 조건
     */
    private Filter filter;

    /**
     * Process 기본 정보 (Core 타입, reqE2e/verifierNonce 제외)
     * reqE2e와 verifierNonce는 SDK가 런타임에 동적으로 추가
     */
    private VerifyProcess process;

    /**
     * Verifier 정보 (Core 타입)
     */
    private ProviderDetail verifier;
}
