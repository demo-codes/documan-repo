package com.app.documan.util;

import com.app.documan.exception.DocumanException;

import java.util.function.Supplier;

import static org.springframework.util.StringUtils.hasText;

public final class ObjectUtils {

    private ObjectUtils() {
    }

    // evaluate supplier and throw exception if null
    public static <T> T getNonNullOrThrow(Supplier<T> supplier, String message) {
        try {
            T value = supplier.get();
            if (value == null) throw new NullPointerException("Null value");
            return value;
        }
        catch (NullPointerException exc) {
            throw new DocumanException(message, exc);
        }
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