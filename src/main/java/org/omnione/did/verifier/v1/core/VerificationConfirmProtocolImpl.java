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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.omnione.did.verifier.v1.model.enums.OfferType;
import org.omnione.did.verifier.v1.model.response.VerificationConfirmResult;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.omnione.did.verifier.v1.protocol.VerificationConfirmProtocol;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verification Confirm Service 기본 구현체
 *
 * VP 제출 확인 및 클레임 추출을 처리합니다.
 *
 * <h2>Protocol Logic</h2>
 * <ol>
 *   <li>VP에서 Holder DID 추출</li>
 *   <li>VP에서 제출된 VC 목록 추출</li>
 *   <li>VP에서 클레임 추출</li>
 * </ol>
 *
 * <h2>Application Logic (별도 처리)</h2>
 * <ul>
 *   <li>Transaction 조회</li>
 *   <li>VpSubmit 조회</li>
 *   <li>커스텀 클레임 처리</li>
 * </ul>
 *
 * @since 2.0.0
 */
public class VerificationConfirmProtocolImpl implements VerificationConfirmProtocol {

    private final Gson gson;

    /**
     * 생성자
     */
    public VerificationConfirmProtocolImpl() {
        this.gson = new Gson();
    }

    @Override
    public VerificationConfirmResult confirmVerification(
        String txId,
        String vpJson,
        boolean verified
    ) {
        return confirmVerification(txId, vpJson, verified, OfferType.VerifyOffer);
    }

    @Override
    public VerificationConfirmResult confirmVerification(
        String txId,
        String vpOrProofJson,
        boolean verified,
        OfferType offerType
    ) {
        if (!verified) {
            return VerificationConfirmResult.builder()
                .txId(txId)
                .verified(false)
                .errorMessage("Verification failed")
                .verifiedAt(Instant.parse(Instant.now().toString()))
                .build();
        }

        // OfferType에 따른 분기 처리
        if (offerType == OfferType.VerifyProofOffer) {
            // ZKP Proof 검증 결과 반환
            return confirmZkpProofVerification(txId);
        } else {
            // VP 검증 결과 반환
            return confirmVpVerification(txId, vpOrProofJson);
        }
    }

    /**
     * ZKP Proof 검증 결과 반환
     *
     * @param txId Transaction ID
     * @return VerificationConfirmResult (ZKP 결과)
     */
    private VerificationConfirmResult confirmZkpProofVerification(String txId) {
        // ZKP Proof 검증 성공 시 간단한 결과 반환
        Map<String, Object> zkpClaims = new HashMap<>();
        zkpClaims.put("zkpVerificationResult", "Successful");

        return VerificationConfirmResult.builder()
            .txId(txId)
            .verified(true)
            .extractedClaims(zkpClaims)
            .verifiedAt(Instant.parse(Instant.now().toString()))
            .build();
    }

    /**
     * VP 검증 결과 반환
     *
     * @param txId Transaction ID
     * @param vpJson VP JSON 문자열
     * @return VerificationConfirmResult (VP 클레임)
     */
    private VerificationConfirmResult confirmVpVerification(String txId, String vpJson) {
        // VP 파싱
        JsonObject vpObject = gson.fromJson(vpJson, JsonObject.class);

        // 1. Holder DID 추출
        String holderDid = extractHolderDid(vpObject);

        // 2. 제출된 VC 목록 추출
        Map<String, Object> submittedVcs = extractSubmittedVcsAsMap(vpObject);

        // 3. 클레임 추출
        Map<String, Object> extractedClaims = extractClaims(vpObject);

        // 4. 결과 반환
        return VerificationConfirmResult.builder()
            .txId(txId)
            .verified(true)
            .holderDid(holderDid)
            .submittedVcs(submittedVcs)
            .extractedClaims(extractedClaims)
            .verifiedAt(Instant.parse(Instant.now().toString()))
            .build();
    }

    @Override
    public String extractHolderDid(JsonObject vpObject) {
        if (!vpObject.has("holder")) {
            throw new VerifierSdkException(
                VerifierSdkErrorCode.SDK_INVALID_VP,
                "VP has no holder"
            );
        }
        return vpObject.get("holder").getAsString();
    }

    @Override
    public List<String> extractSubmittedVcs(JsonObject vpObject) {
        List<String> vcIds = new ArrayList<>();

        if (!vpObject.has("verifiableCredential")) {
            return vcIds;
        }

        JsonArray vcs = vpObject.getAsJsonArray("verifiableCredential");
        vcs.forEach(vcElement -> {
            JsonObject vcObject = vcElement.getAsJsonObject();
            if (vcObject.has("id")) {
                vcIds.add(vcObject.get("id").getAsString());
            }
        });

        return vcIds;
    }

    @Override
    public Map<String, Object> extractSubmittedVcsAsMap(JsonObject vpObject) {
        Map<String, Object> vcMap = new HashMap<>();

        if (!vpObject.has("verifiableCredential")) {
            return vcMap;
        }

        JsonArray vcs = vpObject.getAsJsonArray("verifiableCredential");
        vcs.forEach(vcElement -> {
            JsonObject vcObject = vcElement.getAsJsonObject();
            if (vcObject.has("id")) {
                String vcId = vcObject.get("id").getAsString();
                // VC 객체 전체를 Map으로 변환
                Map<String, Object> vcData = gson.fromJson(vcObject, Map.class);
                vcMap.put(vcId, vcData);
            }
        });

        return vcMap;
    }

    @Override
    public Map<String, Object> extractClaims(JsonObject vpObject) {
        Map<String, Object> claims = new HashMap<>();

        if (!vpObject.has("verifiableCredential")) {
            return claims;
        }

        JsonArray vcs = vpObject.getAsJsonArray("verifiableCredential");
        vcs.forEach(vcElement -> {
            JsonObject vcObject = vcElement.getAsJsonObject();

            // CredentialSubject에서 클레임 추출
            if (vcObject.has("credentialSubject")) {
                JsonObject credentialSubject = vcObject.getAsJsonObject("credentialSubject");

                if (credentialSubject.has("claims")) {
                    JsonArray claimsArray = credentialSubject.getAsJsonArray("claims");

                    claimsArray.forEach(claimElement -> {
                        JsonObject claimObject = claimElement.getAsJsonObject();

                        // Claim code와 value 추출
                        if (claimObject.has("code") && claimObject.has("value")) {
                            String claimCode = claimObject.get("code").getAsString();
                            String claimValue = claimObject.get("value").getAsString();
                            claims.put(claimCode, claimValue);
                        }
                    });
                }
            }
        });

        return claims;
    }

    @Override
    public Map<String, Object> extractFilteredClaims(
        JsonObject vpObject,
        List<String> requiredClaims
    ) {
        Map<String, Object> allClaims = extractClaims(vpObject);
        Map<String, Object> filteredClaims = new HashMap<>();

        for (String requiredClaim : requiredClaims) {
            if (allClaims.containsKey(requiredClaim)) {
                filteredClaims.put(requiredClaim, allClaims.get(requiredClaim));
            }
        }

        return filteredClaims;
    }
}
