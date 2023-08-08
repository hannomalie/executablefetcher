package de.hanno.executablefetcher

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.executables.Executable
import de.hanno.executablefetcher.executables.ExecutableConfig
import de.hanno.executablefetcher.executables.builtin.BuiltIn
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import org.gradle.api.invocation.Gradle
import java.io.File

open class ExecutableFetcherExtension(gradle: Gradle) {
    var parentFolder = gradle.gradleUserHomeDir.resolve("executablefetcher")

    private val _executables: MutableMap<ExecutableConfig, Executable> by lazy {
        BuiltIn.executables.associateBy {
            ExecutableConfig(parentFolder, Variant(currentOS, currentArchitecture, it.defaultVersion))
        }.toMutableMap()
    }
    val executables: Map<ExecutableConfig, Executable> by ::_executables

    fun registerExecutable(executable: Executable, version: String, parentFolder: File? = null) = _executables.put(
        ExecutableConfig(parentFolder ?: this.parentFolder, Variant(currentOS, currentArchitecture, version)), executable
    )
}