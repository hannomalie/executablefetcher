import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class ExecutableFetcherTest {

    @Test
    fun `listExecutables gradle task prints builtin commands`(@TempDir testProjectDir: File) {
        testProjectDir.createSimpleProject()

        val result = testProjectDir.executeGradle()

        assertThat(result.task(":listExecutables")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
            TaskOutcome.UP_TO_DATE,
        )
        assertThat(result.output).containsIgnoringWhitespaces("The following executables are registered:\nhelm, kubectl")
    }

    @Test
    fun `listExecutables gradle task prints builtin commands with more info`(@TempDir testProjectDir: File) {
        testProjectDir.createSimpleProject()

        val result = testProjectDir.executeGradle("listExecutables", "--rerun-tasks", "--info")

        assertThat(result.task(":listExecutables")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
            TaskOutcome.UP_TO_DATE,
        )
        assertThat(result.output).containsIgnoringWhitespaces("The following executables are registered:")
        assertThat(result.output).containsPattern("""helm - .*build\\tmp\\test\\work\\\.gradle-test-kit\\executablefetcher\\helm\\windows\\amd64\\3\.12\.0\\windows-amd64\\helm\.exe""")
        assertThat(result.output).containsPattern("""kubectl - .*build\\tmp\\test\\work\\\.gradle-test-kit\\executablefetcher\\kubectl\\windows\\amd64\\1\.27\.3\\kubectl\.exe""")
    }

    private fun File.executeGradle(
        vararg arguments: String = arrayOf("listExecutables", "--rerun-tasks")
    ) = GradleRunner.create()
        .withProjectDir(this)
        .withArguments(*arguments)
        .withPluginClasspath()
        .build()
}

fun File.createSimpleProject() {
    assertTrue(resolve("settings.gradle.kts").createNewFile())
    resolve("build.gradle.kts").apply {
        assertTrue(createNewFile())
        writeText(
            """
                plugins {
                    id("de.hanno.executablefetcher")
                }
            """.trimIndent()
        )
    }
}
