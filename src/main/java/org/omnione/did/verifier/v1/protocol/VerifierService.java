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

import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.verifier.v1.model.enums.*;
import org.omnione.did.verifier.v1.model.policy.*;
import org.omnione.did.verifier.v1.model.request.*;
import org.omnione.did.verifier.v1.model.response.*;
import org.omnione.did.verifier.v1.model.data.*;
import org.omnione.did.zkp.datamodel.proof.Proof;

/**
 * Verifier Service Interface (Facade)
 *
 * VP/ZKP 검증 프로토콜의 통합 진입점입니다.
 * 5개 서비스를 통합하여 사용 편의성을 제공합니다.
 *
 * <h2>통합 서비스</h2>
 * <ul>
 *   <li>{@link VpOfferProtocol}: VP Offer 생성</li>
 *   <li>{@link VpProfileProtocol}: Verify Profile 생성</li>
 *   <li>{@link VpVerificationProtocol}: VP 검증</li>
 *   <li>{@link VerificationConfirmProtocol}: 검증 확인</li>
 *   <li>{@link ZkpProofVerificationProtocol}: ZKP Proof 검증</li>
 * </ul>
 *
 * <h2>기본 구현체</h2>
 * {@link org.omnione.did.verifier.v1.service.DefaultVerifierService}
 *
 * @since 2.0.0
 */
public interface VerifierService {

    // ========================================================================
    // 1. VP Offer 생성
    // ========================================================================

    /**
     * VP Offer Payload 생성 (Dynamic QR용)
     *
     * @param policyId Policy ID
     * @param device 응대장치 식별자
     * @param service 서비스 식별자
     * @param locked Offer 잠김 여부
     * @return VpOfferPayload (offerId 포함)
     */
    VpOfferPayload requestVpOffer(
        String policyId,
        String device,
        String service,
        boolean locked
    );

    /**
     * VP Offer Payload 생성 (Static QR용)
     *
     * @param policyId Policy ID
     * @param device 응대장치 식별자
     * @param service 서비스 식별자
     * @param locked Offer 잠김 여부
     * @return VpOfferPayload (offerId 없음)
     */
    VpOfferPayload requestStaticVpOffer(
        String policyId,
        String device,
        String service,
        boolean locked
    );

    // ========================================================================
    // 2. Verify Profile 생성
    // ========================================================================

    /**
     * Verify Profile 생성
     *
     * @param policyId Policy ID
     * @param profileId Profile ID (UUID 권장)
     * @param reqE2e E2E 암호화 요청 정보
     * @return VerifyProfile (Core, Proof 제외) - Application에서 Proof 추가 필요
     */
    VerifyProfile requestVerifyProfile(
        String policyId,
        String profileId,
        ReqE2e reqE2e
    );

    // ========================================================================
    // 3. VP 검증
    // ========================================================================

    /**
     * VP 검증 (전체 프로세스)
     *
     * @param request VP 검증 요청
     * @return 검증된 VP JSON 문자열
     */
    String verifyPresentation(VpVerificationRequest request);

    /**
     * VP 복호화
     *
     * @param txId Transaction ID
     * @param encHolderPublicKey 암호화된 Holder 공개키
     * @param encVp 암호화된 VP
     * @param iv Initial Vector
     * @return 복호화된 VP JSON 문자열
     */
    String decryptVp(String txId, String encHolderPublicKey, String encVp, String iv);

    // ========================================================================
    // 4. 검증 확인
    // ========================================================================

    /**
     * VP 제출 확인 및 클레임 추출
     *
     * @param txId Transaction ID
     * @param vpJson 검증된 VP JSON 문자열
     * @param verified 검증 성공 여부
     * @return VerificationConfirmResult
     */
    VerificationConfirmResult confirmVerification(
        String txId,
        String vpJson,
        boolean verified
    );

    /**
     * VP/ZKP Proof 제출 확인 및 클레임 추출
     *
     * @param txId Transaction ID
     * @param vpOrProofJson 검증된 VP/Proof JSON 문자열
     * @param verified 검증 성공 여부
     * @param offerType Offer 타입 (VerifyOffer, VerifyProofOffer)
     * @return VerificationConfirmResult
     */
    VerificationConfirmResult confirmVerification(
        String txId,
        String vpOrProofJson,
        boolean verified,
        OfferType offerType
    );

    // ========================================================================
    // 5. ZKP Proof 검증
    // ========================================================================

    /**
     * ZKP ProofRequestProfile 생성
     *
     * @param request ProofRequestProfile 요청
     * @param policy ZKP Policy
     * @return ProofRequestProfile (Proof 포함)
     */
    ProofRequestProfile requestZkpProofRequestProfile(
        ProofRequestProfileRequest request,
        ZkpPolicy policy
    );

    /**
     * ZKP Proof 검증
     *
     * @param request ZKP 검증 요청
     * @return 검증 결과
     */
    ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request);

    /**
     * ZKP Proof 복호화
     *
     * @param txId Transaction ID
     * @param encProof 암호화된 Proof
     * @param iv Initial Vector
     * @param accE2e AccE2e 정보
     * @return 복호화된 Proof
     */
    Proof decryptZkpProof(
        String txId,
        String encProof,
        String iv,
        ZkpVerificationRequest.AccE2e accE2e
    );

    // ========================================================================
    // 개별 서비스 접근
    // ========================================================================

    /**
     * VpOfferProtocol 반환
     *
     * @return VpOfferProtocol
     */
    VpOfferProtocol getOfferService();

    /**
     * VpProfileProtocol 반환
     *
     * @return VpProfileProtocol
     */
    VpProfileProtocol getProfileService();

    /**
     * VpVerificationProtocol 반환
     *
     * @return VpVerificationProtocol
     */
    VpVerificationProtocol getVerificationService();

    /**
     * VerificationConfirmProtocol 반환
     *
     * @return VerificationConfirmProtocol
     */
    VerificationConfirmProtocol getConfirmService();

    /**
     * ZkpProofVerificationProtocol 반환
     *
     * @return ZkpProofVerificationProtocol
     */
    ZkpProofVerificationProtocol getZkpProofVerificationProtocol();
}
