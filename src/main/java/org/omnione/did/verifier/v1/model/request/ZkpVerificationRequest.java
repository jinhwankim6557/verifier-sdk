package org.omnione.did.verifier.v1.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.omnione.did.verifier.v1.model.data.ProofVerifyParam;

import java.math.BigInteger;

/**
 * ZKP Proof 검증 요청 DTO
 * Holder로부터 제출받은 ZKP Proof를 검증하기 위한 요청 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZkpVerificationRequest {
    /**
     * Transaction ID
     * - ZKP Proof 제출 거래 식별자
     */
    private String txId;

    /**
     * 암호화된 ZKP Proof (Multibase 인코딩)
     * - E2E 암호화된 Proof
     */
    private String encProof;

    /**
     * Initial Vector (Multibase 인코딩)
     * - E2E 복호화에 사용
     */
    private String iv;

    /**
     * AccE2e (Holder→Verifier 암호화 정보)
     * - publicKey: Holder 공개키 (Multibase 인코딩)
     * - iv: Initial Vector
     * - proof: AccE2e 서명 (Optional)
     */
    private AccE2e accE2e;

    /**
     * Proof Nonce (Base10)
     * - Holder가 제출한 Nonce
     */
    private String nonce;

    /**
     * Proof Request (from Profile)
     * - ProofRequestProfile에 포함된 ProofRequest
     * - SDK에서 검증 시 사용
     */
    private Object proofRequest;  // org.omnione.did.zkp.datamodel.proofrequest.ProofRequest

    /**
     * Proof Verify Parameters
     * - CredentialSchema, CredentialDefinition 정보
     */
    private java.util.List<ProofVerifyParam> proofVerifyParams;

    /**
     * AccE2e DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccE2e {
        /**
         * Holder 공개키 (Multibase 인코딩)
         */
        private String publicKey;

        /**
         * Initial Vector (Multibase 인코딩)
         */
        private String iv;

        /**
         * AccE2e Proof (Optional)
         * - Holder의 서명
         */
        private Object proof;
    }
}
