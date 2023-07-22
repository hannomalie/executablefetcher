package de.hanno.executablefetcher.executables.builtin

import de.hanno.executablefetcher.executables.builtin.kubectx.customExtension
import de.hanno.executablefetcher.executables.builtin.kubectx.customIdentifier
import de.hanno.executablefetcher.variant.Variant
import java.net.URL

object kubens: BuiltIn {
    override val name = "kubens"
    override val defaultVersion = kubectx.defaultVersion

    override fun resolveDownloadUrl(
        variant: Variant
    ): URL = variant.run {
        URL("https://github.com/ahmetb/kubectx/releases/download/v${version}/kubens_v${version}_${operatingSystem.customIdentifier}_${architecture.customIdentifier}.${operatingSystem.customExtension}")
    }
}