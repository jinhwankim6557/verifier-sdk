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

package org.omnione.did.verifier.v1.config;

import org.omnione.did.verifier.v1.core.VerifierServiceImpl;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.protocol.VerifierService;
import org.omnione.did.verifier.v1.provider.*;

/**
 * Verifier SDK 빌더
 *
 * SDK 초기화 책임을 Application이 아닌 SDK 자체로 이동합니다.
 * 7개 Provider 누락 시 런타임이 아닌 build() 시점에 즉시 실패합니다.
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * VerifierService verifierService = new VerifierSdkBuilder()
 *     .configProvider(configProvider)
 *     .verifierInfoProvider(verifierInfoProvider)
 *     .sessionProvider(sessionProvider)
 *     .storageProvider(storageProvider)
 *     .transactionProvider(transactionProvider)
 *     .nonceGenerator(nonceGenerator)
 *     .cryptoHelper(cryptoHelper)
 *     .build();
 * }</pre>
 *
 * @since 2.0.0
 */
public class VerifierSdkBuilder {

    private VerificationConfigProvider configProvider;
    private VerifierInfoProvider verifierInfoProvider;
    private EcdhSessionProvider sessionProvider;
    private StorageProvider storageProvider;
    private TransactionProvider transactionProvider;
    private NonceGenerator nonceGenerator;
    private CryptoHelper cryptoHelper;

    public VerifierSdkBuilder configProvider(VerificationConfigProvider configProvider) {
        this.configProvider = configProvider;
        return this;
    }

    public VerifierSdkBuilder verifierInfoProvider(VerifierInfoProvider verifierInfoProvider) {
        this.verifierInfoProvider = verifierInfoProvider;
        return this;
    }

    public VerifierSdkBuilder sessionProvider(EcdhSessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
        return this;
    }

    public VerifierSdkBuilder storageProvider(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
        return this;
    }

    public VerifierSdkBuilder transactionProvider(TransactionProvider transactionProvider) {
        this.transactionProvider = transactionProvider;
        return this;
    }

    public VerifierSdkBuilder nonceGenerator(NonceGenerator nonceGenerator) {
        this.nonceGenerator = nonceGenerator;
        return this;
    }

    public VerifierSdkBuilder cryptoHelper(CryptoHelper cryptoHelper) {
        this.cryptoHelper = cryptoHelper;
        return this;
    }

    /**
     * VerifierService 인스턴스를 생성합니다.
     *
     * 7개 Provider가 모두 주입되지 않은 경우 즉시 예외를 던집니다.
     *
     * @return VerifierService SDK Facade
     * @throws VerifierSdkException Provider 누락 시
     */
    public VerifierService build() {
        if (configProvider == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                "VerificationConfigProvider is required.");
        }
        if (verifierInfoProvider == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                "VerifierInfoProvider is required.");
        }
        if (sessionProvider == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                "EcdhSessionProvider is required.");
        }
        if (storageProvider == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                "StorageProvider is required.");
        }
        if (transactionProvider == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                "TransactionProvider is required.");
        }
        if (nonceGenerator == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                "NonceGenerator is required.");
        }
        if (cryptoHelper == null) {
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                "CryptoHelper is required.");
        }
        return new VerifierServiceImpl(
            configProvider,
            verifierInfoProvider,
            sessionProvider,
            storageProvider,
            transactionProvider,
            nonceGenerator,
            cryptoHelper
        );
    }
}
