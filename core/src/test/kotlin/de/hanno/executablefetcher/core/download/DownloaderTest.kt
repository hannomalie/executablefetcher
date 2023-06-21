package de.hanno.executablefetcher.core.download

import de.hanno.executablefetcher.core.executables.AlreadyCached
import de.hanno.executablefetcher.core.executables.BuiltIns
import de.hanno.executablefetcher.core.executables.Downloaded
import de.hanno.executablefetcher.core.executables.Executable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals


class DownloaderTest {

    private val helmExecutable = BuiltIns.helm

    @Test
    fun `builtin helm resolves version folder properly`(@TempDir parentFolder: File) {
        val executableFolder = helmExecutable.resolveExecutableFile(parentFolder, "windows", "amd64", "1.2.3").parentFile
        val relativePathToVersionFolder = executableFolder.absolutePath.replaceFirst(parentFolder.absolutePath, "")
        assertThat(relativePathToVersionFolder).isEqualTo("""\helm\windows\amd64\1.2.3\windows-amd64""")
    }

    @Test
    fun `builtin helm can be executed`(@TempDir parentFolder: File) {

        helmExecutable.downloadAndProcess(parentFolder, "windows", "amd64", "3.12.0")
        helmExecutable.assertVersionCommandCanBeExecuted(parentFolder, "3.12.0")
    }

    @Test
    fun `builtin helm can be executed in multiple versions`(@TempDir parentFolder: File) {

        helmExecutable.downloadAndProcess(parentFolder, "windows", "amd64", "3.12.0")
        helmExecutable.downloadAndProcess(parentFolder, "windows", "amd64", "3.11.3")

        helmExecutable.assertVersionCommandCanBeExecuted(parentFolder, "3.12.0")
        helmExecutable.assertVersionCommandCanBeExecuted(parentFolder, "3.11.3")
    }

    @Nested
    class CachedExecutable {

        private val helmExecutable = BuiltIns.helm

        @Test
        fun `already downloaded executable is not downloaded again`(@TempDir parentFolder: File) {
            assertThat(helmExecutable.downloadAndProcess(parentFolder, "windows", "amd64", "3.12.0"))
                .isInstanceOf(Downloaded::class.java)
            assertThat(helmExecutable.downloadAndProcess(parentFolder, "windows", "amd64", "3.12.0"))
                .isInstanceOf(AlreadyCached::class.java)
        }
    }

    private fun Executable.assertVersionCommandCanBeExecuted(
        parentFolder: File,
        expectedVersion: String
    ) {
        val logFile = parentFolder.resolve("log.txt").apply { createNewFile() }
        val executableFile = resolveExecutableFile(parentFolder, "windows", "amd64", expectedVersion)

        executableFile.assertExistence()
        assertEquals(0, executableFile.execute("version", logFile))
        assertThat(logFile.readText()).contains("""version.BuildInfo{Version:"v$expectedVersion"""")
    }

    private fun File.execute(command: String, logFile: File) =
        ProcessBuilder().command(absolutePath, command).redirectOutput(logFile).start().waitFor()

    private fun File.assertExistence() {
        assertThat(this).withFailMessage {
            "Can't find $name in directory containing files: ${
                parentFile.listFiles()!!.joinToString()
            }"
        }.exists()
    }
}

