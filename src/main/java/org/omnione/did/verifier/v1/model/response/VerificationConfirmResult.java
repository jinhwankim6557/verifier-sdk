package org.omnione.did.verifier.v1.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * VP 검증 완료 결과 DTO
 * VP 검증이 완료된 후 최종 결과
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationConfirmResult {
    /**
     * Transaction ID
     */
    private String txId;

    /**
     * 검증 성공 여부
     */
    private Boolean verified;

    /**
     * Holder DID
     */
    private String holderDid;

    /**
     * 제출된 VC 목록 (VC ID → VC 객체)
     */
    private Map<String, Object> submittedVcs;

    /**
     * 추출된 Claim 목록 (Claim Code → Claim 값)
     */
    private Map<String, Object> extractedClaims;

    /**
     * 검증 일시
     */
    private Instant verifiedAt;

    /**
     * 오류 메시지 (Optional)
     * - 검증 실패 시 사유
     */
    private String errorMessage;

    /**
     * 오류 코드 (Optional)
     */
    private String errorCode;
}
