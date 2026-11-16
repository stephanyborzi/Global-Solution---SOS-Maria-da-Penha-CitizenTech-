package com.citizentech.sos.security;

import org.springframework.web.util.HtmlUtils;

public final class InputSanitizer {
    private InputSanitizer() {}
    public static String clean(String text) {
        return text == null ? null : HtmlUtils.htmlEscape(text.trim());
    }
}
