package de.hanno.executablefetcher.cli

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.executables.ExecutableConfig
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import de.hanno.executablefetcher.executables.builtin.BuiltIn
import de.hanno.executablefetcher.executables.printExecutables
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(name = "listexecutables", mixinStandardHelpOptions = true, version = ["0.1"],
    description = ["Prints all available executables."])
class ListExecutables : Callable<Int> {

    @Option(names = ["parentfolder"], description = ["The base dir to cache all executables."])
    var parentFolder: File = File(".")

    @Option(names = ["-i", "--info"], description = ["More details for the output"])
    var info = false

    override fun call(): Int {
        val executables = BuiltIn.executables

        System.out.printExecutables(
            verbose = info,
            executables = executables.associateBy { executable ->
                ExecutableConfig(
                    parentFolder,
                    Variant(currentOS, currentArchitecture, executable.defaultVersion)
                )
            }
        )

        return 0
    }
}

fun main(args: Array<String>) : Unit = exitProcess(CommandLine(ListExecutables()).execute(*args))
