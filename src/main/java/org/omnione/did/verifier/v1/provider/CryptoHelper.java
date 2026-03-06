package org.omnione.did.verifier.v1.provider;

import org.omnione.did.verifier.v1.model.data.KeyPairInfo;

/**
 * 암호화 헬퍼 인터페이스
 *
 * 목적:
 * - VP 검증을 위한 암호화 연산 (서명 검증, 해시 생성)
 * - E2E 암호화를 위한 키 생성 및 암호화/복호화
 * - DID Document 검증
 *
 * 구현 예시:
 * - CryptoHelperAdapter: did-crypto-sdk-server 사용
 * - CustomCryptoHelper: 사용자 정의 암호화 라이브러리
 *
 * Phase 1: 서명 검증 (VP/VC 검증용)
 * Phase 2-1: E2E 키 생성 및 암호화 (E2E 세션용)
 */
public interface CryptoHelper {
    
    /**
     * 서명 검증
     * 
     * @param publicKey 공개키 (Base64 또는 Multibase)
     * @param signature 서명 (Base64 또는 Multibase)
     * @param data 원본 데이터
     * @return true: 검증 성공, false: 검증 실패
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 검증 실패 시
     */
    boolean verifySignature(String publicKey, String signature, byte[] data);
    
    /**
     * SHA-256 해시 생성
     * 
     * @param data 원본 데이터
     * @return 해시값 (Base64)
     */
    String sha256(byte[] data);
    
    /**
     * Multibase 디코딩
     * 
     * @param multibase Multibase 인코딩된 문자열
     * @return 디코딩된 바이트 배열
     */
    byte[] decodeMultibase(String multibase);
    
    /**
     * Base64 인코딩
     * 
     * @param data 원본 데이터
     * @return Base64 인코딩된 문자열
     */
    String encodeBase64(byte[] data);
    
    /**
     * Base64 디코딩
     *
     * @param base64 Base64 인코딩된 문자열
     * @return 디코딩된 바이트 배열
     */
    byte[] decodeBase64(String base64);

    // ========================================================================
    // Phase 2-1: E2E 암호화 지원
    // ========================================================================

    /**
     * E2E 암호화용 키쌍 생성
     *
     * ECDH 프로토콜에 사용할 타원곡선 키쌍을 생성합니다.
     *
     * @param curve 타원곡선 타입 ("Secp256r1", "Secp256k1")
     * @return 생성된 키쌍 (publicKey, privateKey 모두 Multibase 인코딩)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 키 생성 실패 시
     */
    KeyPairInfo generateKeyPair(String curve);

    /**
     * Nonce 생성
     *
     * E2E 세션키 생성에 사용할 랜덤 Nonce를 생성합니다.
     *
     * @param length 바이트 길이 (일반적으로 16 bytes)
     * @return Nonce (Multibase 인코딩)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException Nonce 생성 실패 시
     */
    String generateNonce(int length);

    /**
     * Multibase 인코딩
     *
     * 바이트 배열을 Multibase 형식으로 인코딩합니다.
     *
     * @param data 원본 데이터
     * @return Multibase 인코딩된 문자열
     */
    String encodeMultibase(byte[] data);

    /**
     * ECDH 공유 비밀키 생성
     *
     * Holder의 공개키와 Verifier의 개인키로 공유 비밀키를 생성합니다.
     *
     * @param holderPublicKey Holder 공개키 (압축되지 않은 형식)
     * @param verifierPrivateKey Verifier 개인키
     * @param curve 타원곡선 타입 ("Secp256r1", "Secp256k1")
     * @return 공유 비밀키
     * @throws org.omnione.did.verifier.v1.exception.VerifierException ECDH 실패 시
     */
    byte[] generateSharedSecret(byte[] holderPublicKey, byte[] verifierPrivateKey, String curve);

    /**
     * KDF (Key Derivation Function) - 세션키 생성
     *
     * 공유 비밀키와 Nonce를 결합하여 실제 암호화에 사용할 세션키를 생성합니다.
     *
     * @param sharedSecret 공유 비밀키
     * @param nonce Nonce
     * @param cipherType 암호화 타입 ("AES-256-CBC", "AES-128-CBC")
     * @return 세션키 (대칭키)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException KDF 실패 시
     */
    byte[] deriveSessionKey(byte[] sharedSecret, byte[] nonce, String cipherType);

    /**
     * 대칭키 복호화
     *
     * AES 등 대칭키 알고리즘으로 암호화된 데이터를 복호화합니다.
     *
     * @param encData 암호화된 데이터
     * @param sessionKey 세션키
     * @param iv Initial Vector
     * @param cipherType 암호화 타입 ("AES-256-CBC", "AES-128-CBC")
     * @param paddingType 패딩 타입 ("PKCS5", "PKCS7")
     * @return 복호화된 데이터
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 복호화 실패 시
     */
    byte[] decrypt(byte[] encData, byte[] sessionKey, byte[] iv, String cipherType, String paddingType);

    // ========================================================================
    // ZKP 검증 지원
    // ========================================================================

    /**
     * EC 키쌍 생성 (ZKP용)
     *
     * ZKP ProofRequestProfile E2E 암호화를 위한 타원곡선 키쌍을 생성합니다.
     *
     * @param curve 타원곡선 타입 ("Secp256r1", "Secp256k1")
     * @return 생성된 키쌍 (publicKey, privateKey, curve 포함)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 키 생성 실패 시
     */
    KeyPairInfo generateEcKeyPair(String curve);
}
