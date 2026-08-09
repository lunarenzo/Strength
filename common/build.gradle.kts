plugins {
}

dependencies {
    // Core dependencies
    api(projects.api)
    api(libs.morepaperlib)

    // API
    api(libs.javasemver) // Required by VersionWatch
    api(libs.versionwatch)
    api(libs.wordweaver)
    api(libs.bundles.configurate.core) {
        isTransitive = false
    }
    api(libs.bundles.configurate.yaml) {
        isTransitive = false
    }
    annotationProcessor(libs.configurate.interfaces.ap)
    api(libs.colorparser.common) {
        exclude("net.kyori")
    }
    api(libs.threadutil.common)

    // Messaging service clients
    compileOnly(libs.bundles.messagingclients)

    // Testing - Core
    testImplementation(libs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.slf4j)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly(libs.paper.api)

    // Testing - Messaging service clients
    testImplementation(libs.bundles.messagingclients)
}