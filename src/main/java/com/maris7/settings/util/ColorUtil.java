package com.maris7.settings.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {}

    public static Component color(String input) {
        String raw = input == null ? "" : input;
        String normalized = normalizeHex(raw);
        return SERIALIZER.deserialize(normalized)
                .decoration(TextDecoration.ITALIC, false);
    }

    private static String normalizeHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder(input.length() + 32);
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}
