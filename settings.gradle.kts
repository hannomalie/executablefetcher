rootProject.name = "executablefetcher"
include("core")
include("cmd")
include("gradleplugin")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val kotlinVersion = "1.8.0"
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion

        id("com.palantir.graal") version "0.12.0"
    }
}
