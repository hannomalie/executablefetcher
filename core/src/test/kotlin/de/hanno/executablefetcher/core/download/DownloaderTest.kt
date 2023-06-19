package de.hanno.executablefetcher.core.download

import de.hanno.executablefetcher.core.template.expand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.HttpURLConnection
import java.util.zip.ZipFile
import kotlin.test.assertEquals


class DownloaderTest {
    @Test
    fun `builtin helm can be downloaded`(@TempDir tempDir: File) {
        val targetDirectory = tempDir.resolve("helm").apply { mkdirs() }
        val zipFile = download(targetDirectory, "3.12.0", "windows", "amd64")!!
        extractZipFile(zipFile, targetDirectory, true)
        val executable = targetDirectory.resolve("windows-amd64/helm.exe") // TODO: Determine os automatically here
        assertThat(executable).withFailMessage {
            "Can't find ${executable.name} in directory containing files: ${executable.parentFile.listFiles().joinToString()}"
        }.exists()

        val logFile = tempDir.resolve("log.txt").apply { createNewFile() }
        val process = ProcessBuilder().command(executable.absolutePath, "version").redirectOutput(logFile).start()
        val exitValue = process.waitFor()
        assertEquals(0, exitValue)
        assertThat(logFile.readText()).contains("""version.BuildInfo{Version:"v3.12.0"""")
    }
}
private fun extractZipFile(
    zipFile: File,
    extractTo: File,
    extractHere: Boolean = false,
): File? {
    return try {
        val outputDir = if (extractHere) {
            extractTo
        } else {
            File(extractTo, zipFile.nameWithoutExtension)
        }

        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    if (entry.isDirectory) {
                        val d = File(outputDir, entry.name)
                        if (!d.exists()) d.mkdirs()
                    } else {
                        val f = File(outputDir, entry.name)
                        if (f.parentFile?.exists() != true)  f.parentFile?.mkdirs()

                        f.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }

        extractTo
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
private fun download(targetDirectory: File, version: String, operatingSystem: String, architecture: String): File? {
    val url = expand("https://get.helm.sh/helm-v{version}-{os}-{arch}.zip", version, operatingSystem, architecture)
    val con = url.openConnection() as HttpURLConnection
    con.requestMethod = "GET"
    val responseCode = con.responseCode
    println("GET Response Code :: $responseCode")
    return if (responseCode == HttpURLConnection.HTTP_OK) {
        val targetFile = targetDirectory.resolve(url.file.removePrefix("/")).apply {
            runCatching { createNewFile() }.onFailure {
                throw IllegalStateException("Can not create file $absolutePath", it)
            }
        }
        con.inputStream.use { inputStream ->
            targetFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        targetFile
    } else {
        // TODO: Consider exception here
        null
    }
}
