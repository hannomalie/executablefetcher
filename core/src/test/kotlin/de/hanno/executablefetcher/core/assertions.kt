import de.hanno.executablefetcher.core.executables.Executable
import de.hanno.executablefetcher.core.variant.Variant
import de.hanno.executablefetcher.os.OperatingSystem
import org.assertj.core.api.Assertions
import java.io.File
import kotlin.test.assertEquals

fun Executable.assertVersionCommandCanBeExecuted(
    parentFolder: File,
    expectedVersion: String
) {
    val variant = Variant(
        operatingSystem = OperatingSystem.Windows,
        architecture = "amd64",
        version = expectedVersion,
    )
    val logFile = parentFolder.resolve("log.txt").apply { createNewFile() }
    val executableFile = resolveExecutableFile(parentFolder, variant)

    executableFile.assertExistence()
    assertEquals(0, executableFile.execute("version", logFile))
    Assertions.assertThat(logFile.readText()).contains("""version.BuildInfo{Version:"v$expectedVersion"""")
}

fun File.execute(command: String, logFile: File) =
    ProcessBuilder().command(absolutePath, command).redirectOutput(logFile).start().waitFor()

fun File.assertExistence() {
    Assertions.assertThat(this).withFailMessage {
        "Can't find $name in directory containing files: ${
            parentFile.listFiles()!!.joinToString()
        }"
    }.exists()
}