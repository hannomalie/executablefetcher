package de.hanno.executablefetcher.template

import de.hanno.executablefetcher.arch.toArchitecture
import de.hanno.executablefetcher.os.OperatingSystem.Windows
import de.hanno.executablefetcher.template.expand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

class TemplateExpansionTest {

    private val version = "3.12.0"
    private val architecture = "amd64".toArchitecture()

    private val template = "https://get.helm.sh/helm-v{version}-{os}-{arch}.zip"

    @Test
    fun `template expands for all given operating systems`() {
        val finalUrl: URL = template.expand(
            version = version,
            operatingSystem = Windows,
            architecture = architecture,
        )

        assertThat(finalUrl).isEqualTo(
            URL("https://get.helm.sh/helm-v3.12.0-windows-amd64.zip"),
        )
    }
}

