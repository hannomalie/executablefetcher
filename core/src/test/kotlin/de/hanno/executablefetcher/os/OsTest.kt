package de.hanno.executablefetcher.os

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OsTest {
    @Test
    fun `operating systems are determined correctly from strings`() {
        assertThat("windows".determineOperatingSystem()).isInstanceOf(OperatingSystem.Windows::class.java)
        assertThat("Windows".determineOperatingSystem()).isInstanceOf(OperatingSystem.Windows::class.java)

        assertThat("linux".determineOperatingSystem()).isInstanceOf(OperatingSystem.Linux::class.java)
        assertThat("macos".determineOperatingSystem()).isInstanceOf(OperatingSystem.Mac::class.java)
    }
}