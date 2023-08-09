package de.hanno.executablefetcher.cli

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.executables.*
import de.hanno.executablefetcher.executables.builtin.BuiltIn
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import picocli.CommandLine
import java.io.File
import java.lang.IllegalArgumentException
import java.util.concurrent.Callable

@CommandLine.Command(name = "execute", mixinStandardHelpOptions = true, version = ["0.1"],
    description = [
        "Executes an executable. Fails when it is not available in the specified configuration"
    ]
)
class Execute : Callable<Int> {

    @CommandLine.Option(names = ["parentfolder"], description = ["The base dir to cache all executables."])
    var parentFolder: File = File(".")

    @CommandLine.Option(
        names = ["name"],
        description = [
            "The name of the executable to be used. Find available executables with the listExecutables command"
        ]
    )
    var name: String? = null

    @CommandLine.Option(names = ["args"], description = ["The args passed that will be passed to the executable."])
    var args: String? = null

    @CommandLine.Option(names = ["version"], description = ["The version of the executable to be used."])
    var version: String? = null

    override fun call(): Int {
        val name = requireNotNull(name) { "Provide a name of an executable!" }
        val executables = BuiltIn.executables

        val executable = executables.firstOrNull { it.name == name } ?: throw IllegalArgumentException(
            "Cannot find executable $name, available executables: ${executables.joinToString { it.name }}"
        )

        val executableConfig = ExecutableConfig(
            parentFolder = parentFolder,
            Variant(
                currentOS,
                currentArchitecture,
                version ?: executable.defaultVersion
            )
        )

        return executable.executeCareFree(executableConfig, args)
    }
}