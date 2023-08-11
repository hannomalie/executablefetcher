package de.hanno.executablefetcher.executables.builtin

interface BuiltIn: de.hanno.executablefetcher.executables.Executable {
    val defaultVersion: String

    companion object {
        val executables: List<BuiltIn> by lazy {
            listOf(
                helm,
                kubectl,
                kubectx,
                kubens,
                javaZulu,
            )
        }
    }
}
