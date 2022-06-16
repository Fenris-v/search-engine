package main.enums;

import lombok.Getter;

public enum ExcludingDocumentElements {
    HEADER("header"),
    FOOTER("footer"),
    NAV("nav");

    @Getter
    private final String value;

    ExcludingDocumentElements(String value) {
        this.value = value;
    }
}
