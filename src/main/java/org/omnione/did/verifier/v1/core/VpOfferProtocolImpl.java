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

import org.omnione.did.verifier.v1.provider.TransactionProvider;
import org.omnione.did.verifier.v1.provider.VerificationConfigProvider;
import org.omnione.did.verifier.v1.model.enums.OfferType;
import org.omnione.did.verifier.v1.model.enums.PresentMode;
import org.omnione.did.verifier.v1.model.data.VpOfferPayload;
import org.omnione.did.verifier.v1.model.policy.VerificationPolicy;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.omnione.did.verifier.v1.protocol.VpOfferProtocol;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * VP Offer Service 기본 구현체
 *
 * VP 제출 Offer Payload를 생성합니다.
 *
 * <h2>Protocol Logic</h2>
 * <ol>
 *   <li>Policy 조회</li>
 *   <li>Offer ID 생성 (Transaction Manager 사용)</li>
 *   <li>Payload 가공</li>
 * </ol>
 *
 * <h2>Application Logic (별도 처리)</h2>
 * <ul>
 *   <li>Transaction 저장</li>
 *   <li>VpOffer 저장</li>
 *   <li>Payload 저장</li>
 * </ul>
 *
 * @since 2.0.0
 */
public class VpOfferProtocolImpl implements VpOfferProtocol {

    private final VerificationConfigProvider configProvider;
    private final TransactionProvider transactionManager;

    /**
     * 생성자
     *
     * @param configProvider Policy 제공자
     * @param transactionManager Transaction 관리자
     */
    public VpOfferProtocolImpl(
        VerificationConfigProvider configProvider,
        TransactionProvider transactionManager
    ) {
        this.configProvider = configProvider;
        this.transactionManager = transactionManager;
    }

    @Override
    public VpOfferPayload requestVpOffer(
        String policyId,
        String device,
        String service,
        boolean locked
    ) {
        // 1. Policy 조회
        VerificationPolicy policy = configProvider.getPolicy(policyId);
        if (policy == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_POLICY_NOT_FOUND,
                    "Policy not found: " + policyId);
        }

        // 2. Offer ID 생성
        String offerId = transactionManager.createTransactionId();

        // 3. ValidUntil 계산 (현재 시간 + 유효기간)
        Instant validUntilInstant = Instant.now().plus(policy.getValidityDuration(), ChronoUnit.SECONDS);

        // 4. Payload 가공
        return VpOfferPayload.builder()
            .offerId(offerId)
            .type(OfferType.VerifyOffer)
            .mode(PresentMode.fromDisplayName(policy.getMode()))
            .device(device)
            .service(service)
            .endpoints(policy.getEndpoints())
            .validUntil(validUntilInstant.toString())
            .locked(locked)
            .build();
    }

    @Override
    public VpOfferPayload requestStaticVpOffer(
        String policyId,
        String device,
        String service,
        boolean locked
    ) {
        // 1. Policy 조회
        VerificationPolicy policy = configProvider.getPolicy(policyId);
        if (policy == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_POLICY_NOT_FOUND,
                    "Policy not found: " + policyId);
        }

        // 2. Payload 가공 (offerId 없음)
        return VpOfferPayload.builder()
            .offerId(null)  // Static QR은 offerId가 없음
            .type(OfferType.VerifyOffer)
            .mode(PresentMode.fromDisplayName(policy.getMode()))
            .device(device)
            .service(service)
            .endpoints(policy.getEndpoints())
            .validUntil(null)  // Static QR은 유효기간이 없음
            .locked(locked)
            .build();
    }
}
