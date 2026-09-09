package lunatech.strength.license;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig;
import lunatech.strength.utility.Logger;

import java.io.File;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ultra-low latency DRM manager with hardware MAC binding, bitwise salt obfuscation,
 * and zero-heap accumulation offline token verification.
 */
public class LicenseManager {
    private static final String SERVER_URL = "https://backend.lunatech-solutions.workers.dev";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson GSON = new Gson();
    private static final long CACHE_TTL_MS = 86_400_000L;

    private final Strength plugin;
    private final Map<String, Double> dynamicData = new ConcurrentHashMap<>();
    private volatile boolean authenticated = false;
    private volatile int stateChecksum = -1;
    private volatile int keyHash = 0;
    private volatile int fpHash = 0;

    public LicenseManager(Strength plugin) {
        this.plugin = plugin;
        loadOfflineCache();
    }

    private void loadOfflineCache() {
        try {
            final File cacheFile = new File(plugin.getDataFolder(), ".license_cache");
            if (!cacheFile.exists()) return;

            final String content = Files.readString(cacheFile.toPath(), StandardCharsets.UTF_8).trim();
            final String[] parts = content.split(":");
            if (parts.length != 2) return;

            final String tokenHash = parts[0];
            final long expiry = Long.parseLong(parts[1]);

            if (System.currentTimeMillis() >= expiry) return;

            final String key = plugin.getConfigHandler().getConfig().license.key;
            if (key == null || key.isBlank()) return;

            final String fp = generateFingerprint();
            final String expected = computeTokenHash(fp, key, expiry);

            if (expected.equals(tokenHash)) {
                setAuthState(true, key, fp);
                Logger.get().info("[DRM] Hardware-bound offline token verified cleanly!");
            }
        } catch (Exception ignored) {}
    }

    private void saveOfflineCache(String key, String fp) {
        try {
            final File folder = plugin.getDataFolder();
            if (!folder.exists()) folder.mkdirs();
            final File cacheFile = new File(folder, ".license_cache");

            final long expiry = System.currentTimeMillis() + CACHE_TTL_MS;
            final String tokenHash = computeTokenHash(fp, key, expiry);
            final String payload = tokenHash + ":" + expiry;

            Files.writeString(cacheFile.toPath(), payload, StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }

    // Dynamic bitwise salt construction - no plaintext salt in constant pool
    private String computeTokenHash(String fp, String key, long expiry) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final long mixedSeed = ((long) key.hashCode() << 32) ^ fp.hashCode() ^ expiry;
            final String dynamicSalt = Long.toHexString(mixedSeed ^ 0x5F3759DFL) + "_SALT_" + (key.hashCode() ^ 0x1F3F);
            final String raw = fp + ":" + key + ":" + expiry + ":" + dynamicSalt;
            final byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private void setAuthState(boolean auth, String key, String fp) {
        this.authenticated = auth;
        if (auth) {
            this.keyHash = key.hashCode();
            this.fpHash = fp.hashCode();
            this.stateChecksum = keyHash ^ fpHash;
        } else {
            this.stateChecksum = -1;
        }
    }

    public void verifyAsync() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                final PluginConfig.LicenseSettings license = plugin.getConfigHandler().getConfig().license;
                final String key = license.key;

                if (key == null || key.isBlank()) {
                    Logger.get().warn("[DRM] License key is missing from config!");
                    setAuthState(false, "", "");
                    return;
                }

                final String fingerprint = generateFingerprint();
                final int port = plugin.getServer().getPort();

                final JsonObject requestBody = new JsonObject();
                requestBody.addProperty("license_key", key);
                requestBody.addProperty("fingerprint", fingerprint);
                requestBody.addProperty("port", port);

                final HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                        .build();

                final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() == 200) {
                    final JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        setAuthState(true, key, fingerprint);
                        saveOfflineCache(key, fingerprint);

                        if (json.has("data")) {
                            final JsonObject data = json.getAsJsonObject("data");
                            for (String dataKey : data.keySet()) {
                                dynamicData.put(dataKey, data.get(dataKey).getAsDouble());
                            }
                        }
                        Logger.get().info("[DRM] License key authenticated successfully!");
                    } else {
                        final String err = json.has("error") ? json.get("error").getAsString() : "Unknown authentication error";
                        Logger.get().warn("[DRM] License authentication failed: " + err);
                        if (!authenticated) {
                            setAuthState(false, key, fingerprint);
                        }
                    }
                } else {
                    final JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
                    final String err = json.has("error") ? json.get("error").getAsString() : "HTTP " + response.statusCode();
                    Logger.get().warn("[DRM] License authentication failed: " + err);
                    if (!authenticated) {
                        setAuthState(false, key, fingerprint);
                    }
                }
            } catch (Exception e) {
                Logger.get().warn("[DRM] Unable to connect to licensing server: " + e.getMessage());
            }
        });
    }

    public boolean isAuthenticated() {
        return authenticated && ((stateChecksum ^ keyHash ^ fpHash) == 0);
    }

    public double getScale() {
        return isAuthenticated() ? 1.0 : 0.0;
    }

    public double getScale(int seed) {
        final int authMask = (stateChecksum ^ keyHash ^ fpHash);
        return (authMask == 0 && authenticated && ((stateChecksum ^ seed) != seed)) ? 1.0 : 0.0;
    }

    public double scale(double baseVal) {
        return baseVal * getScale();
    }

    public int scaleInt(int baseVal) {
        return (int) (baseVal * getScale());
    }

    public double getMultiplier(String weaponKey, double fallback) {
        return dynamicData.getOrDefault(weaponKey, fallback) * getScale();
    }

    // Hardware MAC + Path + Port Fingerprinting to prevent container cloning
    private String generateFingerprint() {
        try {
            final StringBuilder raw = new StringBuilder(System.getProperty("user.dir"))
                    .append(":").append(plugin.getServer().getPort());

            // Append hardware network interface MAC address to prevent container/dir cloning
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                if (interfaces != null) {
                    while (interfaces.hasMoreElements()) {
                        final NetworkInterface ni = interfaces.nextElement();
                        final byte[] mac = ni.getHardwareAddress();
                        if (mac != null && mac.length > 0) {
                            raw.append(":").append(HexFormat.of().formatHex(mac));
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}

            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "fp-" + plugin.getServer().getPort();
        }
    }
}
