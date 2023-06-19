package de.hanno.executablefetcher.core.template

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

class TemplateExpansionTest {

    val version = "3.12.0"
    val architecture = "amd64"

    val template = "https://get.helm.sh/helm-v{version}-{os}-{arch}.zip"

    @Test
    fun `template expands for all given operating systems`() {
        val finalUrl: URL = expand(
            template = template,
            version = version,
            operatingSystem = "windows",
            architecture = architecture,
        )

        assertThat(finalUrl).isEqualTo(
            URL("https://get.helm.sh/helm-v3.12.0-windows-amd64.zip"),
        )
    }
}

