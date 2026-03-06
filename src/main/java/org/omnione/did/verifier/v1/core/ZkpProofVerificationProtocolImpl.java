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

import com.google.gson.Gson;
import org.omnione.did.verifier.v1.provider.CryptoHelper;
import org.omnione.did.verifier.v1.provider.EcdhSessionProvider;
import org.omnione.did.verifier.v1.provider.StorageProvider;
import org.omnione.did.verifier.v1.provider.VerifierInfoProvider;
import org.omnione.did.verifier.v1.model.enums.*;
import org.omnione.did.verifier.v1.model.policy.*;
import org.omnione.did.verifier.v1.model.request.*;
import org.omnione.did.verifier.v1.model.response.*;
import org.omnione.did.verifier.v1.model.data.*;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.omnione.did.verifier.v1.protocol.ZkpProofVerificationProtocol;
import org.omnione.did.zkp.core.manager.ZkpProofManager;
import org.omnione.did.zkp.crypto.constant.ZkpCryptoConstants;
import org.omnione.did.zkp.crypto.util.BigIntegerUtil;
import org.omnione.did.zkp.datamodel.definition.CredentialDefinition;
import org.omnione.did.zkp.datamodel.proof.Identifiers;
import org.omnione.did.zkp.datamodel.proof.Proof;
import org.omnione.did.zkp.datamodel.proof.verifyparam.ProofVerifyParam;
import org.omnione.did.zkp.datamodel.proofrequest.ProofRequest;
import org.omnione.did.zkp.datamodel.schema.CredentialSchema;
import org.omnione.did.zkp.exception.ZkpException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ZKP Proof Verification Service 기본 구현체
 *
 * ZKP Proof 검증을 처리합니다.
 *
 * <h2>Protocol Logic</h2>
 * <ol>
 *   <li>ProofRequestProfile 생성 (E2E 키쌍 포함)</li>
 *   <li>ZKP Proof 복호화 (E2E 세션 사용)</li>
 *   <li>Nonce 검증</li>
 *   <li>ZkpProofManager 검증 (Proof, ProofRequest, ProofVerifyParams)</li>
 *   <li>Revealed Attributes 추출</li>
 * </ol>
 *
 * <h2>Application Logic (별도 처리)</h2>
 * <ul>
 *   <li>Policy/Profile DB 조회</li>
 *   <li>Proof 서명 추가 (FileWallet)</li>
 *   <li>E2E 세션 저장</li>
 *   <li>VpSubmit 저장</li>
 *   <li>Transaction 상태 업데이트</li>
 * </ul>
 *
 * @since 2.0.0
 */
public class ZkpProofVerificationProtocolImpl implements ZkpProofVerificationProtocol {

    private final EcdhSessionProvider sessionProvider;
    private final StorageProvider storageService;
    private final VerifierInfoProvider verifierInfoProvider;
    private final CryptoHelper cryptoHelper;
    private final Gson gson;

    /**
     * 생성자
     *
     * @param sessionProvider E2E 세션 제공자
     * @param storageService 저장소 서비스
     * @param verifierInfoProvider Verifier 정보 제공자
     * @param cryptoHelper 암호화 헬퍼
     */
    public ZkpProofVerificationProtocolImpl(
        EcdhSessionProvider sessionProvider,
        StorageProvider storageService,
        VerifierInfoProvider verifierInfoProvider,
        CryptoHelper cryptoHelper
    ) {
        this.sessionProvider = sessionProvider;
        this.storageService = storageService;
        this.verifierInfoProvider = verifierInfoProvider;
        this.cryptoHelper = cryptoHelper;
        this.gson = new Gson();
    }

