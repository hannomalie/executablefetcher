package de.hanno.executablefetcher.executables.builtin

import de.hanno.executablefetcher.arch.Architecture
import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.executables.Executable
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.os.identifier
import de.hanno.executablefetcher.template.expand
import de.hanno.executablefetcher.variant.Variant
import java.net.URL

object kubectx: Executable, BuiltIn {
    override val name = "kubectx"
    override val defaultVersion = "0.9.4"

    override fun resolveDownloadUrl(
        variant: Variant
    ): URL = variant.run {
        URL("https://github.com/ahmetb/kubectx/releases/download/v${version}/kubectx_v${version}_${operatingSystem.customIdentifier}_${architecture.customIdentifier}.${operatingSystem.customExtension}")
    }

    val OperatingSystem.customIdentifier: String get() = if(this is OperatingSystem.Mac) "darwin" else identifier
    val Architecture.customIdentifier: String get() = when(this) {
        Architecture.arm_64 -> "arm64"
        Architecture.x86_64 -> "x86_64"
        else -> identifier
    }

    val OperatingSystem.customExtension: String get() = when(this) {
        OperatingSystem.Linux -> "tar.gz"
        OperatingSystem.Mac -> "tar.gz"
        OperatingSystem.Windows -> "zip"
        is OperatingSystem.Unknown -> "tar.gz"
    }
}