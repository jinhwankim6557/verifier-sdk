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
 * Verifier SDK 통합 예외 클래스
 *
 * 모든 SDK 예외를 처리하는 단일 예외 클래스입니다.
 * VerifierSdkErrorCode enum으로 예외 정보를 전달합니다.
 *
 * @since 2.0.0
 */
@Getter
public class VerifierSdkException extends RuntimeException {

    /**
     * 에러 코드 (VerifierSdkErrorCode enum)
     */
    private VerifierSdkErrorCode errorCode;

    /**
     * VerifierSdkErrorCode로 예외 생성
     *
     * @param errorCode 에러 코드
     */
    public VerifierSdkException(VerifierSdkErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * VerifierSdkErrorCode와 커스텀 메시지로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 에러 메시지
     */
    public VerifierSdkException(VerifierSdkErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
