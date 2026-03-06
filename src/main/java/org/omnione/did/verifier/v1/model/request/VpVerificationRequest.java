package org.omnione.did.verifier.v1.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.omnione.did.data.model.profile.Filter;

/**
 * VP 검증 요청 DTO
 * Holder로부터 제출받은 VP를 검증하기 위한 요청 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VpVerificationRequest {
    /**
     * 제출된 VP (JSON 문자열 또는 객체)
     */
    private String vp;

    /**
     * E2E 암호화된 VP 여부
     * - true: encVp 필드 사용
     * - false: vp 필드 사용
     */
    @Builder.Default
    private Boolean encrypted = false;

    /**
     * 암호화된 VP (Optional)
     * - encrypted = true인 경우 사용
     */
    private String encVp;

    /**
     * E2E 암호화 IV (Optional)
     * - encrypted = true인 경우 필수
     */
    private String iv;

    /**
     * 암호화된 Holder 공개키 (Optional)
     * - encrypted = true인 경우 필수
     * - Holder가 자신의 공개키를 Verifier 공개키로 암호화한 값
     * - ECDH 공유 비밀키 생성에 사용
     */
    private String encHolderPublicKey;

    /**
     * Transaction ID
     * - VP 제출 거래 식별자
     */
    private String txId;

    /**
     * Server Token (Optional)
     * - 인증 토큰
     */
    private String serverToken;

    /**
     * Verifier Nonce
     * - Profile에 포함된 nonce와 일치해야 함
     */
    private String verifierNonce;

    /**
     * 요구되는 인증 타입 (Optional)
     * - 0x00000000: 제한없음
     * - 0x00000002: PIN
     * - 0x00000004: BIO
     * - 0x00000006: PIN or BIO
     * - 0x00008006: PIN and BIO
     */
    private Integer requiredAuthType;

    /**
     * VP 검증에 적용할 Filter 조건 (Core 타입)
     * - Policy에 정의된 Filter를 Application에서 전달
     * - VpManager에서 스키마, 클레임 등 검증에 사용
     */
    private Filter filter;
}
