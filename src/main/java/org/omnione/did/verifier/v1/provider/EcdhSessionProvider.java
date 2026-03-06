package org.omnione.did.verifier.v1.provider;

import org.omnione.did.verifier.v1.model.data.KeyPairInfo;
import org.omnione.did.verifier.v1.model.data.ReqE2e;

/**
 * E2E 세션 제공자 인터페이스
 * 
 * 목적:
 * - E2E 암호화에 필요한 키 쌍 및 nonce 생성
 * - E2E 세션 저장/조회
 * 
 * 구현 예시:
 * - InMemoryE2eSessionProvider: 메모리에 세션 저장
 * - RedisE2eSessionProvider: Redis에 세션 저장
 * - DatabaseE2eSessionProvider: DB에 세션 저장
 * 
 * 중요:
 * - SDK는 E2E 세션을 저장하지 않음
 * - Application이 세션을 관리하고 SDK에 제공
 */
public interface EcdhSessionProvider {
    
    /**
     * 새로운 E2E 세션 생성
     *
     * @param txId Transaction ID
     * @return ReqE2e (nonce, curve, publicKey, cipher, padding)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 생성 실패 시
     */
    ReqE2e createSession(String txId);

    /**
     * E2E 세션 저장 (ZKP용)
     *
     * ZKP ProofRequestProfile 생성 시 E2E 키쌍과 설정을 저장합니다.
     *
     * @param txId Transaction ID
     * @param keyPair E2E 키쌍 (publicKey, privateKey, curve)
     * @param reqE2e E2E 암호화 설정 (nonce, cipher, padding)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 저장 실패 시
     */
    void saveSession(String txId, KeyPairInfo keyPair, ReqE2e reqE2e);
    
    /**
     * E2E 세션 조회
     * 
     * @param txId Transaction ID
     * @return ReqE2e (nonce, curve, publicKey, cipher, padding)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 조회 실패 시
     */
    ReqE2e getSession(String txId);
    
    /**
     * E2E 세션 삭제
     * 
     * @param txId Transaction ID
     */
    void removeSession(String txId);
    
    /**
     * E2E 세션 존재 여부 확인
     * 
     * @param txId Transaction ID
     * @return true: 존재, false: 미존재
     */
    boolean existsSession(String txId);
    
    /**
     * 암호화된 데이터 복호화 (ECDH 기반 E2E 복호화)
     *
     * E2E 복호화 프로토콜:
     * 1. Holder 공개키 추출 (Multibase 디코딩)
     * 2. ECDH: Verifier 개인키 + Holder 공개키 → 공유 비밀키 생성
     * 3. 세션키 = KDF(공유 비밀키 + nonce)
     * 4. 평문 = Decrypt(encVp, 세션키, iv)
     *
     * 주의: encHolderPublicKey는 암호화되지 않은 공개키입니다 (Multibase 인코딩만)
     *
     * @param txId Transaction ID
     * @param encHolderPublicKey Holder 공개키 (Multibase 인코딩)
     * @param encVp 암호화된 VP (Multibase 인코딩)
     * @param iv IV (Multibase 인코딩)
     * @return 복호화된 VP (JSON 문자열)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 복호화 실패 시
     */
    String decrypt(String txId, String encHolderPublicKey, String encVp, String iv);
}
