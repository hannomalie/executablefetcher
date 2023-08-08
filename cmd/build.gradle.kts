plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.palantir.graal")
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

    // Workaround for vs distributions that aren't properly detected by plugin
    File("""C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\bin\amd64\vcvars64.bat""").let { localVcVarsFile ->
        if (localVcVarsFile.exists()) {
            windowsVsVarsPath(localVcVarsFile.absolutePath)
        }
    }
    File("""C:\Program Files\Microsoft Visual Studio\2022\Enterprise\VC\Auxiliary\Build\vcvars64.bat""").let { localVcVarsFile ->
        if (localVcVarsFile.exists()) {
            windowsVsVarsPath(localVcVarsFile.absolutePath)
        }
    }
    option("-H:EnableURLProtocols=https")
}

tasks.test {
    dependsOn(tasks.nativeImage)
}
