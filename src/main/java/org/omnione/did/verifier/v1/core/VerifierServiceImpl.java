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

package org.omnione.did.verifier.v1.core;

import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.verifier.v1.provider.*;
import org.omnione.did.verifier.v1.model.enums.*;
import org.omnione.did.verifier.v1.model.policy.*;
import org.omnione.did.verifier.v1.model.request.*;
import org.omnione.did.verifier.v1.model.response.*;
import org.omnione.did.verifier.v1.model.data.*;
import org.omnione.did.verifier.v1.protocol.*;
import org.omnione.did.zkp.datamodel.proof.Proof;

/**
 * Verifier Service 기본 구현체 (Facade)
 *
 * VP/ZKP 검증 프로토콜의 통합 진입점입니다.
 * 5개 서비스를 통합하여 사용 편의성을 제공합니다.
 *
 * <h2>Application에서 사용 방법</h2>
 * <ol>
 *   <li>Interface 구현체 주입</li>
 *   <li>DefaultVerifierService 생성</li>
 *   <li>VP/ZKP 메서드 호출</li>
 * </ol>
 *
 * @since 2.0.0
 */
public class VerifierServiceImpl implements VerifierService {

    private final VpOfferProtocol offerService;
    private final VpProfileProtocol profileService;
    private final VpVerificationProtocol verificationService;
    private final VerificationConfirmProtocol confirmService;
    private final ZkpProofVerificationProtocol zkpProofVerificationService;

    /**
     * 생성자 (API 인터페이스 주입)
     *
     * @param configProvider Policy 제공자
     * @param verifierInfoProvider Verifier 정보 제공자
     * @param sessionProvider E2E 세션 제공자
     * @param storageService 저장소 서비스
     * @param transactionManager Transaction 관리자
     * @param nonceGenerator Nonce 생성기
     * @param cryptoHelper 암호화 헬퍼
     */
    public VerifierServiceImpl(
        VerificationConfigProvider configProvider,
        VerifierInfoProvider verifierInfoProvider,
        EcdhSessionProvider sessionProvider,
        StorageProvider storageService,
        TransactionProvider transactionManager,
        NonceGenerator nonceGenerator,
        CryptoHelper cryptoHelper
    ) {
        this.offerService = new VpOfferProtocolImpl(configProvider, transactionManager);
        this.profileService = new VpProfileProtocolImpl(configProvider, verifierInfoProvider, nonceGenerator);
        this.verificationService = new VpVerificationProtocolImpl(sessionProvider, storageService, cryptoHelper);
        this.confirmService = new VerificationConfirmProtocolImpl();
        this.zkpProofVerificationService = new ZkpProofVerificationProtocolImpl(
            sessionProvider,
            storageService,
            verifierInfoProvider,
            cryptoHelper
        );
    }

    /**
     * 생성자 (SPI 서비스 직접 주입)
     *
     * 커스텀 서비스 구현체를 직접 주입할 때 사용합니다.
     *
     * @param offerService VP Offer 서비스
     * @param profileService VP Profile 서비스
     * @param verificationService VP 검증 서비스
     * @param confirmService 검증 확인 서비스
     * @param zkpProofVerificationService ZKP 검증 서비스
     */
    public VerifierServiceImpl(
        VpOfferProtocol offerService,
        VpProfileProtocol profileService,
        VpVerificationProtocol verificationService,
        VerificationConfirmProtocol confirmService,
        ZkpProofVerificationProtocol zkpProofVerificationService
    ) {
        this.offerService = offerService;
        this.profileService = profileService;
        this.verificationService = verificationService;
        this.confirmService = confirmService;
        this.zkpProofVerificationService = zkpProofVerificationService;
    }

    // ========================================================================
    // 1. VP Offer 생성
    // ========================================================================

    @Override
    public VpOfferPayload requestVpOffer(
        String policyId,
        String device,
        String service,
        boolean locked
    ) {
        return offerService.requestVpOffer(policyId, device, service, locked);
    }

    @Override
    public VpOfferPayload requestStaticVpOffer(
        String policyId,
        String device,
        String service,
        boolean locked
    ) {
        return offerService.requestStaticVpOffer(policyId, device, service, locked);
    }

    // ========================================================================
    // 2. Verify Profile 생성
    // ========================================================================

    @Override
    public VerifyProfile requestVerifyProfile(
        String policyId,
        String profileId,
        ReqE2e reqE2e
    ) {
        return profileService.requestVerifyProfile(policyId, profileId, reqE2e);
    }

    // ========================================================================
    // 3. VP 검증
    // ========================================================================

    @Override
    public String verifyPresentation(VpVerificationRequest request) {
        return verificationService.verifyPresentation(request);
    }

    @Override
    public String decryptVp(String txId, String encHolderPublicKey, String encVp, String iv) {
        return verificationService.decryptVp(txId, encHolderPublicKey, encVp, iv);
    }

    // ========================================================================
    // 4. 검증 확인
    // ========================================================================

    @Override
    public VerificationConfirmResult confirmVerification(
        String txId,
        String vpJson,
        boolean verified
    ) {
        return confirmService.confirmVerification(txId, vpJson, verified);
    }

    @Override
    public VerificationConfirmResult confirmVerification(
        String txId,
        String vpOrProofJson,
        boolean verified,
        OfferType offerType
    ) {
        return confirmService.confirmVerification(txId, vpOrProofJson, verified, offerType);
    }

    // ========================================================================
    // 5. ZKP Proof 검증
    // ========================================================================

    @Override
    public ProofRequestProfile requestZkpProofRequestProfile(
        ProofRequestProfileRequest request,
        ZkpPolicy policy
    ) {
        return zkpProofVerificationService.requestZkpProofRequestProfile(request, policy);
    }

    @Override
    public ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request) {
        return zkpProofVerificationService.verifyZkpProof(request);
    }

    @Override
    public Proof decryptZkpProof(
        String txId,
        String encProof,
        String iv,
        ZkpVerificationRequest.AccE2e accE2e
    ) {
        return zkpProofVerificationService.decryptZkpProof(txId, encProof, iv, accE2e);
    }

    // ========================================================================
    // 개별 서비스 접근
    // ========================================================================

    @Override
    public VpOfferProtocol getOfferService() {
        return offerService;
    }

    @Override
    public VpProfileProtocol getProfileService() {
        return profileService;
    }

    @Override
    public VpVerificationProtocol getVerificationService() {
        return verificationService;
    }

    @Override
    public VerificationConfirmProtocol getConfirmService() {
        return confirmService;
    }

    @Override
    public ZkpProofVerificationProtocol getZkpProofVerificationProtocol() {
        return zkpProofVerificationService;
    }
}
