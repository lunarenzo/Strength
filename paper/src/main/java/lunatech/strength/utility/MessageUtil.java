package lunatech.strength.utility;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.colorparser.paper.PaperComponentBuilder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Utility for sending MiniMessage parsed text to command senders and players.
 * Supports both {placeholder} and <placeholder> tag syntaxes transparently.
 * If the template string is null or empty (""), message sending is suppressed cleanly.
 */
public final class MessageUtil {

    private MessageUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Checks if a message template string is null, empty, blank, or set to a disabled key ("none", "disabled", "''", "\"\"").
     *
     * @param message the template string to evaluate
     * @return true if the message should be suppressed, false otherwise
     */
    public static boolean isNullOrEmpty(@Nullable String message) {
        if (message == null || message.isBlank()) {
            return true;
        }
        final String trimmed = message.trim();
        return "none".equalsIgnoreCase(trimmed) 
            || "disabled".equalsIgnoreCase(trimmed) 
            || "''".equals(trimmed) 
            || "\"\"".equals(trimmed);
    }

    /**
     * Sends a parsed ColorParser message to a recipient.
     * If message is null, empty, or disabled, no message is sent to chat.
     *
     * @param sender the recipient
     * @param message the raw template string
     */
    public static void send(@NotNull CommandSender sender, @Nullable String message) {
        if (isNullOrEmpty(message)) {
            return;
        }
        sender.sendMessage(ColorParser.of(message).build());
    }

    /**
     * Sends a parsed ColorParser message with a single placeholder pair to a recipient.
     * Supports both {placeholder} and <placeholder> formats.
     * If message is null, empty, or disabled, no message is sent to chat.
     *
     * @param sender the recipient
     * @param message the raw template string
     * @param placeholder the placeholder key
     * @param value the replacement value
     */
    public static void send(@NotNull CommandSender sender, @Nullable String message, @NotNull String placeholder, @NotNull String value) {
        if (isNullOrEmpty(message)) {
            return;
        }
        final String formatted = message
            .replace("{" + placeholder + "}", value)
            .replace("<" + placeholder + ">", value);
        sender.sendMessage(ColorParser.of(formatted).with(placeholder, value).build());
    }

    /**
     * Sends a parsed ColorParser message with two placeholder pairs to a recipient.
     * Supports both {placeholder} and <placeholder> formats.
     * If message is null, empty, or disabled, no message is sent to chat.
     *
     * @param sender the recipient
     * @param message the raw template string
     * @param k1 first placeholder key
     * @param v1 first replacement value
     * @param k2 second placeholder key
     * @param v2 second replacement value
     */
    public static void send(@NotNull CommandSender sender, @Nullable String message, @NotNull String k1, @NotNull String v1, @NotNull String k2, @NotNull String v2) {
        if (isNullOrEmpty(message)) {
            return;
        }
        final String formatted = message
            .replace("{" + k1 + "}", v1)
            .replace("<" + k1 + ">", v1)
            .replace("{" + k2 + "}", v2)
            .replace("<" + k2 + ">", v2);
        sender.sendMessage(ColorParser.of(formatted).with(k1, v1).with(k2, v2).build());
    }

    /**
     * Sends a parsed ColorParser message with key-value placeholders to a recipient.
     * Supports both {placeholder} and <placeholder> formats.
     * If message is null, empty, or disabled, no message is sent to chat.
     *
     * @param sender the recipient
     * @param message the raw template string
     * @param placeholders key-value replacement mapping
     */
    public static void send(@NotNull CommandSender sender, @Nullable String message, @NotNull Map<String, String> placeholders) {
        if (isNullOrEmpty(message)) {
            return;
        }
        String formatted = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                formatted = formatted
                    .replace("{" + entry.getKey() + "}", entry.getValue())
                    .replace("<" + entry.getKey() + ">", entry.getValue());
            }
        }
        PaperComponentBuilder builder = ColorParser.of(formatted);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                builder = builder.with(entry.getKey(), entry.getValue());
            }
        }
        sender.sendMessage(builder.build());
    }
}
