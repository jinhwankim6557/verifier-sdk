package org.omnione.did.verifier.v1.provider;

/**
 * Nonce 생성기 인터페이스
 * 
 * 목적:
 * - Verifier Nonce, E2E Nonce 등 생성
 * - 보안 난수 생성
 * 
 * 구현 예시:
 * - SecureRandomNonceGenerator: SecureRandom 사용
 * - CustomNonceGenerator: 사용자 정의 알고리즘
 * 
 * 중요:
 * - 암호학적으로 안전한 난수 생성 필요
 */
public interface NonceGenerator {
    
    /**
     * Base64 인코딩된 Nonce 생성
     * 
     * @param length 바이트 길이 (기본값: 16)
     * @return Base64 인코딩된 Nonce
     */
    String generateNonce(int length);
    
    /**
     * 기본 길이(16바이트)의 Nonce 생성
     * 
     * @return Base64 인코딩된 Nonce
     */
    default String generateNonce() {
        return generateNonce(16);
    }
}
