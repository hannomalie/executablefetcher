package de.hanno.executablefetcher.core.template

import de.hanno.executablefetcher.core.Executable
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

    @Test
    fun `executable's template expands for all given operating systems`() {
        val executable = Executable("git", "https://www.imaginaryexdecutable.com/{os}/foo.sh", listOf("windows", "macos"))

        val finalUrls: List<URL> = expand(executable, listOf("windows", "linux", "macos"))

        assertThat(finalUrls).containsExactly(
            URL("https://www.imaginaryexdecutable.com/windows/foo.sh"),
            URL("https://www.imaginaryexdecutable.com/linux/foo.sh"),
            URL("https://www.imaginaryexdecutable.com/macos/foo.sh"),
        )
    }
}

