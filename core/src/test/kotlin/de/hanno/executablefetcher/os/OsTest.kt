package de.hanno.executablefetcher.os

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OsTest {
    @Test
    fun `operating systems are determined correctly from strings`() {
        assertThat("windows".toOperatingSystem()).isInstanceOf(OperatingSystem.Windows::class.java)
        assertThat("Windows".toOperatingSystem()).isInstanceOf(OperatingSystem.Windows::class.java)

        assertThat("linux".toOperatingSystem()).isInstanceOf(OperatingSystem.Linux::class.java)
        assertThat("macos".toOperatingSystem()).isInstanceOf(OperatingSystem.Mac::class.java)
    }
}