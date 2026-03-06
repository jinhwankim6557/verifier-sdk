package org.omnione.did.verifier.v1.model.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.omnione.did.verifier.v1.model.enums.OfferType;
import org.omnione.did.verifier.v1.model.enums.PresentMode;

import java.util.List;

/**
 * VP Offer Payload DTO
 * Verifier가 Holder에게 VP 제출을 요청하기 위한 Offer 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VpOfferPayload {
    /**
     * Offer ID (Optional)
     * - Dynamic QR: offerId 포함
     * - Static QR: offerId 없이 재사용 가능
     */
    @SerializedName("offerId")
    private String offerId;

    /**
     * Offer Type
     * - VerifyOffer: VP 제출 offer
     * - VerifyProofOffer: ZKP Proof 검증 offer
     */
    @SerializedName("type")
    private OfferType type;

    /**
     * 제출 모드
     * - DIRECT: 인가앱 → Verifier 직접 제출
     * - INDIRECT: 인가앱 → 응대장치 → Verifier
     */
    @SerializedName("mode")
    private PresentMode mode;

    /**
     * 응대장치 식별자
     * 예: "N13-08", "WEB-BROWSER"
     */
    @SerializedName("device")
    private String device;

    /**
     * 서비스 식별자
     * 예: "login", "signup", "permission"
     */
    @SerializedName("service")
    private String service;

    /**
     * Verifier API endpoint 목록
     * - Direct 모드: 필수
     * - Indirect 모드: Optional
     */
    @SerializedName("endpoints")
    private List<String> endpoints;

    /**
     * 제출 가능 종료일시 (ISO-8601 형식, Optional)
     */
    @SerializedName("validUntil")
    private String validUntil;

    /**
     * Offer 잠김 여부 (Optional)
     * - true: passcode 필요
     * - false: passcode 불필요
     */
    @SerializedName("locked")
    @Builder.Default
    private Boolean locked = false;
}
