package de.hanno.executablefetcher.executables

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.arch.toArchitecture
import de.hanno.executablefetcher.executables.builtin.helm
import de.hanno.executablefetcher.executables.builtin.kubectl
import de.hanno.executablefetcher.executables.builtin.kubectx
import de.hanno.executablefetcher.executables.builtin.kubens
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SmokeTests {
    @TestFactory
    fun `executable file is resolved and executable`(@TempDir tempDir: File): List<DynamicTest> {
        return listOf(
            Pair(helm, Variant(currentOS, currentArchitecture, helm.defaultVersion)),
            Pair(kubectl, Variant(currentOS, currentArchitecture, kubectl.defaultVersion)),
            Pair(kubectx, Variant(currentOS, currentArchitecture, kubectx.defaultVersion)),
            Pair(kubens, Variant(currentOS, currentArchitecture, kubens.defaultVersion)),
        ).map { (executable, variant) ->
            dynamicTest("for ${executable.name}") {
                assertThat(executable.downloadAndProcess(tempDir, variant)).isInstanceOf(Downloaded::class.java)
                assertThat(executable.resolveExecutableFile(tempDir, variant)).isExecutable()
            }
        }
    }

    @Test
    fun `zip gets extracted`(@TempDir tempDir: File) {
        val variant = Variant(OperatingSystem.Linux, "amd64".toArchitecture(), helm.defaultVersion)
        val downloadResult = helm.downloadAndProcess(tempDir, variant)
        assertThat(downloadResult).isInstanceOf(Downloaded::class.java)
        downloadResult as Downloaded
        assertThat(downloadResult.file.extension).isEqualTo("zip")

        val executableFile = helm.resolveExecutableFile(tempDir, variant)
        assertThat(executableFile).`as` {
            executableFile.parentFile.listFiles().joinToString { it.path }
        }.exists()
    }

    @Test
    fun `tar gz gets decompressed`(@TempDir tempDir: File) {
        val variant = Variant(OperatingSystem.Linux, "amd64".toArchitecture(), kubectx.defaultVersion)
        val downloadResult = kubectx.downloadAndProcess(tempDir, variant)
        assertThat(downloadResult).isInstanceOf(Downloaded::class.java)
        downloadResult as Downloaded
        assertThat(downloadResult.file.extension).isEqualTo("gz")

        val executableFile = kubectx.resolveExecutableFile(tempDir, variant)
        assertThat(executableFile).`as` {
            executableFile.parentFile.listFiles().joinToString { it.path }
        }.exists()
    }
}