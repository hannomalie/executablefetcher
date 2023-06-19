package de.hanno.executablefetcher.core.template

import java.net.URL

fun expand(
    template: String,
    version: String,
    operatingSystem: String,
    architecture: String
) = URL(
    template
        .replace("{version}", version)
        .replace("{os}", operatingSystem)
        .replace("{arch}", architecture)
)
