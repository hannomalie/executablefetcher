package de.hanno.executablefetcher.core.executables

import java.io.File
import java.net.URL

interface Executable {
    val name: String
    val fileName: String
    fun resolveDownloadUrl(version: String, operatingSystem: String, architecture: String): URL
    fun processDownload(downloadedFile: File, versionFolder: File) {
        if(downloadedFile.extension == "zip") {
            downloadedFile.extractZipFile(versionFolder)
        }
    }

    fun downloadAndProcess(parentFolder: File, operatingSystem: String, architecture: String, version: String): DownloadResult {
        val versionFolder = resolveVersionFolder(parentFolder, operatingSystem, architecture, version)
        val downloadedFile = download(parentFolder, versionFolder, operatingSystem, architecture, version)
        when(downloadedFile) {
            AlreadyCached -> {}
            is Downloaded -> processDownload(downloadedFile.file, versionFolder)
        }
        return downloadedFile
    }

    fun download(
        parentFolder: File,
        versionFolder: File,
        operatingSystem: String,
        architecture: String,
        version: String
    ): DownloadResult = if(resolveExecutableFile(parentFolder, operatingSystem, architecture, version).exists()) {
        AlreadyCached
    } else {
        Downloaded(resolveDownloadUrl(version, operatingSystem, architecture).download(versionFolder)!!)
    }

    fun resolveExecutableFile(
        parentFolder: File,
        operatingSystem: String,
        architecture: String,
        version: String
    ): File = resolveVersionFolder(parentFolder, operatingSystem, architecture, version).resolve(fileName)

    fun resolveVersionFolder(
        parentFolder: File,
        operatingSystem: String,
        architecture: String,
        version: String
    ): File = parentFolder.resolve(name).resolve(operatingSystem).resolve(architecture).resolve(version)
}

sealed interface DownloadResult
object AlreadyCached: DownloadResult
data class Downloaded(val file: File): DownloadResult
