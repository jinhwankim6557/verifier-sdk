package org.omnione.did.verifier.v1.model.enums;

/**
 * Offer Type
 *
 * 검증 요청의 타입을 정의합니다.
 *
 * - IssueOffer: VC 발급 요청
 * - VerifyOffer: VP 검증 요청 (일반)
 * - VerifyProofOffer: ZKP Proof 검증 요청
 */
public enum OfferType {
    /**
     * VC 발급 요청
     */
    IssueOffer,

    /**
     * VP 검증 요청 (일반)
     */
    VerifyOffer,

    /**
     * ZKP Proof 검증 요청
     */
    VerifyProofOffer
}
