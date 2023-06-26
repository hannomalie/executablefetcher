package de.hanno.executablefetcher.core.template

import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.os.identifier
import java.net.URL

fun String.expand(
    version: String,
    operatingSystem: OperatingSystem,
    architecture: String
) = URL(
    replace("{version}", version)
        .replace("{os}", operatingSystem.identifier)
        .replace("{arch}", architecture)
)
