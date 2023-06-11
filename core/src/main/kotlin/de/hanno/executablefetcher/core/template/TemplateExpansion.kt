package de.hanno.executablefetcher.core.template

import de.hanno.executablefetcher.core.Executable
import de.hanno.executablefetcher.core.ExecutableWithUrlPerOperatingSystem
import de.hanno.executablefetcher.core.SimpleExecutable
import java.net.URL

fun expand(
    template: String,
    operatingSystems: List<String>
): List<URL> = operatingSystems.map { operatingSystem ->
    URL(template.replace("{os}", operatingSystem))
}

fun expand(
    executable: Executable,
    operatingSystems: List<String>
): List<URL> = operatingSystems.map { operatingSystem ->
    when(executable) {
        is ExecutableWithUrlPerOperatingSystem -> {
            executable.urlsForOperatingSystems.first { it.os == operatingSystem }.url
        }
        is SimpleExecutable -> {
            URL(executable.urlTemplate.replace("{os}", operatingSystem))
        }
    }
}