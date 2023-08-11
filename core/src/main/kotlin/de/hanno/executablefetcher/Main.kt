package de.hanno.executablefetcher

import de.hanno.executablefetcher.executables.DownloadStrategy
import de.hanno.executablefetcher.executables.builtin.javaZulu
import de.hanno.executablefetcher.executables.executeCareFree

// Meant for fast manual testing
fun main() {
    println(
        javaZulu.executeCareFree(
            downloadStrategy = DownloadStrategy.Normal,
            args = "--version",
        )
    )
}