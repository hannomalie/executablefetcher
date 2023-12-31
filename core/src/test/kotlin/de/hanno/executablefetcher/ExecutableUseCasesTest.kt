package de.hanno.executablefetcher

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.arch.toArchitecture
import de.hanno.executablefetcher.executables.AlreadyCached
import de.hanno.executablefetcher.executables.DownloadStrategy
import de.hanno.executablefetcher.executables.Downloaded
import de.hanno.executablefetcher.executables.builtin.helm
import de.hanno.executablefetcher.os.OperatingSystem.Windows
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.os.identifier
import de.hanno.executablefetcher.variant.Variant
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIf
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertIs


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
        val s = File.separator
        Assertions.assertThat(relativePathToVersionFolder)
            .isEqualTo("""${s}helm${s}${Windows.identifier}${s}${currentArchitecture.identifier}${s}1.2.3${s}${Windows.identifier}-${currentArchitecture.identifier}""")
    }

    @Test
    @EnabledOnOs(value = [OS.WINDOWS])
    fun `builtin helm can be executed`(@TempDir parentFolder: File) {
        helm.downloadAndProcess(parentFolder, variant.copy(version = "3.12.0"), DownloadStrategy.AlwaysLocalHost(localServer.port))
        helm.assertVersionCommandCanBeExecuted(parentFolder, "3.12.0")
    }

    @Test
    @EnabledOnOs(value = [OS.WINDOWS])
    fun `builtin helm can be executed in multiple versions`(@TempDir parentFolder: File) {

        assertIs<Downloaded>(helm.downloadAndProcess(parentFolder, variant.copy(version = "3.12.0"), DownloadStrategy.AlwaysLocalHost(localServer.port)))
        assertIs<Downloaded>(helm.downloadAndProcess(parentFolder, variant.copy(version = "3.11.3"), DownloadStrategy.AlwaysLocalHost(localServer.port)))

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
            Assertions.assertThat(helm.downloadAndProcess(parentFolder, variant, DownloadStrategy.AlwaysLocalHost(
                localServer.port
            )))
                .isInstanceOf(Downloaded::class.java)

            Assertions.assertThat(helm.downloadAndProcess(parentFolder, variant, DownloadStrategy.AlwaysLocalHost(
                localServer.port
            )))
                .isInstanceOf(AlreadyCached::class.java)
        }
        companion object {
            private lateinit var localServer: LocalServer

            @JvmStatic
            @BeforeAll
            fun beforeAll() {
                localServer = LocalServer("src/test/resources/served_locally")
                localServer.run()
            }
            @JvmStatic
            @AfterAll
            fun afterAll() {
                localServer.shutdown()
            }
        }
    }

    companion object {
        private lateinit var localServer: LocalServer

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            localServer = LocalServer("src/test/resources/served_locally")
            localServer.run()
        }
        @JvmStatic
        @AfterAll
        fun afterAll() {
            localServer.shutdown()
        }
    }
}