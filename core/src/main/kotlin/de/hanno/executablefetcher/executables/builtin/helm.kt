package de.hanno.executablefetcher.executables.builtin

import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.executables.Executable
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.template.expand
import de.hanno.executablefetcher.variant.Variant
import de.hanno.executablefetcher.os.identifier
import java.io.File

object helm : Executable, BuiltIn {
    override val name = "helm"
    override val defaultVersion = "3.12.0"

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
        .resolve(getFileName(variant.operatingSystem))
}
