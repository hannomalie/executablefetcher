package de.hanno.executablefetcher.executables

import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.download.download
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.os.OperatingSystem.*
import de.hanno.executablefetcher.os.identifier
import de.hanno.executablefetcher.variant.Variant
import de.hanno.executablefetcher.zip.extractZipFile
import java.io.File
import java.net.URL

interface Executable {
    val name: String

    fun getFileName(
        operatingSystem: OperatingSystem
    ): String = when(operatingSystem) {
        Linux -> name
        Mac -> name
        Windows -> "$name.exe"
        is Unknown -> name
    }

    fun resolveDownloadUrl(variant: Variant): URL
    fun processDownload(downloadedFile: File, versionFolder: File, parentFolder: File, variant: Variant) {
        if(downloadedFile.extension == "zip") {
            downloadedFile.extractZipFile(versionFolder)
        } else if(downloadedFile.name.endsWith(".tar.gz")) {
            decompressTarGzipFile(downloadedFile.toPath(), versionFolder.toPath())
        }
        val executableFile = resolveExecutableFile(parentFolder, variant)
        executableFile.setExecutable(true)
    }

    fun downloadAndProcess(parentFolder: File, variant: Variant): DownloadResult {
        val versionFolder = resolveVersionFolder(parentFolder, variant)
        return download(
            parentFolder,
            versionFolder,
            variant
        ).apply {
            when (this) {
                AlreadyCached -> {}
                is Downloaded -> processDownload(file, versionFolder, parentFolder, variant)
                is NotFound -> {}
            }
        }
    }

    fun download(
        parentFolder: File,
        versionFolder: File,
        variant: Variant,
    ): DownloadResult = if(resolveExecutableFile(parentFolder, variant).exists()) {
        AlreadyCached
    } else {
        val url = resolveDownloadUrl(variant)
        val file = url.download(versionFolder)
        if(file == null) {
            NotFound(url)
        } else {
            Downloaded(file)
        }
    }

    fun resolveExecutableFile(
        parentFolder: File,
        variant: Variant,
    ): File = resolveVersionFolder(parentFolder, variant).resolve(getFileName(variant.operatingSystem))

    fun resolveVersionFolder(
        parentFolder: File,
        variant: Variant
    ): File = parentFolder
        .resolve(name)
        .resolve(variant.operatingSystem.identifier)
        .resolve(variant.architecture.identifier)
        .resolve(variant.version)
}

fun Executable.downloadAndProcess(config: ExecutableConfig) = downloadAndProcess(config.parentFolder, config.variant)
fun Executable.resolveExecutableFile(config: ExecutableConfig) = resolveExecutableFile(config.parentFolder, config.variant)

data class ExecutableConfig(val parentFolder: File, val variant: Variant)
