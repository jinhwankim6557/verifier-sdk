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

/**
 * SDK Service Provider Interfaces (SPI)
 *
 * 이 패키지는 SDK가 제공하는 서비스의 인터페이스를 정의합니다.
 * Application은 기본 구현체(service 패키지)를 사용하거나,
 * 필요시 인터페이스를 직접 구현하여 커스텀 로직을 적용할 수 있습니다.
 *
 * <h2>패키지 구조</h2>
 * <ul>
 *   <li>{@code api/} - Application이 구현해서 SDK에 제공하는 인터페이스 (SPI)</li>
 *   <li>{@code spi/} - SDK가 제공하는 서비스 인터페이스 (이 패키지)</li>
 *   <li>{@code service/} - SDK 기본 구현체 (Default*)</li>
 * </ul>
 *
 * <h2>주요 인터페이스</h2>
 * <ul>
 *   <li>{@link org.omnione.did.verifier.v1.spi.VerifierService} - 통합 Facade</li>
 *   <li>{@link org.omnione.did.verifier.v1.spi.VpOfferService} - VP Offer 생성</li>
 *   <li>{@link org.omnione.did.verifier.v1.spi.VpProfileService} - Verify Profile 생성</li>
 *   <li>{@link org.omnione.did.verifier.v1.spi.VpVerificationService} - VP 검증</li>
 *   <li>{@link org.omnione.did.verifier.v1.spi.VerificationConfirmService} - 검증 확인</li>
 *   <li>{@link org.omnione.did.verifier.v1.spi.ZkpProofVerificationService} - ZKP 검증</li>
 * </ul>
 *
 * @since 2.0.0
 */
package org.omnione.did.verifier.v1.protocol;
