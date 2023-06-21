package de.hanno.executablefetcher.core.executables

import de.hanno.executablefetcher.core.variant.Variant
import java.io.File
import java.net.URL

interface Executable {
    val name: String
    val fileName: String
    fun resolveDownloadUrl(variant: Variant): URL
    fun processDownload(downloadedFile: File, versionFolder: File) {
        if(downloadedFile.extension == "zip") {
            downloadedFile.extractZipFile(versionFolder)
        }
    }

    fun downloadAndProcess(parentFolder: File, variant: Variant): DownloadResult {
        val versionFolder = resolveVersionFolder(parentFolder, variant)
        val downloadedFile = download(parentFolder, versionFolder, variant)
        when(downloadedFile) {
            AlreadyCached -> {}
            is Downloaded -> processDownload(downloadedFile.file, versionFolder)
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
        Downloaded(resolveDownloadUrl(variant).download(versionFolder)!!)
    }

    fun resolveExecutableFile(
        parentFolder: File,
        variant: Variant,
    ): File = resolveVersionFolder(parentFolder, variant).resolve(fileName)

    fun resolveVersionFolder(
        parentFolder: File,
        variant: Variant
    ): File = parentFolder
        .resolve(name)
        .resolve(variant.operatingSystem)
        .resolve(variant.architecture)
        .resolve(variant.version)
}

sealed interface DownloadResult
object AlreadyCached: DownloadResult
data class Downloaded(val file: File): DownloadResult
