package de.hanno.executablefetcher.core

import java.net.URL

fun expand(
    template: String,
    operatingSystems: List<String>
): List<URL> = operatingSystems.map { operatingSystem ->
    URL(template.replace("{os}", operatingSystem))
}