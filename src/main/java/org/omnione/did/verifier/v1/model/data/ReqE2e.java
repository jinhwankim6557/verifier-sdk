package org.omnione.did.verifier.v1.model.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E2E 암호화 요청 정보 DTO
 * Verifier → Holder 간 E2E 암호화를 위한 키 교환 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqE2e {
    /**
     * E2E 대칭키 생성용 nonce
     * - Base64 인코딩된 16바이트
     */
    @SerializedName("nonce")
    private String nonce;

    /**
     * ECC Curve Type
     * - "Secp256k1" 또는 "Secp256r1"
     */
    @SerializedName("curve")
    private String curve;

    /**
     * E2E 암호화용 Verifier 공개키
     * - multibase 인코딩
     */
    @SerializedName("publicKey")
    private String publicKey;

    /**
     * 대칭키 암호화 알고리즘
     * - "AES-128-CBC", "AES-256-CBC" 등
     */
    @SerializedName("cipher")
    private String cipher;

    /**
     * 패딩 방식
     * - "NOPAD", "PKCS5"
     */
    @SerializedName("padding")
    private String padding;

}
