import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("pmd")
    alias(libs.plugins.shadow) // Shades and relocates dependencies, see https://gradleup.com/shadow/
    alias(libs.plugins.run.paper) // Built in test server using runServer and runMojangMappedServer tasks
    alias(libs.plugins.plugin.yml.bukkit) // Automatic plugin.yml generation
    alias(libs.plugins.plugin.yml.paper) // Automatic plugin.yml generation
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.11.0"
    ruleSetFiles = files(rootProject.file("config/pmd/pmd-ruleset.xml"))
    ruleSets = listOf()
    isIgnoreFailures = true
}

dependencies {
    // Core dependencies
    implementation(projects.common)

    // API
    implementation(libs.commandapi.shade.paper)
    api(libs.colorparser.paper) {
        exclude("net.kyori")
    }
    api(libs.threadutil.bukkit)

    // Plugin dependencies
    implementation(libs.bstats)
    compileOnly(libs.packetevents)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.authme) {
        exclude("me.clip.placeholderapi.libs", "kyori")
    }
    compileOnly(libs.pvpmanager)
    compileOnly(libs.worldguard) {
        isTransitive = false
    }
    compileOnly(libs.worldguard.core) {
        isTransitive = false
    }
    compileOnly(libs.worldedit) {
        isTransitive = false
    }
    compileOnly(libs.worldedit.core) {
        isTransitive = false
    }
    // Testing - Core
    testImplementation(libs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.slf4j)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly(libs.paper.api)
}

tasks {
    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveClassifier.set("")

        // Shadow classes
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${project.relocationPackage}.${targetPkg}")

        reloc("space.arim.morepaperlib", "morepaperlib")
        reloc("io.github.milkdrinkers.javasemver", "javasemver")
        reloc("io.github.milkdrinkers.versionwatch", "versionwatch")
        reloc("io.github.milkdrinkers.crate", "crate")
        reloc("io.github.milkdrinkers.colorparser", "colorparser")
        reloc("io.github.milkdrinkers.threadutil", "threadutil")
        reloc("org.snakeyaml", "snakeyaml")
        reloc("org.json", "json")
        reloc("dev.jorel.commandapi", "commandapi")
        reloc("org.bstats", "bstats")

        reloc("io.leangen.geantyref", "geantyref")
        reloc("org.yaml", "yaml")
        reloc("org.spongepowered", "spongepowered")

        mergeServiceFiles()
    }

    runServer {
        // Configure the Minecraft version for our task.
        minecraftVersion(libs.versions.paper.run.get())

        // IntelliJ IDEA debugger setup: https://docs.papermc.io/paper/dev/debugging#using-a-remote-debugger
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true", "-DIReallyKnowWhatIAmDoingISwear", "-Dpaper.playerconnection.keepalive=6000")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)

        // Automatically install dependencies
        downloadPlugins {
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
            github("PlaceholderAPI", "PlaceholderAPI", "2.12.2", "PlaceholderAPI-2.12.2.jar")
            hangar("ViaVersion", "5.8.1")
            hangar("ViaBackwards", "5.8.1")
        }
    }
}

bukkit { // Options: https://docs.eldoria.de/pluginyml/bukkit/
    // Plugin main class (required)
    main = rootProject.entryPointClass

    // Plugin Information
    name = rootProject.name
    prefix = rootProject.name
    version = "${rootProject.version}"
    description = "${rootProject.description}"
    authors = rootProject.authors
    contributors = rootProject.contributors
    apiVersion = libs.versions.paper.api.get().substringBefore("-R").substringBefore("-pre")
    foliaSupported = true

    // Misc properties
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP or POSTWORLD
    depend = listOf()
    softDepend = listOf("Vault", "PlaceholderAPI", "AuthMe", "PvPManager", "WorldGuard")
    loadBefore = listOf()
    provides = listOf()
}

paper { // Options: https://docs.eldoria.de/pluginyml/paper/
    main = rootProject.entryPointClass
    loader = rootProject.entryPointClass + "PluginLoader"
    generateLibrariesJson = true
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    // Info
    name = rootProject.name
    prefix = rootProject.name
    version = "${rootProject.version}"
    description = "${rootProject.description}"
    authors = rootProject.authors
    contributors = rootProject.contributors
    apiVersion = libs.versions.paper.api.get().substringBefore("-R").substringBefore("-pre")
    foliaSupported = true

    // Dependencies
    hasOpenClassloader = true
    bootstrapDependencies {}
    serverDependencies {
        register("Vault") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("AuthMe") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("PvPManager") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("WorldGuard") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
}