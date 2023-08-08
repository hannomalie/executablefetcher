package de.hanno.executablefetcher.executables

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import java.io.PrintStream

fun PrintStream.printExecutables(
    verbose: Boolean,
    executables: Map<ExecutableConfig, Executable>
) {
    println("The following executables are registered:")

    val infoString = if (verbose) {
        executables.entries.joinToString("\n") { (executableConfig, executable) ->
            val executableFile = executable.resolveExecutableFile(
                executableConfig.parentFolder,
                Variant(currentOS, currentArchitecture, executableConfig.variant.version)
            )
            "${executable.name} - $executableFile"
        }
    } else {
        executables.values.map { it.name }.distinct().joinToString(", ")
    }

    println(infoString)
}