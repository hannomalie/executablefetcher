allprojects {
    group = "de.hanno.executablefetcher"

    repositories {
        mavenCentral()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
