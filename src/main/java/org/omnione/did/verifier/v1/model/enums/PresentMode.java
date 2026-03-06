package org.omnione.did.verifier.v1.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * VP 제출 모드
 *
 * - DIRECT: 인가앱이 Verifier에 직접 VP를 제출
 * - INDIRECT: 인가앱 → 응대장치 → Verifier로 VP 전달
 * - PROXY: Proxy 서버를 통해 VP 전달
 */
public enum PresentMode {

    DIRECT("Direct"),
    INDIRECT("Indirect"),
    PROXY("Proxy");

    private final String displayName;

    PresentMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    @JsonValue
    public String toString() {
        return displayName;
    }

    public static PresentMode fromDisplayName(String displayName) {
        for (PresentMode mode : PresentMode.values()) {
            if (mode.displayName.equals(displayName)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("No PresentMode with displayName: " + displayName);
    }
}
