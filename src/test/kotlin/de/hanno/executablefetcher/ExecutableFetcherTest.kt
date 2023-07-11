package de.hanno.executablefetcher

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.arch.identifier
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.os.identifier
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
        )
        assertThat(result.output).containsIgnoringWhitespaces("The following executables are registered:\nhelm, kubectl")
    }

    @Test
    fun `listExecutables gradle task prints builtin commands with more info`(@TempDir testProjectDir: File) {
        testProjectDir.createSimpleProject()

        val result = testProjectDir.executeGradle("listExecutables", "--rerun-tasks", "--info")

        assertThat(result.task(":listExecutables")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
        )
        assertThat(result.output).containsIgnoringWhitespaces("The following executables are registered:")
        assertThat(result.output).containsPattern("""helm - .*build\\tmp\\test\\work\\\.gradle-test-kit\\executablefetcher\\helm\\${currentOS.identifier}\\${currentArchitecture.identifier}\\3\.12\.0\\${currentOS.identifier}-${currentArchitecture.identifier}\\helm""")
        assertThat(result.output).containsPattern("""kubectl - .*build\\tmp\\test\\work\\\.gradle-test-kit\\executablefetcher\\kubectl\\${currentOS.identifier}\\${currentArchitecture.identifier}\\1\.27\.3\\kubectl""")
    }

    @Test
    fun `listExecutables gradle task prints custom variants of builtin commands`(@TempDir testProjectDir: File) {
        assertTrue(testProjectDir.resolve("settings.gradle.kts").createNewFile())
        testProjectDir.resolve("build.gradle.kts").apply {
            assertTrue(createNewFile())
            writeText(
                """
                    plugins {
                        id("de.hanno.executablefetcher")
                    }
                    extensions.getByType(de.hanno.executablefetcher.ExecutableFetcherExtension::class.java).apply {
                        registerExecutable(de.hanno.executablefetcher.executables.builtin.helm, "3.11.3")    
                    }
                """.trimIndent()
            )
        }

        val result = testProjectDir.executeGradle("listExecutables", "--rerun-tasks", "--info")

        assertThat(result.task(":listExecutables")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
        )
        assertThat(result.output).containsIgnoringWhitespaces("The following executables are registered:")
        assertThat(result.output).containsPattern("""helm - .*build\\tmp\\test\\work\\\.gradle-test-kit\\executablefetcher\\helm\\${currentOS.identifier}\\${currentArchitecture.identifier}\\3\.12\.0\\${currentOS.identifier}-${currentArchitecture.identifier}\\helm""")
        assertThat(result.output).containsPattern("""helm - .*build\\tmp\\test\\work\\\.gradle-test-kit\\executablefetcher\\helm\\${currentOS.identifier}\\${currentArchitecture.identifier}\\3\.11\.3\\${currentOS.identifier}-${currentArchitecture.identifier}\\helm""")
    }

    @Test
    fun `executeHelm custom task prints helm version`(@TempDir testProjectDir: File) {
        testProjectDir.apply {
            assertTrue(resolve("settings.gradle.kts").createNewFile())
            resolve("build.gradle.kts").apply {
                assertTrue(createNewFile())
                writeText(
                    """
                plugins {
                    id("de.hanno.executablefetcher")
                }
                executableFetcher {
                    registerExecutable(de.hanno.executablefetcher.executables.builtin.helm, "3.11.3")
                }
                tasks.named("executeHelm", de.hanno.executablefetcher.ExecuteTask::class.java) {
                    args = "version"
                    version = "3.11.3"
                }
            """.trimIndent()
                )
            }
        }

        val result = testProjectDir.executeGradle("executeHelm")

        assertThat(result.task(":executeHelm")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
        )
        assertThat(result.output).containsIgnoringWhitespaces("""version.BuildInfo{Version:"v3.11.3"""")
    }

    @Test
    fun `global executable task prints helm version when used with helm parameter`(@TempDir testProjectDir: File) {
        testProjectDir.apply {
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

        val result = testProjectDir.executeGradle("execute", "--executable=helm", "--args=version")

        assertThat(result.task(":execute")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
        )
        assertThat(result.output).containsIgnoringWhitespaces("""version.BuildInfo{Version:"v3.12.0"""")
    }

    private fun File.executeGradle(
        vararg arguments: String = arrayOf("listExecutables")
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
