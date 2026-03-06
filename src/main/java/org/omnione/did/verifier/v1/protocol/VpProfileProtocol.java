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
import org.omnione.did.verifier.v1.model.data.ReqE2e;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;

/**
 * VP Profile Service Interface
 *
 * Verify Profile 생성을 담당합니다.
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>Verify Profile 생성 (E2E 암호화 정보 포함)</li>
 * </ul>
 *
 * <h2>설계 원칙</h2>
 * <ul>
 *   <li>SDK는 Core DTO(VerifyProfile)를 직접 반환 - SDK 고유 중간 DTO 사용 금지</li>
 *   <li>Proof 생성은 Application Server 책임 (SDK는 Proof 없는 Profile 반환)</li>
 * </ul>
 *
 * <h2>기본 구현체</h2>
 * {@link org.omnione.did.verifier.v1.service.DefaultVpProfileService}
 *
 * @since 2.0.0
 */
public interface VpProfileProtocol {

    /**
     * Verify Profile 생성
     *
     * @param policyId Policy ID
     * @param profileId Profile ID (UUID 권장)
     * @param reqE2e E2E 암호화 요청 정보 (Application에서 생성)
     * @return VerifyProfile (Core, Proof 제외) - Application에서 Proof 추가 필요
     * @throws VerifierSdkException Policy가 존재하지 않을 경우 (SDK_POLICY_NOT_FOUND)
     */
    VerifyProfile requestVerifyProfile(
        String policyId,
        String profileId,
        ReqE2e reqE2e
    );
}
