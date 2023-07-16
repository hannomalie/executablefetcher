import de.hanno.executablefetcher.os.OperatingSystem
import de.hanno.executablefetcher.os.currentOS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CmdTest {
    @Test
    fun `compiled executable runs`() {
        val pathFromProjectRootToExecutable = if(currentOS is OperatingSystem.Windows) {
            "build/graal/executablefetcher.exe"
        } else {
            "build/graal/executablefetcher"
        }
        val file = File(pathFromProjectRootToExecutable)
        assertThat(file).exists()
        assertThat(file).isExecutable()

        val process = Runtime.getRuntime().exec(pathFromProjectRootToExecutable)

        assertThat(process.waitFor())
            .`as` { process.inputStream.use { String(it.readBytes()) } + process.errorStream.use { String(it.readBytes()) } }
            .isEqualTo(0)
    }
}