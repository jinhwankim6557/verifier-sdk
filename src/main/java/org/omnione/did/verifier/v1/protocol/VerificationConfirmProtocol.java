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

import com.google.gson.JsonObject;
import org.omnione.did.verifier.v1.model.enums.OfferType;
import org.omnione.did.verifier.v1.model.response.VerificationConfirmResult;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;

import java.util.List;
import java.util.Map;

/**
 * Verification Confirm Service Interface
 *
 * VP 제출 확인 및 클레임 추출을 담당합니다.
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>VP 제출 확인</li>
 *   <li>Holder DID 추출</li>
 *   <li>제출된 VC 목록 추출</li>
 *   <li>클레임 추출 (전체/필터링)</li>
 * </ul>
 *
 * <h2>기본 구현체</h2>
 * {@link org.omnione.did.verifier.v1.service.DefaultVerificationConfirmService}
 *
 * @since 2.0.0
 */
public interface VerificationConfirmProtocol {

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

    /**
     * Holder DID 추출
     *
     * @param vpObject VP JSON 객체
     * @return Holder DID
     * @throws VerifierSdkException Holder DID가 없을 경우
     */
    String extractHolderDid(JsonObject vpObject);

    /**
     * 제출된 VC 목록 추출 (VC ID 목록만)
     *
     * @param vpObject VP JSON 객체
     * @return VC ID 목록
     */
    List<String> extractSubmittedVcs(JsonObject vpObject);

    /**
     * 제출된 VC 목록 추출 (VC ID → VC 객체 맵)
     *
     * @param vpObject VP JSON 객체
     * @return VC ID → VC 객체 맵
     */
    Map<String, Object> extractSubmittedVcsAsMap(JsonObject vpObject);

    /**
     * 클레임 추출 (전체)
     *
     * @param vpObject VP JSON 객체
     * @return 추출된 클레임 맵 (claimCode -> claimValue)
     */
    Map<String, Object> extractClaims(JsonObject vpObject);

    /**
     * 클레임 추출 (필터링)
     *
     * @param vpObject VP JSON 객체
     * @param requiredClaims 필요한 클레임 코드 목록
     * @return 추출된 클레임 맵 (requiredClaims에 해당하는 것만)
     */
    Map<String, Object> extractFilteredClaims(
        JsonObject vpObject,
        List<String> requiredClaims
    );
}
