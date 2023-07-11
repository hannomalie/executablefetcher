package de.hanno.executablefetcher.executables

import de.hanno.executablefetcher.arch.toArchitecture
import de.hanno.executablefetcher.executables.builtin.helm
import de.hanno.executablefetcher.executables.builtin.kubectl
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SmokeTests {
    @TestFactory
    fun `executable file is resolved and executable`(@TempDir tempDir: File): List<DynamicTest> {
        return listOf(
            Pair(helm, Variant(currentOS, "amd64".toArchitecture(), "3.12.0")),
            Pair(kubectl, Variant(currentOS, "amd64".toArchitecture(), "1.27.3"))
        ).map { (executable, variant) ->
            dynamicTest("for ${executable.name}") {
                assertThat(executable.downloadAndProcess(tempDir, variant)).isInstanceOf(Downloaded::class.java)
                assertThat(executable.resolveExecutableFile(tempDir, variant)).isExecutable()
            }
        }
    }
}