package de.hanno.executablefetcher.executables

import java.io.File
import java.io.InputStream

val defaultStandardOutHandler: (InputStream) -> Unit = { println(String(it.readBytes())) }
val defaultErrorOutHandler: (InputStream) -> Unit = {
    val errorString = String(it.readBytes())
    if(errorString.isNotEmpty()) {
        System.err.println(String(it.readBytes()))
    }
}

fun Executable.executeCareFree(config: ExecutableConfig,
            args: String?,
            standard: (InputStream) -> Unit = defaultStandardOutHandler,
            error: (InputStream) -> Unit = defaultErrorOutHandler,
): Int = when(val result = downloadAndProcess(config)) {
    AlreadyCached, is Downloaded -> {
        val executableFile = resolveExecutableFile(config)
        execute(executableFile, args, standard, error)
    }
    is NotFound -> {
        error("Download for executable $name not found on '${result.url}'!".byteInputStream())
        -1
    }
}

fun execute(
    executableFile: File,
    args: String?,
    standard: (InputStream) -> Unit = defaultStandardOutHandler,
    error: (InputStream) -> Unit = defaultErrorOutHandler,
): Int {
    val cmdArray = if(args == null) arrayOf(executableFile.absolutePath) else arrayOf(executableFile.absolutePath, args)
    return Runtime.getRuntime().exec(cmdArray).let { process ->
        process.handleOutput(standard, error)
        process.waitFor()
    }
}

private fun Process.handleOutput(
    standard: (InputStream) -> Unit = defaultStandardOutHandler,
    error: (InputStream) -> Unit = defaultErrorOutHandler,
) {
    inputStream.use { standard(it) }
    errorStream.use { error(it) }
}