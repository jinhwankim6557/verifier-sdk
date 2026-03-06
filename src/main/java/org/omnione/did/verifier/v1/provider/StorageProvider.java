package org.omnione.did.verifier.v1.provider;

import org.omnione.did.verifier.v1.exception.VerifierSdkException;

/**
 * 저장소 서비스 인터페이스
 * 
 * 목적:
 * - DID Document, VC Meta 등 프로토콜 검증에 필요한 데이터 조회
 * - Blockchain, Database, LSS 등 다양한 저장소 구현 가능
 * 
 * 구현 예시:
 * - BlockchainStorageService: 블록체인에서 조회
 * - DatabaseStorageService: PostgreSQL/Oracle에서 조회
 * - MockStorageService: 테스트용 Mock 데이터
 * 
 * 중요:
 * - Policy/Filter는 포함하지 않음 (VerificationConfigProvider에서 제공)
 * - DID Document, VC Meta 등 프로토콜 검증에 필요한 정보만 제공
 */
public interface StorageProvider {
    
    /**
     * DID로 DID Document 조회
     *
     * @param did DID
     * @return DID Document (JSON 문자열)
     * @throws VerifierSdkException DID Document 미존재 또는 조회 실패 시 (SDK_STORAGE_ERROR, SDK_DID_DOCUMENT_NOT_FOUND 등)
     */
    String findDidDocument(String did);
    
    /**
     * VC ID로 VC Meta 조회
     *
     * @param vcId VC ID
     * @return VC Meta 정보 (JSON 문자열)
     * @throws VerifierSdkException 조회 실패 시 (SDK_STORAGE_ERROR)
     */
    String getVcMeta(String vcId);
    
    /**
     * DID Document 존재 여부 확인
     *
     * @param did DID
     * @return true: 존재, false: 미존재
     */
    boolean existsDidDocument(String did);

    // ========================================================================
    // ZKP 검증 지원
    // ========================================================================

    /**
     * ZKP Credential Schema 조회
     *
     * @param schemaId Schema ID
     * @return org.omnione.did.zkp.datamodel.schema.CredentialSchema
     * @throws VerifierSdkException 조회 실패 시 (SDK_STORAGE_ERROR)
     */
    org.omnione.did.zkp.datamodel.schema.CredentialSchema getZKPCredential(String schemaId);

    /**
     * ZKP Credential Definition 조회
     *
     * @param credDefId Credential Definition ID
     * @return org.omnione.did.zkp.datamodel.definition.CredentialDefinition
     * @throws VerifierSdkException 조회 실패 시 (SDK_STORAGE_ERROR)
     */
    org.omnione.did.zkp.datamodel.definition.CredentialDefinition getZKPCredentialDefinition(String credDefId);
}
