package org.omnione.did.verifier.v1.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.omnione.did.verifier.v1.model.data.ReqE2e;

/**
 * Proof Request Profile 요청 DTO
 * ProofRequestProfile 생성을 위한 요청 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofRequestProfileRequest {
    /**
     * Transaction ID
     * - VP 제출 거래 식별자
     */
    private String txId;

    /**
     * Policy ID
     * - ZKP Policy 식별자
     */
    private String policyId;

    /**
     * Profile ID (UUID)
     * - 생성될 ProofRequestProfile의 고유 식별자
     */
    private String profileId;

    /**
     * E2E 암호화 요청 정보
     * - Curve (예: "Secp256r1")
     * - Cipher (예: "AES-256-CBC")
     * - Padding (예: "PKCS5")
     */
    private ReqE2e reqE2e;

    /**
     * Verifier DID
     * - Proof 서명에 사용할 Verifier의 DID
     */
    private String verifierDid;
}
