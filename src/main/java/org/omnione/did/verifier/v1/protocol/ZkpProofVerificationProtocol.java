/*
 * Copyright 2025 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnione.did.verifier.v1.protocol;

import org.omnione.did.verifier.v1.model.data.ProofRequestProfile;
import org.omnione.did.verifier.v1.model.request.ProofRequestProfileRequest;
import org.omnione.did.verifier.v1.model.policy.ZkpPolicy;
import org.omnione.did.verifier.v1.model.request.ZkpVerificationRequest;
import org.omnione.did.verifier.v1.model.response.ZkpVerificationResult;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.zkp.datamodel.proof.Proof;

/**
 * ZKP Proof Verification Service Interface
 *
 * ZKP Proof 검증을 담당합니다.
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>ProofRequestProfile 생성</li>
 *   <li>ZKP Proof 검증</li>
 *   <li>ZKP Proof 복호화</li>
 * </ul>
 *
 * <h2>기본 구현체</h2>
 * {@link org.omnione.did.verifier.v1.service.DefaultZkpProofVerificationService}
 *
 * @since 2.0.0
 */
public interface ZkpProofVerificationProtocol {

    /**
     * ProofRequestProfile 생성
     *
     * ZKP Proof 요청을 위한 Profile을 생성합니다.
     * <ul>
     *   <li>ProofRequest 초기화 (Nonce 생성)</li>
     *   <li>E2E 암호화 설정</li>
     *   <li>Profile 서명 생성</li>
     * </ul>
     *
     * @param request ProofRequestProfile 요청
     * @param policy ZKP Policy (ProofRequest 템플릿 포함)
     * @return ProofRequestProfile (Proof 포함)
     * @throws VerifierSdkException Profile 생성 실패
     */
    ProofRequestProfile requestZkpProofRequestProfile(
        ProofRequestProfileRequest request,
        ZkpPolicy policy
    );

    /**
     * ZKP Proof 검증 (전체 프로세스)
     *
     * did-zkp-sdk의 ZkpProofManager를 통해 다음 검증을 수행합니다:
     * <ul>
     *   <li>Proof 서명 검증</li>
     *   <li>Revealed Attributes 검증</li>
     *   <li>ZK Proof 검증 (Predicates)</li>
     *   <li>ProofRequest 일치 검증</li>
     * </ul>
     *
     * @param request ZKP 검증 요청
     * @return 검증 결과
     * @throws VerifierSdkException ZKP 검증 실패
     */
    ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request);

    /**
     * ZKP Proof 복호화
     *
     * @param txId Transaction ID
     * @param encProof 암호화된 Proof (Multibase 인코딩)
     * @param iv Initial Vector (Multibase 인코딩)
     * @param accE2e AccE2e 정보
     * @return 복호화된 Proof
     * @throws VerifierSdkException 복호화 실패
     */
    Proof decryptZkpProof(
        String txId,
        String encProof,
        String iv,
        ZkpVerificationRequest.AccE2e accE2e
    );
}
