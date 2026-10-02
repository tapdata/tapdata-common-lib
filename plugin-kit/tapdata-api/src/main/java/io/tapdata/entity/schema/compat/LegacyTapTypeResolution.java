package io.tapdata.entity.schema.compat;

import io.tapdata.entity.schema.type.TapType;

/** Result of resolving a legacy TapNumber field against an in-memory source spec. */
public final class LegacyTapTypeResolution {
    private final String connectorId;
    private final String sourceType;
    private final TapType tapType;
    private final boolean resolved;
    private final boolean legacyFloat;
    private final String code;
    private final String message;

    public LegacyTapTypeResolution(String connectorId, String sourceType, TapType tapType,
                                   boolean resolved, boolean legacyFloat, String code, String message) {
        this.connectorId = connectorId;
        this.sourceType = sourceType;
        this.tapType = tapType;
        this.resolved = resolved;
        this.legacyFloat = legacyFloat;
        this.code = code;
        this.message = message;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public TapType getTapType() {
        return tapType;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean isLegacyFloat() {
        return legacyFloat;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
