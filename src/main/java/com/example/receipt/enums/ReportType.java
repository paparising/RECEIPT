package com.example.receipt.enums;

/**
 * Enum representing the different report types available
 */
public enum ReportType {
    PDF("pdf"),
    CSV("csv");

    private final String code;

    ReportType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReportType fromCode(String code) {
        if (code == null) {
            return PDF;
        }
        for (ReportType type : ReportType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return PDF;
    }
}
