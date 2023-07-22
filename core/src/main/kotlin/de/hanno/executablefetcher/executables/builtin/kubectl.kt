package de.hanno.executablefetcher.executables.builtin

import de.hanno.executablefetcher.template.expand
import de.hanno.executablefetcher.variant.Variant

object kubectl: BuiltIn {
    override val name = "kubectl"
    override val defaultVersion = "1.27.3"

    override fun resolveDownloadUrl(
        variant: Variant
    ) = "https://dl.k8s.io/release/v{version}/bin/{os}/{arch}/${getFileName(variant.operatingSystem)}".expand(
        variant.version,
        variant.operatingSystem,
        variant.architecture
    )
}