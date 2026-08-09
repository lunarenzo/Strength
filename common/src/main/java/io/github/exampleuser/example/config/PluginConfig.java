package io.github.exampleuser.example.config;

import io.github.exampleuser.example.config.exception.ConfigValidationException;
import io.github.exampleuser.example.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

@ConfigSerializable
public class PluginConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    @Exclude
    public int configVersion() {
        return configVersion;
    }

    @Override
    @Exclude
    public @NotNull Map<Integer, Migration> migrations() {
        return Map.of();
    }

    @Override
    @Exclude
    public void validate() throws ConfigValidationException {
        if (messaging.pollingInterval >= messaging.cleanupInterval) {
            throw new ConfigValidationException(
                "messaging.polling-interval (" + messaging.pollingInterval + "ms) " +
                    "must be less than messaging.cleanup-interval (" + messaging.cleanupInterval + "ms)"
            );
        }
    }

    @Comment("Update Checker Settings")
    public UpdateChecker updateChecker = new UpdateChecker();

    @ConfigSerializable
    public static class UpdateChecker {
        @Comment("Should the plugin check for plugin updates on startup?")
        public boolean enabled = true;

        @Comment("Send update notifications to the console?")
        public boolean console = true;

        @Comment("Send update notifications to opped players on join?")
        public boolean op = true;
    }

    @Comment("Language, specify the language file to use, for example `en_US` which will load `/lang/en_US.json`")
    public String language = "en_US";

    @Comment("Message Broker Settings")
    public Messaging messaging = new Messaging();

    @ConfigSerializable
    public static class Messaging {
        @Comment("Enable or disable the message broker\nOnly required when running on a server network (BungeeCord, Velocity, etc.)")
        public boolean enabled = false;

        @Comment("How often to poll for new messages (in milliseconds)\nMust be less than cleanup-interval, ideally 1/3 or less")
        public int pollingInterval = 1000;

        @Comment("How often to clean up old messages (in milliseconds)\nMust be at least 3x the polling-interval")
        public int cleanupInterval = 30000;

        @Comment("Available broker types: \"plugin\", \"redis\", \"rabbitmq\", \"nats\"")
        public io.github.exampleuser.example.messaging.broker.BrokerType type = io.github.exampleuser.example.messaging.broker.BrokerType.PLUGIN_MESSAGING;

        @Comment("One or more broker addresses. A plain string works for a single broker; use a YAML list for clusters:\n  - node1:6379\n  - node2:6379\nDefault port varies by broker: Redis 6379, RabbitMQ 5672, NATS 4222")
        public java.util.List<String> addresses = java.util.List.of("localhost:6379");

        @Comment("Authentication credentials (used with auth-method: \"password\")")
        public String username = "";
        public String password = "";

        @Comment("Advanced broker configuration")
        public Advanced advanced = new Advanced();

        @ConfigSerializable
        public static class Advanced {
            @Comment(
                "Authentication method. Supported values per broker:\n" +
                    "  Redis:    \"password\" (username + password or password-only)\n" +
                    "            \"token\"    (Redis AUTH token)\n" +
                    "  RabbitMQ: \"password\" (PLAIN mechanism)\n" +
                    "            \"token\"    (token as password)\n" +
                    "            \"certificate\" (mutual TLS, no credentials needed; cert CN maps to a RabbitMQ user)\n" +
                    "  NATS:     \"password\" (username + password)\n" +
                    "            \"token\"    (auth token)\n" +
                    "            \"nkey\"     (NKey seed file, optionally paired with a JWT file)\n" +
                    "            \"credentials\" (combined JWT + NKey .creds file)"
            )
            public String authMethod = "password";

            @Comment("Auth token used with auth-method: \"token\" (JWT, API key, Redis AUTH token, etc.)")
            public String authToken = "";

            @Comment("SSL/TLS configuration")
            public SSL ssl = new SSL();

            @ConfigSerializable
            public static class SSL {
                @Comment("Enable TLS/SSL for the broker connection")
                public boolean enabled = false;

                @Comment("Path to the client certificate in PEM format (.crt or .pem)\nRequired for mutual TLS (auth-method: \"certificate\") and optional for TLS identity")
                public String certPath = "";

                @Comment(
                    "Path to the client private key in PKCS#8 PEM format (.pem)\n" +
                        "Required alongside cert-path for mutual TLS.\n" +
                        "If you have a PKCS#1 key (-----BEGIN RSA PRIVATE KEY-----), convert it first:\n" +
                        "  openssl pkcs8 -topk8 -nocrypt -in key.pem -out key.pkcs8.pem"
                )
                public String keyPath = "";

                @Comment("Path to the Certificate Authority file in PEM format (.crt or .pem)\nCA bundles (multiple certificates in one file) are supported.\nLeave empty to use the JVM's built-in trust store.")
                public String caPath = "";

                @Comment("Verify the server's TLS certificate against the trust store\nDisable only in development, not in production")
                public boolean verifyServerCert = true;

                @Comment("Verify that the server hostname matches the certificate CN/SAN\nDisable only when connecting via IP address or with a wildcard certificate")
                public boolean verifyHostname = true;
            }

            @Comment("RabbitMQ-specific settings")
            public RabbitMQ rabbitmq = new RabbitMQ();

            @ConfigSerializable
            public static class RabbitMQ {
                @Comment("The RabbitMQ virtual host to connect to")
                public String virtualHost = "/";
            }

            @Comment("NATS-specific settings")
            public Nats nats = new Nats();

            @ConfigSerializable
            public static class Nats {
                @Comment("Path to the NKey seed file, used with auth-method: \"nkey\"")
                public String nkeySeedPath = "";

                @Comment("Path to the JWT token file, used with auth-method: \"nkey\" alongside nkey-seed-path.\nOmit for challenge-only NKey auth (no user JWT).")
                public String jwtFilePath = "";

                @Comment("Path to a combined credentials file (.creds) containing both JWT and NKey\nUsed with auth-method: \"credentials\"")
                public String credentialsPath = "";
            }
        }
    }
}
