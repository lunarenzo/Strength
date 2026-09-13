plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("script-runtime"))
    implementation(libs.jgit)
    implementation("com.guardsquare:proguard-gradle:7.5.0")
}

gradlePlugin {
    plugins {
        register("projectextensions") {
            implementationClass = "ProjectExtensionsPlugin"
        }
    }
    plugins {
        register("versioning") {
            implementationClass = "versioning.VersioningPlugin"
        }
    }
}