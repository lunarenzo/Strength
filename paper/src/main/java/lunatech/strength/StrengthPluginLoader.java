package lunatech.strength;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class StrengthPluginLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        PluginLibraries pluginLibraries = load();
        pluginLibraries.asRepositories().forEach(resolver::addRepository);
        pluginLibraries.asDependencies().forEach(resolver::addDependency);
        classpathBuilder.addLibrary(resolver);
    }

    /**
     * Load the plugin libraries from the `paper-libraries.json` file.
     * <p>
     * The file is read using GSON and parsed into a {@link PluginLibraries} instance.
     *
     * @return A {@link PluginLibraries} instance containing repositories and dependencies.
     */
    public PluginLibraries load() {
        try (
            final InputStream is = getClass().getResourceAsStream("/paper-libraries.json")
        ) {
            if (is == null) {
                return new PluginLibraries(java.util.Map.of(), java.util.List.of());
            }

            try (
                final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                final BufferedReader br = new BufferedReader(isr)
            ) {
                final StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return parseJson(sb.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PluginLibraries parseJson(String json) {
        try {
            return new com.google.gson.Gson().fromJson(json, PluginLibraries.class);
        } catch (Throwable t) {
            final java.util.Map<String, String> repos = new java.util.HashMap<>();
            final java.util.List<String> deps = new java.util.ArrayList<>();

            final java.util.regex.Matcher repoMatcher = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"(https?://[^\"]+)\"").matcher(json);
            while (repoMatcher.find()) {
                repos.put(repoMatcher.group(1), repoMatcher.group(2));
            }

            final java.util.regex.Matcher depMatcher = java.util.regex.Pattern.compile("\"([a-zA-Z0-9_.-]+:[a-zA-Z0-9_.-]+:[a-zA-Z0-9_.-]+)\"").matcher(json);
            while (depMatcher.find()) {
                deps.add(depMatcher.group(1));
            }

            return new PluginLibraries(repos, deps);
        }
    }

    /**
     * Represents the plugin libraries as defined in the exported `paper-libraries.json` file.
     * This includes repositories and dependencies.
     * <p>
     * The repositories are a map of repository names to URLs, and the dependencies are a list of artifact definitions.
     * <p>
     * The {@link #asDependencies()} method converts the dependencies to a stream of {@link Dependency} instances,
     * and the {@link #asRepositories()} method converts the repositories to a stream of {@link RemoteRepository} instances.
     */
    public record PluginLibraries(@Nullable Map<String, String> repositories, @Nullable List<String> dependencies) {
        /**
         * These URLs will be replaced with the default mirror.
         */
        private static final Set<String> MAVEN_CENTRAL_URLS = Set.of(
            "https://repo1.maven.org/maven2",
            "http://repo1.maven.org/maven2",
            "https://repo.maven.apache.org/maven2",
            "http://repo.maven.apache.org/maven2"
        );
        /**
         * The default Maven Central mirror URL as configured by the Paper server.
         * This is used to replace any of the {@link #MAVEN_CENTRAL_URLS} in the repositories.
         *
         * @implNote This is obtained using reflection to support pre 1.21.7 versions of Paper that do not have this field.
         */
        private static final String MAVEN_CENTRAL_MIRROR = getMavenCentralMirror();
        /**
         * A fallback Maven Central mirror URL in case the Paper server does not define one.
         * This is used to replace any of the {@link #MAVEN_CENTRAL_URLS} in the repositories.
         */
        private static final String FALLBACK_MAVEN_CENTRAL_MIRROR = "https://maven-central.storage-download.googleapis.com/maven2";

        /**
         * Get the declared dependencies as a stream of {@link Dependency}.
         *
         * @return A stream of {@link Dependency} instances.
         */
        public Stream<Dependency> asDependencies() {
            if (dependencies == null)
                return Stream.empty();

            return dependencies.stream()
                .map(d -> new Dependency(new DefaultArtifact(d), null));
        }

        /**
         * Get the declared repositories as a stream of {@link RemoteRepository}.
         *
         * @return A stream of {@link RemoteRepository} instances.
         * @implNote This method filters out any maven central repositories as defined in {@link #MAVEN_CENTRAL_URLS}, and replaces them with {@link #MAVEN_CENTRAL_MIRROR}.
         */
        public Stream<RemoteRepository> asRepositories() {
            if (repositories == null)
                return Stream.empty();

            final boolean hasMavenCentral = repositories.values().stream()
                .anyMatch(url -> MAVEN_CENTRAL_URLS.stream().anyMatch(url::startsWith));

            final Stream<RemoteRepository> filtered = repositories.entrySet().stream()
                .filter(entry -> MAVEN_CENTRAL_URLS.stream().noneMatch(url -> entry.getValue().startsWith(url)))
                .map(entry -> new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue()).build());

            if (hasMavenCentral)
                return Stream.concat(Stream.of(new RemoteRepository.Builder("MavenCentral", "default", MAVEN_CENTRAL_MIRROR).build()), filtered);

            return filtered;
        }

        /**
         * Get the default Maven Central mirror URL as configure by the Paper server, or fallback.
         *
         * @return A Maven Central mirror URL.
         * @implNote Uses reflection to support pre 1.21.7 versions of Paper that do not have this field.
         */
        private static String getMavenCentralMirror() {
            try {
                final Field field = MavenLibraryResolver.class.getDeclaredField("MAVEN_CENTRAL_DEFAULT_MIRROR");
                return (String) field.get(null);
            } catch (Exception e) {
                return FALLBACK_MAVEN_CENTRAL_MIRROR;
            }
        }
    }
}