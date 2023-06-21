package de.hanno.executablefetcher.core.template

import java.net.URL

fun String.expand(
    version: String,
    operatingSystem: String,
    architecture: String
) = URL(
    replace("{version}", version)
        .replace("{os}", operatingSystem)
        .replace("{arch}", architecture)
)
