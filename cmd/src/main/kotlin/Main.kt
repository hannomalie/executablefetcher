package de.hanno.executablefetcher.cli

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.executables.builtin.helm
import de.hanno.executablefetcher.executables.builtin.kubectl
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import picocli.CommandLine
import picocli.CommandLine.*

import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(name = "listexecutables", mixinStandardHelpOptions = true, version = ["0.1"],
    description = ["Prints all available executables."])
class Checksum : Callable<Int> {

    @Option(names = ["parentfolder"], description = ["The base dir to cache all executables."])
    var parentFolder: File = File(".")

    @Option(names = ["-i", "--info"], description = ["More details for the output"])
    var info = false

    override fun call(): Int {
        // TODO: https://github.com/hannomalie/executablefetcher/issues/6 Add other executables or make a list in core
        val executables = listOf(helm, kubectl)

        if (info) {
            executables.forEach { executable ->
                val executableFile = executable.resolveExecutableFile(
                    parentFolder,
                    Variant(currentOS, currentArchitecture, executable.defaultVersion)
                )
                println("${executable.name} - $executableFile")
            }
        } else {
            println(executables.map { it.name }.distinct().joinToString(", "))
        }

        return 0
    }
}

fun main(args: Array<String>) : Unit = exitProcess(CommandLine(Checksum()).execute(*args))
