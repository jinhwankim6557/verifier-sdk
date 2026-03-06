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

package org.omnione.did.verifier.v1.model.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ZKP Policy DTO
 *
 * ZKP Policy 정보 (Application에서 DB 조회 후 전달)
 *
 * @since 2.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZkpPolicy {
    private String policyId;
    private String title;
    private String description;
    private String name;  // ProofRequest.name
    private String version;  // ProofRequest.version
    private String requestedAttributes;  // JSON 문자열
    private String requestedPredicates;  // JSON 문자열
    private String curve;  // E2E 암호화 Curve (Secp256r1, Secp256k1)
    private String cipher;  // E2E 암호화 Cipher (AES-256-CBC, AES-256-CTR)
    private String padding;  // E2E 암호화 Padding (PKCS5)
}
