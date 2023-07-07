package de.hanno.executablefetcher.os

import java.util.*

// This code is translated from
// https://mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/

private val osIdentifier = System.getProperty("os.name")
val currentOS = osIdentifier.toOperatingSystem()

fun String.toOperatingSystem(): OperatingSystem = lowercase(Locale.getDefault()).let { value ->
    when {
        value.contains("win") -> OperatingSystem.Windows
        value.contains("mac") -> OperatingSystem.Mac
        value.contains("nix") || value.contains("nux") || value.contains("aix") -> OperatingSystem.Linux
        else -> OperatingSystem.Unknown(value)
    }
}

sealed interface OperatingSystem {
    object Windows: OperatingSystem
    object Linux: OperatingSystem
    object Mac: OperatingSystem
    data class Unknown(val rawIdentifier: String): OperatingSystem
}

val OperatingSystem.identifier: String get() = when(this) {
    OperatingSystem.Linux -> "linux"
    OperatingSystem.Mac -> "macos"
    OperatingSystem.Windows -> "windows"
    is OperatingSystem.Unknown -> rawIdentifier
}