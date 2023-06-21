package de.hanno.executablefetcher.core.executables

import java.io.File
import java.net.URL

interface Executable {
    val name: String
    val fileName: String
    fun resolveDownloadUrl(version: String, operatingSystem: String, architecture: String): URL
    fun processDownload(downloadedFile: File, executableBaseFolder: File) {
        if(downloadedFile.extension == "zip") {
            downloadedFile.extractZipFile(executableBaseFolder)
        }
    }

    fun downloadAndProcess(parentFolder: File, operatingSystem: String, architecture: String, version: String) {
        val versionFolder = resolveVersionFolder(parentFolder, operatingSystem, architecture, version)

        val zipFile = resolveDownloadUrl(version, operatingSystem, architecture).download(versionFolder)!!
        processDownload(zipFile, versionFolder)
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
