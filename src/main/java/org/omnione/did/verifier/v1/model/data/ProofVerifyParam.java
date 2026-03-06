package org.omnione.did.verifier.v1.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ZKP Proof 검증 파라미터 DTO
 * CredentialSchema와 CredentialDefinition 정보를 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofVerifyParam {
    /**
     * Credential Schema
     * - org.omnione.did.zkp.datamodel.schema.CredentialSchema
     */
    private Object credentialSchema;

    /**
     * Credential Definition
     * - org.omnione.did.zkp.datamodel.definition.CredentialDefinition
     */
    private Object credentialDefinition;

    /**
     * Schema ID
     * - Identifier.schemaId에 매핑
     */
    private String schemaId;

    /**
     * Credential Definition ID
     * - Identifier.credDefId에 매핑
     */
    private String credDefId;
}
