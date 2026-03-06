package org.omnione.did.verifier.v1.provider;

/**
 * Transaction 관리자 인터페이스
 *
 * 목적:
 * - Transaction ID 생성 및 조회
 *
 * 중요:
 * - SDK는 Transaction을 저장하지 않음
 * - Application이 DB를 통해 Transaction을 관리
 */
public interface TransactionProvider {

    /**
     * 새로운 Transaction ID 생성
     *
     * @return Transaction ID (UUID 권장)
     */
    String createTransactionId();

    /**
     * Transaction ID (Long) 조회
     *
     * txId (UUID 문자열)로 DB의 Transaction PK (Long)를 조회
     * E2E 세션 등 다른 테이블과의 연결에 필요
     *
     * @param txId Transaction ID (UUID 문자열)
     * @return Transaction PK (Long)
     * @throws org.omnione.did.verifier.v1.exception.VerifierException 조회 실패 시
     */
    Long getTransactionId(String txId);
}
