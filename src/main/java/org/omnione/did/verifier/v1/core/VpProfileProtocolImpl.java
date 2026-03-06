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

import org.omnione.did.data.model.profile.verify.InnerVerifyProfile;
import org.omnione.did.data.model.profile.verify.VerifyProcess;
import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.data.model.provider.ProviderDetail;
import org.omnione.did.verifier.v1.provider.NonceGenerator;
import org.omnione.did.verifier.v1.provider.VerificationConfigProvider;
import org.omnione.did.verifier.v1.provider.VerifierInfoProvider;
import org.omnione.did.verifier.v1.model.data.ReqE2e;
import org.omnione.did.verifier.v1.model.policy.VerificationPolicy;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.omnione.did.verifier.v1.protocol.VpProfileProtocol;

/**
 * VP Profile Service 기본 구현체
 *
 * Verify Profile을 생성합니다.
 *
 * <h2>설계 원칙</h2>
 * <ul>
 *   <li>SDK는 프로토콜 로직만 담당 (검증 알고리즘 순서, E2E 복호화 흐름, Profile 데이터 조립)</li>
 *   <li>Core DTO(VerifyProfile)를 직접 조립하여 반환 - SDK 고유 중간 DTO 사용 금지</li>
 *   <li>Proof 생성은 Application Server 책임 (SDK는 Proof 없는 Profile 반환)</li>
 *   <li>DB 저장/조회, Wallet 서명, Transaction 관리 없음</li>
 * </ul>
 *
 * <h2>Protocol Logic</h2>
 * <ol>
 *   <li>Policy 조회</li>
 *   <li>Verifier 정보 조회</li>
 *   <li>Verifier Nonce 생성</li>
 *   <li>Core VerifyProcess 조립 (reqE2e + verifierNonce 포함)</li>
 *   <li>Core InnerVerifyProfile 조립</li>
 *   <li>Core VerifyProfile 반환 (Proof 제외)</li>
 * </ol>
 *
 * <h2>Application Logic (별도 처리)</h2>
 * <ul>
 *   <li>Proof 서명 추가</li>
 *   <li>Profile 저장</li>
 * </ul>
 *
 * @since 2.0.0
 */
public class VpProfileProtocolImpl implements VpProfileProtocol {

    private final VerificationConfigProvider configProvider;
    private final VerifierInfoProvider verifierInfoProvider;
    private final NonceGenerator nonceGenerator;

    /**
     * 생성자
     *
     * @param configProvider Policy 제공자
     * @param verifierInfoProvider Verifier 정보 제공자
     * @param nonceGenerator Nonce 생성기
     */
    public VpProfileProtocolImpl(
        VerificationConfigProvider configProvider,
        VerifierInfoProvider verifierInfoProvider,
        NonceGenerator nonceGenerator
    ) {
        this.configProvider = configProvider;
        this.verifierInfoProvider = verifierInfoProvider;
        this.nonceGenerator = nonceGenerator;
    }

    @Override
    public VerifyProfile requestVerifyProfile(
        String policyId,
        String profileId,
        ReqE2e reqE2e
    ) {
        // 1. Policy 조회
        VerificationPolicy policy = configProvider.getPolicy(policyId);
        if (policy == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_POLICY_NOT_FOUND,
                    "Policy not found: " + policyId);
        }

        // 2. Verifier 정보 조회 (Core ProviderDetail)
        ProviderDetail verifierInfo = verifierInfoProvider.getVerifierInfo();

        // 3. Verifier Nonce 생성
        String verifierNonce = nonceGenerator.generateNonce();

        // 4. Core ReqE2e 구성 (SDK ReqE2e → Core ReqE2e, 필드 직접 매핑)
        org.omnione.did.data.model.profile.ReqE2e coreReqE2e = buildCoreReqE2e(reqE2e);

        // 5. Core VerifyProcess 조립 (base process + reqE2e + verifierNonce)
        VerifyProcess verifyProcess = buildVerifyProcess(policy.getProcess(), coreReqE2e, verifierNonce);

        // 6. Core InnerVerifyProfile 조립
        InnerVerifyProfile innerProfile = new InnerVerifyProfile();
        innerProfile.setVerifier(verifierInfo);
        innerProfile.setFilter(policy.getFilter());
        innerProfile.setProcess(verifyProcess);

        // 7. Core VerifyProfile 반환 (Proof는 Application에서 추가)
        VerifyProfile profile = new VerifyProfile();
        profile.setId(profileId);
        profile.setType("VerifyProfile");
        profile.setTitle(policy.getPolicyName());
        profile.setDescription(policy.getDescription() != null ? policy.getDescription() : "");
        profile.setEncoding("UTF-8");
        profile.setLanguage(policy.getLanguage());
        profile.setProfile(innerProfile);

        return profile;
    }

    /**
     * SDK ReqE2e → Core ReqE2e 변환 (필드 직접 매핑, JSON 직렬화 없음)
     */
    private org.omnione.did.data.model.profile.ReqE2e buildCoreReqE2e(ReqE2e sdkReqE2e) {
        org.omnione.did.data.model.profile.ReqE2e coreReqE2e =
                new org.omnione.did.data.model.profile.ReqE2e();
        coreReqE2e.setNonce(sdkReqE2e.getNonce());
        coreReqE2e.setCurve(sdkReqE2e.getCurve());
        coreReqE2e.setPublicKey(sdkReqE2e.getPublicKey());
        coreReqE2e.setCipher(sdkReqE2e.getCipher());
        coreReqE2e.setPadding(sdkReqE2e.getPadding());
        return coreReqE2e;
    }

    /**
     * Core VerifyProcess 조립
     * base process(DB에서 조회된 endpoints, authType)에 reqE2e와 verifierNonce를 추가
     */
    private VerifyProcess buildVerifyProcess(
            VerifyProcess baseProcess,
            org.omnione.did.data.model.profile.ReqE2e coreReqE2e,
            String verifierNonce) {
        VerifyProcess process = new VerifyProcess();
        if (baseProcess != null) {
            process.setEndpoints(baseProcess.getEndpoints());
            process.setAuthType(baseProcess.getAuthType());
        }
        process.setReqE2e(coreReqE2e);
        process.setVerifierNonce(verifierNonce);
        return process;
    }
}
