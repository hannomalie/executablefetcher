package de.hanno.executablefetcher

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.arch.toArchitecture
import de.hanno.executablefetcher.executables.AlreadyCached
import de.hanno.executablefetcher.executables.Downloaded
import de.hanno.executablefetcher.executables.builtin.helm
import de.hanno.executablefetcher.variant.Variant
import de.hanno.executablefetcher.os.OperatingSystem.Windows
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.os.identifier
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ExecutableUseCasesTest {

    private val variant = Variant(
        operatingSystem = Windows,
        architecture = "amd64".toArchitecture(),
        version = "1.2.3",
    )

    @Test
    fun `builtin helm resolves version folder properly`(@TempDir parentFolder: File) {
        val executableFolder = helm.resolveExecutableFile(parentFolder, variant).parentFile
        val relativePathToVersionFolder = executableFolder.absolutePath.replaceFirst(parentFolder.absolutePath, "")
        Assertions.assertThat(relativePathToVersionFolder)
            .isEqualTo("""\helm\${currentOS.identifier}\${currentArchitecture.identifier}\1.2.3\${currentOS.identifier}-${currentArchitecture.identifier}""")
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
                architecture = "amd64".toArchitecture(),
                version = "3.12.0",
            )
            Assertions.assertThat(helm.downloadAndProcess(parentFolder, variant))
                .isInstanceOf(Downloaded::class.java)

            Assertions.assertThat(helm.downloadAndProcess(parentFolder, variant))
                .isInstanceOf(AlreadyCached::class.java)
        }
    }
}