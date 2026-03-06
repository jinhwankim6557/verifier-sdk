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

import lombok.extern.slf4j.Slf4j;
import org.omnione.did.core.data.rest.VpVerifyParam;
import org.omnione.did.core.exception.CoreException;
import org.omnione.did.core.manager.VpManager;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.profile.Filter;
import org.omnione.did.data.model.vc.VerifiableCredential;
import org.omnione.did.data.model.vp.VerifiablePresentation;
import org.omnione.did.verifier.v1.provider.CryptoHelper;
import org.omnione.did.verifier.v1.provider.EcdhSessionProvider;
import org.omnione.did.verifier.v1.provider.StorageProvider;
import org.omnione.did.verifier.v1.model.request.VpVerificationRequest;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.omnione.did.verifier.v1.protocol.VpVerificationProtocol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * VP Verification Service 기본 구현체
 *
 * VP 검증을 처리합니다.
 *
 * <h2>Protocol Logic</h2>
 * <ol>
 *   <li>VP 복호화 (E2E 세션 사용)</li>
 *   <li>AuthType 검증</li>
 *   <li>Nonce 검증</li>
 *   <li>VpManager 종합 검증 (서명, 스키마, 필터, 시간, Holder-Subject 등)</li>
 *   <li>VC 상태 검증 (블록체인 기반)</li>
 * </ol>
 *
 * <h2>Application Logic (별도 처리)</h2>
 * <ul>
 *   <li>E2E 세션 관리</li>
 *   <li>VpSubmit 저장</li>
 *   <li>Transaction 상태 업데이트</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Slf4j
public class VpVerificationProtocolImpl implements VpVerificationProtocol {

    private final EcdhSessionProvider sessionProvider;
    private final StorageProvider storageService;
    private final Gson gson;

    /**
     * 생성자
     *
     * @param sessionProvider E2E 세션 제공자
     * @param storageService 저장소 서비스
     * @param cryptoHelper 암호화 헬퍼 (하위 호환성 유지, 내부적으로 미사용)
     */
    public VpVerificationProtocolImpl(
        EcdhSessionProvider sessionProvider,
        StorageProvider storageService,
        CryptoHelper cryptoHelper
    ) {
        this.sessionProvider = sessionProvider;
        this.storageService = storageService;
        this.gson = new Gson();
    }

    @Override
    public String verifyPresentation(VpVerificationRequest request) {
        // 1. VP 복호화
        String vpJson = decryptVp(
            request.getTxId(),
            request.getEncHolderPublicKey(),
            request.getEncVp(),
            request.getIv()
        );

        // 2. VP 파싱 (한 번만 파싱하여 모든 검증에 사용)
        VerifiablePresentation vp = new VerifiablePresentation();
        vp.fromJson(vpJson);

        log.debug("Verifying VP toJSON: {}", vp.toJson());

        // 3. AuthType 검증
        validateAuthType(vp, request.getRequiredAuthType());

        // 4. Nonce 검증
        validateNonce(vp, request.getVerifierNonce());

        // 5. VpManager 종합 검증 (서명, 스키마, 필터, 시간, Holder-Subject 등)
        validateWithVpManager(vp, request.getFilter());

        // 6. VC 상태 검증 (블록체인 기반 상태 확인)
        validateVcStatuses(vp);

        return vpJson;
    }

