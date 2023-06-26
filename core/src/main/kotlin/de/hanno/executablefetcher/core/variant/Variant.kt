package de.hanno.executablefetcher.core.variant

import de.hanno.executablefetcher.os.OperatingSystem

data class Variant(
    val operatingSystem: OperatingSystem,
    val architecture: String,
    val version: String,
)