package de.hanno.executablefetcher.variant

import de.hanno.executablefetcher.arch.Architecture
import de.hanno.executablefetcher.os.OperatingSystem

data class Variant(
    val operatingSystem: OperatingSystem,
    val architecture: Architecture,
    val version: String,
)