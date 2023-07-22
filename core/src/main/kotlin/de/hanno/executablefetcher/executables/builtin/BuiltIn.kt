package de.hanno.executablefetcher.executables.builtin

interface BuiltIn {
    val defaultVersion: String

    companion object {
        val executables = listOf(
            helm,
        )
    }
}