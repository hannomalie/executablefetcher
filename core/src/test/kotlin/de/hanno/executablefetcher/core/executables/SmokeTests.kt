package de.hanno.executablefetcher.core.executables

import de.hanno.executablefetcher.core.executables.builtin.helm
import de.hanno.executablefetcher.core.executables.builtin.kubectl
import de.hanno.executablefetcher.core.variant.Variant
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
            Pair(::helm, Variant("windows", "amd64", "3.12.0")),
            Pair(::kubectl, Variant("windows", "amd64", "1.27.3"))
        ).map { (executableProperty, variant) ->
            dynamicTest("for ${executableProperty.name}") {
                val executable = executableProperty.get()
                assertThat(executable.downloadAndProcess(tempDir, variant)).isInstanceOf(Downloaded::class.java)
                assertThat(executable.resolveExecutableFile(tempDir, variant)).isExecutable()
            }
        }
    }
}