import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class ExecutableFetcherTest {

    @Test
    fun `listExecutables gradle task succeeds`(@TempDir testProjectDir: File) {
        testProjectDir.createSimpleProject()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("listExecutables", "--rerun-tasks")
            .withPluginClasspath()
            .build()

        assertThat(result.task(":listExecutables")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
            TaskOutcome.UP_TO_DATE,
        )
        assertThat(result.output).contains("The following executables are registered:")
    }
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
