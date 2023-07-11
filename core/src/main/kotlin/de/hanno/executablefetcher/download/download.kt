package de.hanno.executablefetcher.download

import java.io.File
import java.net.HttpURLConnection
import java.net.URL

fun URL.download(targetDirectory: File): File? {
    if(!targetDirectory.exists()) {
        targetDirectory.mkdirs()
    }
    val con = openConnection() as HttpURLConnection
    con.requestMethod = "GET"

    return when (con.responseCode) {
        HttpURLConnection.HTTP_OK -> {
            val targetFile = targetDirectory.resolve(fileNameWithExtension).apply {
                runCatching {
                    createNewFile()
                }.onFailure {
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
        else -> null
    }
}

private val URL.fileNameWithExtension get() = file.split('/').last()