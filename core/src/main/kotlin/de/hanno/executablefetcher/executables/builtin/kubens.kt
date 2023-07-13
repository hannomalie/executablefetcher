package de.hanno.executablefetcher.executables.builtin

import de.hanno.executablefetcher.executables.Executable
import de.hanno.executablefetcher.executables.builtin.kubectx.customExtension
import de.hanno.executablefetcher.executables.builtin.kubectx.customIdentifier
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.variant.Variant
import java.net.URL

object kubens: Executable {
    override val name = "kubens"
    override fun getFileName(operatingSystem: OperatingSystem) = when(operatingSystem) {
        OperatingSystem.Linux -> "kubens"
        OperatingSystem.Mac -> "kubens"
        OperatingSystem.Windows -> "kubens.exe"
        is OperatingSystem.Unknown -> "kubens"
    }

    val defaultVersion = kubectx.defaultVersion

    override fun resolveDownloadUrl(
        variant: Variant
    ): URL = variant.run {
        URL("https://github.com/ahmetb/kubectx/releases/download/v${version}/kubens_v${version}_${operatingSystem.customIdentifier}_${architecture.customIdentifier}.${operatingSystem.customExtension}")
    }
}