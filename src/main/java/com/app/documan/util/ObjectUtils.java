package com.app.documan.util;

import java.util.function.Supplier;

import static org.springframework.util.StringUtils.hasText;

public final class ObjectUtils {

    private ObjectUtils() {
    }

    public static String getNonBlankOrDefault(Supplier<String> supplier, String defaultValue) {
        try {
            String value = supplier.get();
            return (hasText(value)) ? value : defaultValue;
        }
        catch (NullPointerException exc) {
            return defaultValue;
        }
    }


}