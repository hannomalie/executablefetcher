package de.hanno.executablefetcher

import assertVersionCommandCanBeExecuted
import de.hanno.executablefetcher.core.executables.AlreadyCached
import de.hanno.executablefetcher.core.executables.Downloaded
import de.hanno.executablefetcher.core.executables.builtin.helm
import de.hanno.executablefetcher.core.variant.Variant
import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.os.OperatingSystem.Windows
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ExecutableUseCasesTest {

    private val variant = Variant(
        operatingSystem = Windows,
        architecture = "amd64",
        version = "1.2.3",
    )

    @Test
    fun `builtin helm resolves version folder properly`(@TempDir parentFolder: File) {
        val executableFolder = helm.resolveExecutableFile(parentFolder, variant).parentFile
        val relativePathToVersionFolder = executableFolder.absolutePath.replaceFirst(parentFolder.absolutePath, "")
        Assertions.assertThat(relativePathToVersionFolder).isEqualTo("""\helm\windows\amd64\1.2.3\windows-amd64""")
    }

    @Test
    fun `builtin helm can be executed`(@TempDir parentFolder: File) {
        helm.downloadAndProcess(parentFolder, variant.copy(version = "3.12.0"))
        helm.assertVersionCommandCanBeExecuted(parentFolder, "3.12.0")
    }

    @Test
    fun `builtin helm can be executed in multiple versions`(@TempDir parentFolder: File) {

        helm.downloadAndProcess(parentFolder, variant.copy(version = "3.12.0"))
        helm.downloadAndProcess(parentFolder, variant.copy(version = "3.11.3"))

        helm.assertVersionCommandCanBeExecuted(parentFolder, "3.12.0")
        helm.assertVersionCommandCanBeExecuted(parentFolder, "3.11.3")
    }

    @Nested
    class CachedExecutable {

        @Test
        fun `already downloaded executable is not downloaded again`(@TempDir parentFolder: File) {
            val variant = Variant(
                operatingSystem = Windows,
                architecture = "amd64",
                version = "3.12.0",
            )
            Assertions.assertThat(helm.downloadAndProcess(parentFolder, variant))
                .isInstanceOf(Downloaded::class.java)
            Assertions.assertThat(helm.downloadAndProcess(parentFolder, variant))
                .isInstanceOf(AlreadyCached::class.java)
        }
    }
}