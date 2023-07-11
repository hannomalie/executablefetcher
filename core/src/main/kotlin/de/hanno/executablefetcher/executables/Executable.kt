package de.hanno.executablefetcher.executables

import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.download.download
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.variant.Variant
import de.hanno.executablefetcher.zip.extractZipFile
import de.hanno.executablefetcher.os.identifier
import java.io.File
import java.net.URL

interface Executable {
    val name: String
    fun getFileName(operatingSystem: OperatingSystem): String
    fun resolveDownloadUrl(variant: Variant): URL
    fun processDownload(downloadedFile: File, versionFolder: File, parentFolder: File, variant: Variant) {
        if(downloadedFile.extension == "zip") {
            downloadedFile.extractZipFile(versionFolder)
        }
        val executableFile = resolveExecutableFile(parentFolder, variant)
        executableFile.setExecutable(true)
    }

    fun downloadAndProcess(parentFolder: File, variant: Variant): DownloadResult {
        val versionFolder = resolveVersionFolder(parentFolder, variant)
        val downloadedFile = download(parentFolder, versionFolder, variant)
        when(downloadedFile) {
            AlreadyCached -> {}
            is Downloaded -> processDownload(downloadedFile.file, versionFolder, parentFolder, variant)
            is NotFound -> {}
        }
        return downloadedFile
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

sealed interface DownloadResult
object AlreadyCached: DownloadResult
data class Downloaded(val file: File): DownloadResult
data class NotFound(val url: URL): DownloadResult
