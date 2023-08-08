package de.hanno.executablefetcher.executables

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

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