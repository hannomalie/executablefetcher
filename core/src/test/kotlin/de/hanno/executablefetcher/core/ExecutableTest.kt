package de.hanno.executablefetcher.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

class ExecutableTest {
    @Test
    fun `executable can be defined for single template and operating systems`() {
        val executable = Executable("git", "www.git.com/{os}/git.sh", listOf("windows", "macos"))

        assertThat(executable.name).isEqualTo("git")
        assertThat(executable.operatingSystems).containsExactly("windows", "macos")
    }

    @Test
    fun `executable can be defined for url per operating systems`() {
        val executable = Executable(
            "svn",
            listOf(
                OperatingSystemAndUrl("windows", URL("http://www.svn.com/wind0ws/svn.sh")),
                OperatingSystemAndUrl("macos", URL("http://www.svn.com/maaaac/svn.sh")),
                OperatingSystemAndUrl("linux", URL("http://www.svn.com/linux/svn.sh")),
            )
        )

        assertThat(executable.name).isEqualTo("svn")
        assertThat(executable.operatingSystems).containsExactly("windows", "macos", "linux")
    }
}