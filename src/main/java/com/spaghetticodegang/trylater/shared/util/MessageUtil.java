package com.spaghetticodegang.trylater.shared.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for resolving localized messages from resource bundles.
 * <p>
 * Uses Spring's {@link MessageSource} and the current locale from {@link LocaleContextHolder}
 * to retrieve internationalized texts for validation messages, exceptions, and other output.
 */
@Component
@RequiredArgsConstructor
public class MessageUtil {

    private final MessageSource messageSource;

    /**
     * Resolves a localized message for the given key using the current locale.
     *
     * @param key the message key to look up
     * @return the localized message string
     */
    public String get(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Resolves a localized message for the given key with placeholders replaced by the given arguments.
     *
     * @param key  the message key to look up
     * @param args the arguments to replace in the message (e.g., placeholders like {0}, {1}, ...)
     * @return the formatted, localized message string
     */
    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
