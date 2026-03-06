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

package org.omnione.did.verifier.v1.exception;

import lombok.Getter;

/**
 * Verifier SDK 에러 코드 Enum
 *
 * SDK 전용 에러 코드는 SSDKVRF로 시작한다.
 */
@Getter
public enum VerifierSdkErrorCode {

    // ===== Verifier SDK 전용 에러 코드 (SSDKVRF) =====

    // 1. SDK 검증 오류 (SSDKVRF001xxx)
    SDK_INVALID_DID("SSDKVRF001001", "Invalid DID format.", 400),
    SDK_INVALID_PROOF("SSDKVRF001002", "Invalid proof.", 400),
    SDK_INVALID_SIGNATURE("SSDKVRF001003", "Invalid signature.", 400),
    SDK_INVALID_MULTIBASE("SSDKVRF001004", "Invalid multibase encoding.", 400),
    SDK_FIELD_REQUIRED("SSDKVRF001005", "Required field is missing.", 400),
    SDK_INVALID_VP("SSDKVRF001006", "Invalid VP format or verification failed.", 400),
    SDK_INVALID_VC("SSDKVRF001007", "Invalid VC format or verification failed.", 400),
    SDK_INVALID_NONCE("SSDKVRF001008", "Invalid nonce.", 400),
    SDK_INVALID_FILTER("SSDKVRF001009", "Invalid filter configuration.", 400),
    SDK_UNAUTHORIZED_CLAIM("SSDKVRF001010", "Unauthorized claim detected.", 400),

    // 2. SDK 프로토콜 오류 (SSDKVRF002xxx)
    SDK_INVALID_PROTOCOL_STEP("SSDKVRF002001", "Invalid protocol step.", 400),
    SDK_INVALID_TRANSACTION_STATUS("SSDKVRF002002", "Invalid transaction status.", 400),
    SDK_INVALID_SUB_TRANSACTION_STATUS("SSDKVRF002003", "Invalid sub-transaction status.", 400),
    SDK_TRANSACTION_EXPIRED("SSDKVRF002004", "Transaction has expired.", 400),
    SDK_TRANSACTION_NOT_FOUND("SSDKVRF002005", "Transaction not found.", 404),
    SDK_INVALID_OFFER("SSDKVRF002006", "Invalid offer payload.", 400),
    SDK_INVALID_PROFILE("SSDKVRF002007", "Invalid verification profile.", 400),
    SDK_POLICY_NOT_FOUND("SSDKVRF002008", "Policy not found.", 404),

    // 3. SDK 암호화 오류 (SSDKVRF003xxx)
    SDK_SIGNATURE_GENERATION_FAILED("SSDKVRF003001", "Failed to generate signature.", 500),
    SDK_SIGNATURE_VERIFICATION_FAILED("SSDKVRF003002", "Failed to verify signature.", 400),
    SDK_ECDH_KEY_AGREEMENT_FAILED("SSDKVRF003003", "ECDH key agreement failed.", 500),
    SDK_ENCRYPTION_FAILED("SSDKVRF003004", "Encryption failed.", 500),
    SDK_DECRYPTION_FAILED("SSDKVRF003005", "Decryption failed.", 500),
    SDK_E2E_SESSION_NOT_FOUND("SSDKVRF003006", "E2E session not found.", 404),
    SDK_KEY_GENERATION_FAILED("SSDKVRF003007", "Failed to generate key.", 500),
    SDK_NONCE_GENERATION_FAILED("SSDKVRF003008", "Failed to generate nonce.", 500),

    // 4. SDK 블록체인/Storage 오류 (SSDKVRF004xxx)
    SDK_DID_DOCUMENT_NOT_FOUND("SSDKVRF004001", "DID Document not found.", 404),
    SDK_DID_DOCUMENT_REGISTRATION_FAILED("SSDKVRF004002", "Failed to register DID Document.", 500),
    SDK_VC_META_NOT_FOUND("SSDKVRF004003", "VC Metadata not found.", 404),
    SDK_BLOCKCHAIN_CONNECTION_FAILED("SSDKVRF004004", "Blockchain connection failed.", 500),
    SDK_STORAGE_ERROR("SSDKVRF004005", "Storage access error.", 500),
    SDK_ZKP_CREDENTIAL_NOT_FOUND("SSDKVRF004006", "ZKP Credential Schema not found.", 404),
    SDK_ZKP_CREDENTIAL_DEF_NOT_FOUND("SSDKVRF004007", "ZKP Credential Definition not found.", 404),

    // 5. SDK Configuration 오류 (SSDKVRF005xxx)
    SDK_CONFIGURATION_ERROR("SSDKVRF005001", "Configuration error.", 500),
    SDK_INVALID_POLICY_CONFIGURATION("SSDKVRF005002", "Invalid policy configuration.", 500),
    SDK_INVALID_PROFILE_CONFIGURATION("SSDKVRF005003", "Invalid profile configuration.", 500),
    SDK_MISSING_VERIFIER_INFO("SSDKVRF005004", "Verifier information not found.", 500),

    // 6. SDK Provider 오류 (SSDKVRF006xxx)
    SDK_VERIFIER_INFO_NOT_FOUND("SSDKVRF006001", "Verifier information not found.", 404),
    SDK_CONFIG_NOT_FOUND("SSDKVRF006002", "Configuration not found.", 404),
    SDK_SESSION_NOT_FOUND("SSDKVRF006003", "Session not found.", 404),

    // 99. SDK 일반 오류 (SSDKVRF009xxx)
    SDK_UNKNOWN_ERROR("SSDKVRF009999", "Unknown SDK error occurred.", 500);

    private final String code;
    private final String message;
    private final int httpStatus;

    /**
     * VerifierSdkErrorCode 생성자
     *
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param httpStatus HTTP 상태 코드
     */
    VerifierSdkErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * 에러 코드로 메시지 조회
     *
     * @param code 에러 코드
     * @return 에러 메시지
     */
    public static String getMessageByCode(String code) {
        for (VerifierSdkErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode.getMessage();
            }
        }
        return "Unknown error code: " + code;
    }

    @Override
    public String toString() {
        return String.format(
            "VerifierSdkErrorCode{code='%s', message='%s', httpStatus=%d}",
            code,
            message,
            httpStatus
        );
    }
}
