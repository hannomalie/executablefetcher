package de.hanno.executablefetcher

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.executables.DownloadStrategy
import de.hanno.executablefetcher.executables.ExecutableConfig
import de.hanno.executablefetcher.executables.builtin.javaZulu
import de.hanno.executablefetcher.executables.executeCareFree
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import java.io.File

// Meant for fast manual testing
fun main() {
    println(
        javaZulu.executeCareFree(
            ExecutableConfig(
                parentFolder = File("."),
                variant = Variant(currentOS, currentArchitecture, javaZulu.defaultVersion),
                downloadStrategy = DownloadStrategy.Normal
            ),
            args = "--version",
        )
    )
}