package de.hanno.executablefetcher.core.executables

import de.hanno.executablefetcher.core.template.expand
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipFile

object BuiltIns {
    val helm = object: Executable {
        override val name = "helm"
        override val fileName = "helm.exe"

        override fun resolveDownloadUrl(version: String, operatingSystem: String, architecture: String) = expand(
            "https://get.helm.sh/helm-v{version}-{os}-{arch}.zip",
            version,
            operatingSystem,
            architecture
        )

        override fun resolveExecutableFile(
            parentFolder: File,
            operatingSystem: String,
            architecture: String,
            version: String
        ): File = resolveVersionFolder(parentFolder, operatingSystem, architecture, version)
            .resolve("$operatingSystem-$architecture").resolve(fileName)
    }
}

fun File.extractZipFile(
    extractTo: File,
): File? = try {
    ZipFile(this).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                if (entry.isDirectory) {
                    val d = File(extractTo, entry.name)
                    if (!d.exists()) d.mkdirs()
                } else {
                    val f = File(extractTo, entry.name)
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

fun URL.download(targetDirectory: File): File? {
    if(!targetDirectory.exists()) {
        targetDirectory.mkdirs()
    }
    val con = openConnection() as HttpURLConnection
    con.requestMethod = "GET"

    return when (con.responseCode) {
        HttpURLConnection.HTTP_OK -> {
            val targetFile = targetDirectory.resolve(file.removePrefix("/")).apply {
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
        }
        else -> {
            // TODO: Consider exception here
            null
        }
    }
}