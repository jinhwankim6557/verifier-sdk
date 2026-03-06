package org.omnione.did.verifier.v1.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 키쌍 정보 DTO
 *
 * E2E 암호화를 위한 키쌍 정보를 전달합니다.
 * CryptoHelper.generateKeyPair()의 반환 타입으로 사용됩니다.
 *
 * 특징:
 * - publicKey, privateKey 모두 Multibase 인코딩된 문자열
 * - crypto-sdk의 KeyPairInterface를 추상화
 * - 직렬화 가능 (String 기반)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyPairInfo {

    /**
     * 공개키 (Multibase 인코딩)
     *
     * - Verifier → Holder에게 전송
     * - ReqE2e.publicKey로 사용
     */
    private String publicKey;

    /**
     * 개인키 (Multibase 인코딩)
     *
     * - Verifier만 보관 (DB 저장)
     * - E2e.sessionKey로 저장
     * - VP 복호화 시 사용
     */
    private String privateKey;
}
