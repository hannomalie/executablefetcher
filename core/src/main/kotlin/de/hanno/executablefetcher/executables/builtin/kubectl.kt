package de.hanno.executablefetcher.executables.builtin

import de.hanno.executablefetcher.executables.Executable
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.template.expand
import de.hanno.executablefetcher.variant.Variant

object kubectl: Executable {
    override val name = "kubectl"
    override fun getFileName(operatingSystem: OperatingSystem) = when(operatingSystem) {
        OperatingSystem.Linux -> "kubectl"
        OperatingSystem.Mac -> "kubectl"
        OperatingSystem.Windows -> "kubectl.exe"
        is OperatingSystem.Unknown -> "kubectl"
    }

    val defaultVersion = "1.27.3"

    override fun resolveDownloadUrl(
        variant: Variant
    ) = "https://dl.k8s.io/release/v{version}/bin/{os}/{arch}/${getFileName(variant.operatingSystem)}".expand(
        variant.version,
        variant.operatingSystem,
        variant.architecture
    )
}