package org.omnione.did.verifier.v1.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ZKP 검증 결과 DTO
 * ZKP Proof 검증 완료 후 반환되는 결과 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZkpVerificationResult {
    /**
     * Transaction ID
     */
    private String txId;

    /**
     * 검증 성공 여부
     */
    private boolean verified;

    /**
     * 검증 실패 사유 (Optional)
     * - verified = false인 경우 설정
     */
    private String errorMessage;

    /**
     * 제출된 Attributes (Key-Value)
     * - Revealed Attributes (공개 속성)
     * - 예: {"name": "홍길동", "birthdate": "19900101"}
     */
    private Map<String, String> revealedAttributes;

    /**
     * 검증된 Predicates
     * - ZKP로 증명된 조건들
     * - 예: [{"type": ">=", "name": "age", "value": 19}]
     */
    private java.util.List<VerifiedPredicate> verifiedPredicates;

    /**
     * Holder DID (Optional)
     * - ZKP는 익명성 보장으로 DID가 없을 수 있음
     */
    private String holderDid;

    /**
     * Proof JSON (Optional)
     * - 검증된 원본 Proof (디버깅/감사용)
     */
    private String proofJson;

    /**
     * 검증 시간 (Unix timestamp)
     */
    private long verifiedAt;

    /**
     * Verified Predicate DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifiedPredicate {
        /**
         * Predicate 타입
         * - ">=", "<=", ">", "<"
         */
        private String type;

        /**
         * Attribute 이름
         */
        private String name;

        /**
         * 비교 값
         */
        private String value;

        /**
         * 검증 성공 여부
         */
        private boolean verified;
    }
}
