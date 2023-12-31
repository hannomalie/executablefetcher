plugins {
    id("de.hanno.executablefetcher")
}

repositories {
    mavenCentral()
}

group = "de.hanno"
version = "0.0.1-SNAPSHOT"

executableFetcher {
    registerExecutable(de.hanno.executablefetcher.executables.builtin.helm, "3.11.3")
}

tasks.named("executeHelm", de.hanno.executablefetcher.tasks.ExecuteTask::class.java) {
    args = "version"
}