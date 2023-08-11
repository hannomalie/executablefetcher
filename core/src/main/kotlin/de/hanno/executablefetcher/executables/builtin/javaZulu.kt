package de.hanno.executablefetcher.executables.builtin

import de.hanno.executablefetcher.arch.Architecture
import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.os.OperatingSystem.*
import de.hanno.executablefetcher.variant.Variant
import java.io.File
import java.net.URL

object javaZulu: BuiltIn {
    override val name = "java-zulu"
    override val defaultVersion = "20.32.11-ca-jdk20.0.2"

    override fun resolveExecutableFile(parentFolder: File, variant: Variant): File {
        return resolveVersionFolder(parentFolder, variant)
            .resolve("zulu${variant.version}-${variant.operatingSystem.customidentifier}_${variant.architecture.customIdentifier}")
            .resolve("bin/")
            .resolve(getFileName(variant.operatingSystem))
    }

    override fun getFileName(
        operatingSystem: OperatingSystem
    ): String = when(operatingSystem) {
        Linux -> "java"
        Mac -> "java"
        Windows -> "java.exe"
        is Unknown -> "java"
    }

    override fun resolveDownloadUrl(
        variant: Variant
    ): URL = variant.run {
        val extension = if(variant.operatingSystem is Windows) "zip" else "tar.gz"
        URL(
            "https://cdn.azul.com/zulu/bin/zulu${version}-${operatingSystem.customidentifier}_${architecture.customIdentifier}.$extension"
        )
    }
}

private val OperatingSystem.customidentifier: String get() = when(this) {
    Linux -> "linux"
    Mac -> "macos"
    Windows -> "win"
    is Unknown -> rawIdentifier
}

private val Architecture.customIdentifier: String get() = when(this) {
    Architecture.x86_64 -> "x64"
    else -> identifier
}

// https://cdn.azul.com/zulu/bin/zulu20.32.11-ca-jdk20.0.2-win_x64.zip