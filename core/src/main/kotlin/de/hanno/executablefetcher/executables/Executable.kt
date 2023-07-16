package de.hanno.executablefetcher.executables

import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.download.download
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.os.identifier
import de.hanno.executablefetcher.variant.Variant
import de.hanno.executablefetcher.zip.extractZipFile
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption


interface Executable {
    val name: String
    fun getFileName(operatingSystem: OperatingSystem): String = when(operatingSystem) {
        OperatingSystem.Linux -> name
        OperatingSystem.Mac -> name
        OperatingSystem.Windows -> "$name.exe"
        is OperatingSystem.Unknown -> name
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

// code translated from https://mkyong.com/java/how-to-create-tar-gz-in-java/
@Throws(IOException::class)
fun decompressTarGzipFile(source: Path, target: Path) {
    if (Files.notExists(source)) { throw IOException("File doesn't exists!") }

    Files.newInputStream(source).use { fi ->
        BufferedInputStream(fi).use { bi ->
            GzipCompressorInputStream(bi).use { gzi ->
                TarArchiveInputStream(gzi).use { ti ->
                    var entry: ArchiveEntry?
                    while (ti.nextEntry.also { entry = it } != null) {

                        // create a new path, zip slip validate
                        val newPath = zipSlipProtect(entry!!, target)
                        if (entry!!.isDirectory) {
                            Files.createDirectories(newPath)
                        } else {

                            // check parent folder again
                            val parent = newPath.parent
                            if (parent != null) {
                                if (Files.notExists(parent)) {
                                    Files.createDirectories(parent)
                                }
                            }

                            // copy TarArchiveInputStream to Path newPath
                            Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            }
        }
    }
}

@Throws(IOException::class)
private fun zipSlipProtect(entry: ArchiveEntry, targetDir: Path): Path {
    val targetDirResolved = targetDir.resolve(entry.name)

    // make sure normalized file still has targetDir as its prefix,
    // else throws exception
    val normalizePath = targetDirResolved.normalize()
    if (!normalizePath.startsWith(targetDir)) {
        throw IOException("Bad entry: " + entry.name)
    }
    return normalizePath
}
