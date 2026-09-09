package lunatech.strength.license;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig;
import lunatech.strength.utility.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ultra-low latency, zero-heap accumulation DRM license manager.
 * Performs async payload handshakes on Paper's AsyncScheduler.
 */
public class LicenseManager {
    private static final String SERVER_URL = "https://backend.lunatech-solutions.workers.dev";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson GSON = new Gson();

    private final Strength plugin;
    private final Map<String, Double> dynamicData = new ConcurrentHashMap<>();
    private volatile boolean authenticated = false;

    public LicenseManager(Strength plugin) {
        this.plugin = plugin;
    }

    public void verifyAsync() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                PluginConfig.LicenseSettings license = plugin.getConfigHandler().getConfig().license;
                String key = license.key;

                if (key == null || key.isBlank()) {
                    Logger.get().warn("[DRM] License key is missing from config!");
                    return;
                }

                String fingerprint = generateFingerprint();
                int port = plugin.getServer().getPort();

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("license_key", key);
                requestBody.addProperty("fingerprint", fingerprint);
                requestBody.addProperty("port", port);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() == 200) {
                    JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        authenticated = true;
                        if (json.has("data")) {
                            JsonObject data = json.getAsJsonObject("data");
                            for (String dataKey : data.keySet()) {
                                dynamicData.put(dataKey, data.get(dataKey).getAsDouble());
                            }
                        }
                        Logger.get().info("[DRM] License key authenticated successfully! Remote payload loaded.");
                    } else {
                        String err = json.has("error") ? json.get("error").getAsString() : "Unknown authentication error";
                        Logger.get().warn("[DRM] License authentication failed: " + err);
                    }
                } else {
                    JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                    String err = json.has("error") ? json.get("error").getAsString() : "HTTP " + response.statusCode();
                    Logger.get().warn("[DRM] License authentication failed: " + err);
                }
            } catch (Exception e) {
                Logger.get().warn("[DRM] Unable to connect to licensing server: " + e.getMessage());
            }
        });
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public double getScale() {
        return authenticated ? 1.0 : 0.0;
    }

    public double getMultiplier(String weaponKey, double fallback) {
        return dynamicData.getOrDefault(weaponKey, fallback) * getScale();
    }

    private String generateFingerprint() {
        try {
            String raw = System.getProperty("user.dir") + ":" + plugin.getServer().getPort();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "fp-" + plugin.getServer().getPort();
        }
    }
}
