plugins {
    kotlin("jvm")
    `java-gradle-plugin`
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
            implementationClass = "de.hanno.executablefetcher.ExecutableFetcher"
        }
    }
}
