package de.hanno.executablefetcher.core.download

import de.hanno.executablefetcher.core.executables.BuiltIns
import de.hanno.executablefetcher.core.executables.Executable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals


class DownloaderTest {

    @Test
    fun `builtin helm can be downloaded`(@TempDir parentFolder: File) {
        val helmExecutable = BuiltIns.helm

        helmExecutable.downloadAndProcess(parentFolder)

        helmExecutable.assertVersionCommandCanBeExecuted(parentFolder)
    }

    private fun Executable.assertVersionCommandCanBeExecuted(
        parentFolder: File
    ) {
        val logFile = parentFolder.resolve("log.txt").apply { createNewFile() }
        val executable = resolveExecutableFile(parentFolder)

        executable.assertExistence()
        assertEquals(0, executable.execute("version", logFile))
        assertThat(logFile.readText()).contains("""version.BuildInfo{Version:"v3.12.0"""")
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

