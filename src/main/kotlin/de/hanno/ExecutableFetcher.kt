package de.hanno

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.core.executables.Executable
import de.hanno.executablefetcher.core.executables.builtin.helm
import de.hanno.executablefetcher.core.executables.builtin.kubectl
import de.hanno.executablefetcher.core.variant.Variant
import de.hanno.executablefetcher.os.currentOS
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.LogLevel
import java.io.File

class ExecutableFetcher: Plugin<Project> {
    override fun apply(target: Project) {
        val extension = ExecutableFetcherExtension(target.gradle)
        target.extensions.add("executableFetcher", extension)

        target.tasks.register("listExecutables") { task ->
            task.doLast {
                println("The following executables are registered:")
                if(target.gradle.startParameter.logLevel in listOf(LogLevel.INFO, LogLevel.DEBUG)) {
                    extension.executables.forEach { (executableConfig, executable) ->
                        val executableFile = executable.resolveExecutableFile(
                            executableConfig.parentFolder,
                            Variant(currentOS, currentArchitecture, executableConfig.version)
                        )
                        println("${executableConfig.name} - $executableFile")
                    }
                } else {
                    println(extension.executables.keys.joinToString(", ") { it -> it.name })
                }
            }
        }
    }
}

data class ExecutableConfig(val name: String, val version: String, val parentFolder: File)
open class ExecutableFetcherExtension(private val gradle: Gradle) {
    var parentFolder = gradle.gradleUserHomeDir.resolve("executablefetcher")

    private val _executables: MutableMap<ExecutableConfig, Executable> by lazy {
        mutableMapOf(
            ExecutableConfig(helm.name, helm.defaultVersion, parentFolder) to helm,
            ExecutableConfig(kubectl.name, kubectl.defaultVersion, parentFolder) to kubectl,
        )
    }
    val executables: Map<ExecutableConfig, Executable> by ::_executables

    fun registerExecutable(executable: Executable, version: String, parentFolder: File?) = _executables.put(
        ExecutableConfig(executable.name, version, parentFolder ?: this.parentFolder), executable
    )
}