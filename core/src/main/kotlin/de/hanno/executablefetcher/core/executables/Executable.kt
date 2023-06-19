package de.hanno.executablefetcher.core.executables

import java.io.File
import java.net.URL

interface Executable {
    val name: String
    val fileName: String

    fun createBaseFolder(executablesFolder: File): File
    fun resolveExecutableFile(parentFolder: File): File
    fun resolveDownloadUrl(): URL
    fun processDownload(downloadedFile: File, executableBaseFolder: File) {
        if(downloadedFile.extension == "zip") {
            downloadedFile.extractZipFile(executableBaseFolder)
        }
    }

    fun downloadAndProcess(parentFolder: File) {
        val baseFolder = resolveBaseFolder(parentFolder)

        val zipFile = resolveDownloadUrl().download(baseFolder)!!
        processDownload(zipFile, baseFolder)
    }
    fun resolveOsSpecificFolder(executableBaseFolder: File): File
    fun resolveBaseFolder(executablesFolder: File): File
}