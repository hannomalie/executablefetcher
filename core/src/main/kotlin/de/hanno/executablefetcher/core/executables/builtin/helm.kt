package de.hanno.executablefetcher.core.executables.builtin

import de.hanno.executablefetcher.core.executables.Executable
import de.hanno.executablefetcher.core.template.expand
import de.hanno.executablefetcher.core.variant.Variant
import java.io.File

val helm = object: Executable {
    override val name = "helm"
    override val fileName = "helm.exe"

    override fun resolveDownloadUrl(
        variant: Variant
    ) = "https://get.helm.sh/helm-v{version}-{os}-{arch}.zip".expand(
        variant.version,
        variant.operatingSystem,
        variant.architecture
    )

    override fun resolveExecutableFile(
        parentFolder: File,
        variant: Variant,
    ): File = resolveVersionFolder(parentFolder, variant)
        .resolve("${variant.operatingSystem}-${variant.architecture}")
        .resolve(fileName)
}