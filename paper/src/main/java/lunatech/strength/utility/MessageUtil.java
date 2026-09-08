package lunatech.strength.utility;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
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
     * Sends a parsed ColorParser message to a recipient.
     * If message is null or empty, no message is sent.
     *
     * @param sender the recipient
     * @param message the raw template string
     */
    public static void send(@NotNull CommandSender sender, @Nullable String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        sender.sendMessage(ColorParser.of(message).build());
    }

    /**
     * Sends a parsed ColorParser message with a single placeholder pair to a recipient.
     * Supports both {placeholder} and <placeholder> formats.
     *
     * @param sender the recipient
     * @param message the raw template string
     * @param placeholder the placeholder key
     * @param value the replacement value
     */
    public static void send(@NotNull CommandSender sender, @Nullable String message, @NotNull String placeholder, @NotNull String value) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        final String formatted = message
            .replace("{" + placeholder + "}", value)
            .replace("<" + placeholder + ">", value);
        sender.sendMessage(ColorParser.of(formatted).with(placeholder, value).build());
    }

    /**
     * Sends a parsed ColorParser message with key-value placeholders to a recipient.
     * Supports both {placeholder} and <placeholder> formats.
     *
     * @param sender the recipient
     * @param message the raw template string
     * @param placeholders key-value replacement mapping
     */
    public static void send(@NotNull CommandSender sender, @Nullable String message, @NotNull Map<String, String> placeholders) {
        if (message == null || message.trim().isEmpty()) {
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
        io.github.milkdrinkers.colorparser.paper.PaperComponentBuilder builder = ColorParser.of(formatted);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                builder = builder.with(entry.getKey(), entry.getValue());
            }
        }
        sender.sendMessage(builder.build());
    }
}
