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

import org.omnione.did.data.model.vp.VerifiablePresentation;
import org.omnione.did.verifier.v1.model.request.VpVerificationRequest;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;

/**
 * VP Verification Service Interface
 *
 * VP 검증을 담당합니다.
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>VP 검증 (전체 프로세스)</li>
 *   <li>VP 복호화</li>
 *   <li>AuthType 검증</li>
 *   <li>Nonce 검증</li>
 *   <li>VC 상태 검증</li>
 * </ul>
 *
 * <h2>기본 구현체</h2>
 * {@link org.omnione.did.verifier.v1.service.DefaultVpVerificationService}
 *
 * @since 2.0.0
 */
public interface VpVerificationProtocol {

    /**
     * VP 검증 (전체 프로세스)
     *
     * core-sdk의 VpManager를 통해 다음 검증을 수행합니다:
     * <ul>
     *   <li>VP/VC 서명 검증</li>
     *   <li>VC 스키마 검증 (CredentialSchema 일치)</li>
     *   <li>클레임 필터 검증 (Policy Filter에 명시된 클레임만 포함 확인)</li>
     *   <li>VC 시간 검증 (issuanceDate, expirationDate)</li>
     *   <li>Holder-Subject 일치 검증 (VP.holder == VC.credentialSubject.id)</li>
     *   <li>Proof Purpose 검증</li>
     *   <li>@context, type 검증</li>
     * </ul>
     *
     * @param request VP 검증 요청 (filter 필드 필수)
     * @return 검증된 VP JSON 문자열
     * @throws VerifierSdkException VP 검증 실패 또는 유효하지 않은 VP (SDK_INVALID_VP, SDK_SIGNATURE_VERIFICATION_FAILED 등)
     */
    String verifyPresentation(VpVerificationRequest request);

    /**
     * VP 복호화
     *
     * @param txId Transaction ID
     * @param encHolderPublicKey Holder 공개키 (Multibase 인코딩)
     * @param encVp 암호화된 VP (Multibase 인코딩)
     * @param iv Initial Vector (Multibase 인코딩)
     * @return 복호화된 VP JSON 문자열
     * @throws VerifierSdkException 복호화 실패
     */
    String decryptVp(String txId, String encHolderPublicKey, String encVp, String iv);

    /**
     * AuthType 검증
     *
     * @param vp VP 객체
     * @param requiredAuthType 요구되는 인증 타입
     * @throws VerifierSdkException AuthType 불일치
     */
    void validateAuthType(VerifiablePresentation vp, Integer requiredAuthType);

    /**
     * Nonce 검증
     *
     * @param vp VP 객체
     * @param expectedNonce 기대되는 Verifier Nonce
     * @throws VerifierSdkException Nonce 불일치
     */
    void validateNonce(VerifiablePresentation vp, String expectedNonce);

    /**
     * VC 상태 검증
     *
     * @param vp VP 객체
     * @throws VerifierSdkException VC가 유효하지 않음
     */
    void validateVcStatuses(VerifiablePresentation vp);
}