    @Override
    public String decryptVp(String txId, String encHolderPublicKey, String encVp, String iv) {
        if (!sessionProvider.existsSession(txId)) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_E2E_SESSION_NOT_FOUND,
                "E2E session not found: " + txId
            );
        }

        try {
            return sessionProvider.decrypt(txId, encHolderPublicKey, encVp, iv);
        } catch (Exception e) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_DECRYPTION_FAILED,
                "Failed to decrypt VP: " + e.getMessage()
            );
        }
    }

    @Override
    public void validateAuthType(VerifiablePresentation vp, Integer requiredAuthType) {
        if (requiredAuthType == null || requiredAuthType == 0) {
            return;  // 인증 제한 없음
        }

        // Proof 개수 확인
        int proofCount = 0;
        if (vp.getProofs() != null && !vp.getProofs().isEmpty()) {
            proofCount = vp.getProofs().size();
        } else if (vp.getProof() != null) {
            proofCount = 1;
        } else {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_INVALID_PROOF,
                "VP has no proof"
            );
        }

        // AuthType 검증 로직
        // 0x00008006: PIN and BIO (2개 proof 필요)
        // 0x00000006: PIN or BIO (1개 proof 필요)
        // 0x00000002: PIN (1개 proof 필요)
        // 0x00000004: BIO (1개 proof 필요)

        if ((requiredAuthType & 0x00008000) != 0) {
            // AND 조건: 2개 proof 필요
            if (proofCount < 2) {
                throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_INVALID_PROOF,
                    "AuthType requires 2 proofs, but got: " + proofCount
                );
            }
        } else {
            // OR 조건 또는 단일 인증: 1개 이상 proof 필요
            if (proofCount < 1) {
                throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_INVALID_PROOF,
                    "AuthType requires at least 1 proof, but got: " + proofCount
                );
            }
        }
    }

    @Override
    public void validateNonce(VerifiablePresentation vp, String expectedNonce) {
        String actualNonce = vp.getVerifierNonce();
        if (actualNonce == null || actualNonce.isEmpty()) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_INVALID_NONCE,
                "VP has no verifierNonce"
            );
        }

        if (!expectedNonce.equals(actualNonce)) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_INVALID_NONCE,
                "Nonce mismatch. Expected: " + expectedNonce + ", Actual: " + actualNonce
            );
        }
    }

    /**
     * VpManager를 통한 종합 VP 검증
     *
     * core-sdk의 VpManager.verifyPresentation()을 사용하여
     * VP 서명, VC 서명, 스키마, 필터, 시간, Holder-Subject 일치 등을 검증합니다.
     * 각 VC에 대해 해당 Issuer의 DID Document를 조회하여 독립적으로 검증합니다.
     *
     * @param vp VP 객체 (이미 파싱된 객체 - proof 정보 보존)
     * @param filter 검증에 적용할 Filter 조건 (Core 타입, 변환 없이 직접 사용)
     * @throws VerifierSdkException 검증 실패 시
     */
    private void validateWithVpManager(VerifiablePresentation vp, Filter filter) {

        // Holder DID Document 조회
        String holderDidDocJson = storageService.findDidDocument(vp.getHolder());
        if (holderDidDocJson == null) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_DID_DOCUMENT_NOT_FOUND,
                "Holder DID Document not found: " + vp.getHolder()
            );
        }
        DidDocument holderDidDoc = new DidDocument();
        holderDidDoc.fromJson(holderDidDocJson);

        // 각 VC에 대해 VpManager로 검증
        VpManager vpManager = new VpManager();
        List<VerifiableCredential> vcs = vp.getVerifiableCredential();

        if (vcs == null || vcs.isEmpty()) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_INVALID_VP,
                "VP has no verifiableCredential"
            );
        }

        log.info("Verifying VP toJSON:222 {}", vp.toJson());

        for (VerifiableCredential vc : vcs) {
            String issuerDid = vc.getIssuer().getId();

            // DEBUG: VC Proof 구조 로깅
            log.debug("=== VC Proof Structure Debug ===");
            log.debug("VC ID: {}", vc.getId());
            log.debug("VC Issuer: {}", issuerDid);
            log.debug("VC Proof: {}", vc.getProof() != null ? vc.getProof().toJson() : "null");
            log.debug("================================");

            // Issuer DID Document 조회
            String issuerDidDocJson = storageService.findDidDocument(issuerDid);
            if (issuerDidDocJson == null) {
                throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_DID_DOCUMENT_NOT_FOUND,
                    "Issuer DID Document not found: " + issuerDid
                );
            }
            DidDocument issuerDidDoc = new DidDocument();
            issuerDidDoc.fromJson(issuerDidDocJson);

            // 검증 파라미터 구성
            VpVerifyParam verifyParam = new VpVerifyParam(holderDidDoc, issuerDidDoc);
            if (filter != null) {
                verifyParam.setFilter(filter);
            }

            try {
                log.info("Verifying VP toJSON333: {}", vp.toJson());
                vpManager.verifyPresentation(vp, verifyParam);
            } catch (CoreException e) {
                log.error("VpManager verification failed for VC ID: {}", vc.getId(), e);
                throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_SIGNATURE_VERIFICATION_FAILED,
                    "VP verification failed: " + e.getMessage()
                );
            }
        }
    }

    @Override
    public void validateVcStatuses(VerifiablePresentation vp) {
        List<VerifiableCredential> vcs = vp.getVerifiableCredential();
        if (vcs == null || vcs.isEmpty()) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_INVALID_VP,
                "VP has no verifiableCredential"
            );
        }

        // 각 VC의 상태 확인
        for (VerifiableCredential vc : vcs) {
            String vcId = vc.getId();
            if (vcId == null) {
                continue;
            }

            // VC Meta 조회
            String vcMetaJson = storageService.getVcMeta(vcId);
            if (vcMetaJson == null) {
                continue;
            }

            // VC 상태 확인
            JsonObject vcMeta = gson.fromJson(vcMetaJson, JsonObject.class);
            String status = extractVcStatus(vcMeta);

            if (status != null) {
                switch (status.toUpperCase()) {
                    case "ACTIVE":
                        break;
                    case "REVOKED":
                        throw new VerifierSdkException(
                            VerifierSdkErrorCode.SDK_INVALID_VC,
                            "VC has been revoked: " + vcId
                        );
                    case "INACTIVE":
                        throw new VerifierSdkException(
                            VerifierSdkErrorCode.SDK_INVALID_VC,
                            "VC is inactive: " + vcId
                        );
                    case "EXPIRED":
                        throw new VerifierSdkException(
                            VerifierSdkErrorCode.SDK_INVALID_VC,
                            "VC has expired: " + vcId
                        );
                    default:
                        break;
                }
            }
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * VC Meta에서 상태 추출
     */
    private String extractVcStatus(JsonObject vcMeta) {
        if (vcMeta.has("status")) {
            return vcMeta.get("status").getAsString();
        } else if (vcMeta.has("vcStatus")) {
            return vcMeta.get("vcStatus").getAsString();
        } else if (vcMeta.has("credentialStatus")) {
            var credentialStatus = vcMeta.get("credentialStatus");
            if (credentialStatus.isJsonObject()) {
                JsonObject statusObj = credentialStatus.getAsJsonObject();
                if (statusObj.has("status")) {
                    return statusObj.get("status").getAsString();
                }
            }
        }
        return null;
    }
}
