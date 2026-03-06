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

import org.omnione.did.verifier.v1.model.data.VpOfferPayload;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;

/**
 * VP Offer Service Interface
 *
 * VP 제출 Offer Payload 생성을 담당합니다.
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>Dynamic QR용 VP Offer Payload 생성 (offerId 포함)</li>
 *   <li>Static QR용 VP Offer Payload 생성 (offerId 없음)</li>
 * </ul>
 *
 * <h2>기본 구현체</h2>
 * {@link org.omnione.did.verifier.v1.service.DefaultVpOfferService}
 *
 * @since 2.0.0
 */
public interface VpOfferProtocol {

    /**
     * VP Offer Payload 생성 (Dynamic QR용)
     *
     * @param policyId Policy ID
     * @param device 응대장치 식별자
     * @param service 서비스 식별자
     * @param locked Offer 잠김 여부
     * @return VpOfferPayload (offerId 포함)
     * @throws VerifierSdkException Policy가 존재하지 않을 경우 (SDK_POLICY_NOT_FOUND)
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
     * @return VpOfferPayload (offerId = null)
     * @throws VerifierSdkException Policy가 존재하지 않을 경우 (SDK_POLICY_NOT_FOUND)
     */
    VpOfferPayload requestStaticVpOffer(
        String policyId,
        String device,
        String service,
        boolean locked
    );
}
