package de.hanno.executablefetcher.core.executables.builtin

import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.core.executables.Executable
import de.hanno.executablefetcher.core.template.expand
import de.hanno.executablefetcher.core.variant.Variant
import de.hanno.executablefetcher.os.identifier
import java.io.File

object helm : Executable {
    override val name = "helm"
    override val fileName = "helm.exe"

    val defaultVersion = "3.12.0"

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
        .resolve("${variant.operatingSystem.identifier}-${variant.architecture.identifier}")
        .resolve(fileName)
}