    @Override
    public ProofRequestProfile requestZkpProofRequestProfile(
        ProofRequestProfileRequest request,
        ZkpPolicy policy
    ) {
        try {
            // 1. ProofRequest 초기화 (Nonce 생성)
            ProofRequest proofRequest = initializeProofRequest(policy);

            // 2. E2E 키쌍 생성
            KeyPairInfo e2eKeyPair = cryptoHelper.generateEcKeyPair(
                request.getReqE2e().getCurve()
            );

            // 3. ReqE2e 설정 (immutable이므로 새로 생성)
            ReqE2e reqE2e = ReqE2e.builder()
                .curve(request.getReqE2e().getCurve())
                .cipher(request.getReqE2e().getCipher())
                .padding(request.getReqE2e().getPadding())
                .publicKey(e2eKeyPair.getPublicKey())
                .nonce(generateE2eNonce())
                .build();

            // 4. ProofRequestProfile 구성 (Proof는 Application Layer에서 추가)
            ProofRequestProfile profile = ProofRequestProfile.builder()
                .id(request.getProfileId())
                .type("VerifyProfile")
                .title(policy.getTitle())
                .description(policy.getDescription())
                .profile(ZkpInnerProfile.builder()
                    .verifier(verifierInfoProvider.getVerifierInfo())
                    .proofRequest(proofRequest)
                    .reqE2e(reqE2e)
                    .build())
                .build();

            // 5. E2E 세션 저장
            sessionProvider.saveSession(
                request.getTxId(),
                e2eKeyPair,
                reqE2e
            );

            return profile;

        } catch (Exception e) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_UNKNOWN_ERROR,
                "Failed to create ProofRequestProfile: " + e.getMessage()
            );
        }
    }

    @Override
    public ZkpVerificationResult verifyZkpProof(ZkpVerificationRequest request) {
        try {
            // 1. ZKP Proof 복호화
            Proof proof = decryptZkpProof(
                request.getTxId(),
                request.getEncProof(),
                request.getIv(),
                request.getAccE2e()
            );

            // 2. Nonce 추출 (실제 검증은 ZkpProofManager.verifyProof()에서 수행됨)
            BigInteger proofNonce = new BigInteger(request.getNonce());

            // 3. ProofVerifyParams 구성
            List<ProofVerifyParam> proofVerifyParams = buildProofVerifyParams(
                proof.getIdentifiers(),
                request.getProofVerifyParams()
            );

            // 4. ZkpProofManager로 검증
            ZkpProofManager zkpProofManager = new ZkpProofManager();
            ProofRequest proofRequest = gson.fromJson(
                gson.toJson(request.getProofRequest()),
                ProofRequest.class
            );

            zkpProofManager.verifyProof(
                proof,
                proofNonce,
                proofRequest,
                proofVerifyParams
            );

            // 5. Revealed Attributes 추출
            Map<String, String> revealedAttributes = extractRevealedAttributes(proof);

            // 6. 검증 결과 반환
            return ZkpVerificationResult.builder()
                .txId(request.getTxId())
                .verified(true)
                .revealedAttributes(revealedAttributes)
                .verifiedAt(System.currentTimeMillis())
                .proofJson(gson.toJson(proof))
                .build();

        } catch (ZkpException e) {
            return ZkpVerificationResult.builder()
                .txId(request.getTxId())
                .verified(false)
                .errorMessage("ZKP verification failed: " + e.getMessage())
                .verifiedAt(System.currentTimeMillis())
                .build();

        } catch (Exception e) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_SIGNATURE_VERIFICATION_FAILED,
                "Failed to verify ZKP proof: " + e.getMessage()
            );
        }
    }

    @Override
    public Proof decryptZkpProof(
        String txId,
        String encProof,
        String iv,
        ZkpVerificationRequest.AccE2e accE2e
    ) {
        if (!sessionProvider.existsSession(txId)) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_E2E_SESSION_NOT_FOUND,
                "E2E session not found: " + txId
            );
        }

        try {
            // E2E 복호화
            String decryptedProofJson = sessionProvider.decrypt(
                txId,
                accE2e.getPublicKey(),
                encProof,
                iv
            );

            // Proof 파싱
            return gson.fromJson(decryptedProofJson, Proof.class);

        } catch (Exception e) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_DECRYPTION_FAILED,
                "Failed to decrypt ZKP proof: " + e.getMessage()
            );
        }
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * ProofRequest 초기화 (Nonce 생성)
     *
     * ZkpPolicy에서 requestedAttributes와 requestedPredicates를 파싱하여
     * ProofRequest를 구성합니다.
     */
    private ProofRequest initializeProofRequest(ZkpPolicy policy) {
        ProofRequest proofRequest = new ProofRequest();

        // Verifier Nonce 생성 (ZKP용 큰 Nonce)
        BigInteger verifierNonce = new BigIntegerUtil()
            .createRandomBigInteger(ZkpCryptoConstants.LARGE_NONCE);

        // requestedAttributes 파싱 (JSON → ProofRequest)
        if (policy.getRequestedAttributes() != null && !policy.getRequestedAttributes().isEmpty()) {
            String requestedAttributesJson = policy.getRequestedAttributes();
            ProofRequest tempProofReq = gson.fromJson(
                "{\"requestedAttributes\":" + requestedAttributesJson + "}",
                ProofRequest.class
            );
            proofRequest.setRequestedAttributes(tempProofReq.getRequestedAttributes());
        }

        // requestedPredicates 파싱 (JSON → ProofRequest)
        if (policy.getRequestedPredicates() != null && !policy.getRequestedPredicates().isEmpty()) {
            String requestedPredicatesJson = policy.getRequestedPredicates();
            ProofRequest tempProofReq = gson.fromJson(
                "{\"requestedPredicates\":" + requestedPredicatesJson + "}",
                ProofRequest.class
            );
            proofRequest.setRequestedPredicates(tempProofReq.getRequestedPredicates());
        }

        // ProofRequest 기본 정보 설정
        proofRequest.setNonce(verifierNonce);
        proofRequest.setName(policy.getName());
        proofRequest.setVersion(policy.getVersion());

        return proofRequest;
    }

    /**
     * E2E Nonce 생성 (16바이트)
     */
    private String generateE2eNonce() {
        return cryptoHelper.generateNonce(16);
    }

    /**
     * ProofVerifyParams 구성
     */
    private List<ProofVerifyParam> buildProofVerifyParams(
        List<Identifiers> identifiers,
        List<org.omnione.did.verifier.v1.model.data.ProofVerifyParam> requestParams
    ) {
        LinkedList<ProofVerifyParam> proofVerifyParams = new LinkedList<>();

        for (Identifiers id : identifiers) {
            // Storage에서 CredentialSchema 조회
            CredentialSchema zkpCredSchema = storageService.getZKPCredential(
                id.getSchemaId()
            );

            // Storage에서 CredentialDefinition 조회
            CredentialDefinition zkpCredDef = storageService.getZKPCredentialDefinition(
                id.getCredDefId()
            );

            // ProofVerifyParam 구성
            ProofVerifyParam proofVerifyParam = new ProofVerifyParam.Builder()
                .setSchema(zkpCredSchema)
                .setCredentialDefinition(zkpCredDef)
                .build();

            proofVerifyParams.add(proofVerifyParam);
        }

        return proofVerifyParams;
    }

    /**
     * Revealed Attributes 추출
     */
    private Map<String, String> extractRevealedAttributes(Proof proof) {
        Map<String, String> attributes = new HashMap<>();

        // Proof에서 Revealed Attributes 추출
        // ZKP Proof 구조에 따라 추출 로직은 Application에서 구현하거나
        // 실제 Proof 구조를 확인하여 수정 필요
        // 현재는 빈 Map 반환

        return attributes;
    }
}
