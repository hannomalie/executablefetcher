plugins {
    kotlin("jvm") version "1.8.0"
    `java-gradle-plugin`
}

allprojects {
    group = "de.hanno"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(project(":core"))

    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.24.2")
}

gradlePlugin {
    plugins {
        create("executableFetcher") {
            id = "de.hanno.executablefetcher"
            implementationClass = "de.hanno.ExecutableFetcher"
        }
    }
}
