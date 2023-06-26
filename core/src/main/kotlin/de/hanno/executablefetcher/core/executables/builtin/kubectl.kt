package de.hanno.executablefetcher.core.executables.builtin

import de.hanno.executablefetcher.core.executables.Executable
import de.hanno.executablefetcher.core.template.expand
import de.hanno.executablefetcher.core.variant.Variant

val kubectl = object: Executable {
    override val name = "kubectl"
    override val fileName = "kubectl.exe"

    override fun resolveDownloadUrl(
        variant: Variant
    ) = "https://dl.k8s.io/release/v{version}/bin/{os}/{arch}/$fileName".expand(
        variant.version,
        variant.operatingSystem,
        variant.architecture
    )
}