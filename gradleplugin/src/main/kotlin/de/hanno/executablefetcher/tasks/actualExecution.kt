package de.hanno.executablefetcher.tasks

import de.hanno.executablefetcher.ExecutableFetcherExtension
import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.executables.*
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import org.gradle.api.Project
import java.io.File
import java.io.InputStream

fun actualExecute(
    project: Project,
    executableName: String,
    version: String?,
    args: String,
    parentFolder: File?,
) {
    val (fallbackExecutableConfig, executable) = project.deriveFallbacks(executableName)
    val executableConfig = deriveExecutableConfig(fallbackExecutableConfig, parentFolder, version)

    executable.downloadAndProcess(executableConfig).let { result ->
        when (result) {
            AlreadyCached -> project.logger.info("Executable $executableName is already cached")
            is Downloaded -> project.logger.info("Downloaded executable $executableName to ${result.file.absolutePath}")
            is NotFound -> throw IllegalStateException("Cannot download executable $executableName from ${result.url}")
        }
    }

    val executableFile = executable.resolveExecutableFile(executableConfig)

    project.logger.info("Executing '${executableFile.absolutePath} ${args}'")
//    TODO: https://github.com/hannomalie/executablefetcher/issues/3
//    val process = ProcessBuilder().command(listOf(file.absolutePath, args)).inheritIO().start()
    val result = execute(executableFile, args) {errorStream ->
        val errorString = String(errorStream.readBytes())
        if(errorString.isNotEmpty()) {
            project.logger.error(errorString)
        }
    }
    project.handleResult(result, executableFile, args)
}


private fun Project.handleResult(result: Int, file: File, args: String) {
    logger.info("Executed with exit code $result")
    if (result != 0) {
        throw IllegalStateException("Execution failed for executable ${file.absolutePath} with args $args")
    }
}


private fun Project.deriveFallbacks(
    executableName: String
): Map.Entry<ExecutableConfig, Executable> {
    val executables = extensions.getByType(ExecutableFetcherExtension::class.java).executables
    return (executables.entries.firstOrNull { it.value.name == executableName }
        ?: throw IllegalStateException(
            "Can't find requested executable '$executableName'. Available: ${
                executables.map { it.value.name }.distinct().joinToString(", ")
            }"
        ))
}

private fun deriveExecutableConfig(
    templateExecutableConfig: ExecutableConfig,
    parentFolder: File?,
    version: String?
): ExecutableConfig {
    val defaultVersion = templateExecutableConfig.variant.version
    val defaultParentFolder = templateExecutableConfig.parentFolder

    return ExecutableConfig(
        parentFolder = parentFolder ?: defaultParentFolder,
        variant = Variant(
            operatingSystem = currentOS,
            architecture = currentArchitecture,
            version = version ?: defaultVersion
        )
    )
}
