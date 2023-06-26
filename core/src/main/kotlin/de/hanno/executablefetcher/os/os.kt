package de.hanno.executablefetcher.os

import java.util.*

// copied from
// https://mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/

private val osIdentifier = System.getProperty("os.name")
val currentOS = osIdentifier.determineOperatingSystem()

fun String.determineOperatingSystem(): OperatingSystem? = lowercase(Locale.getDefault()).let {
    when {
        it.contains("win") -> OperatingSystem.Windows
        it.contains("mac") -> OperatingSystem.Mac
        it.contains("nix") || it.contains("nux") || it.contains("aix") -> OperatingSystem.Linux
        else -> null
    }
}

sealed interface OperatingSystem {
    object Windows: OperatingSystem
    object Linux: OperatingSystem
    object Mac: OperatingSystem
}
