package de.hanno.executablefetcher.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

class TemplateExpansionTest {

    @Test
    fun `template expands for all given operating systems`() {
        val template = "https://imaginaryexdecutable/os/{os}/foo.sh"

        val finalUrls: List<URL> = expand(template, listOf("windows", "linux", "macos"))

        assertThat(finalUrls).containsExactly(
            URL("https://imaginaryexdecutable/os/windows/foo.sh"),
            URL("https://imaginaryexdecutable/os/linux/foo.sh"),
            URL("https://imaginaryexdecutable/os/macos/foo.sh"),
        )
    }
}

