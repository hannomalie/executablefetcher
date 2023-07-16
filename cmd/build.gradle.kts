plugins {
    kotlin("jvm") version "1.8.0" // TODO: Move version to settings gradle file
    id("kotlin-kapt")
    id("com.palantir.graal") version "0.12.0"
}
kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation("info.picocli:picocli:4.7.4")
    implementation(project(":core"))

    kapt("info.picocli:picocli-codegen:4.7.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.24.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

graal {
    mainClass("de.hanno.executablefetcher.cli.MainKt")
    outputName("executablefetcher")
    javaVersion("11")


    // Workaround for vs distribution that isn't properly detected by plugin
    val ghActionsVsVarsFile = File("""C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\bin\amd64\vcvars64.bat""")
    if (ghActionsVsVarsFile.exists()) {
        windowsVsVarsPath(ghActionsVsVarsFile.absolutePath)
    }
}

tasks.test {
    dependsOn(tasks.nativeImage)
}
