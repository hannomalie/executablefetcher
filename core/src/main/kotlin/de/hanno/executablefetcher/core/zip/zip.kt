package de.hanno.executablefetcher.core.zip

import java.io.File
import java.util.zip.ZipFile

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