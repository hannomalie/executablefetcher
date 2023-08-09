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

        val result = testProjectDir.executeGradle("listExecutables", "--info")

        assertThat(result.task(":listExecutables")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
        )
        assertThat(result.output).containsIgnoringWhitespaces("The following executables are registered:")
        val s = "\\" + File.separator
        assertThat(result.output).containsPattern("""helm - .*build${s}tmp${s}test${s}work${s}\.gradle-test-kit${s}executablefetcher${s}helm${s}${currentOS.identifier}${s}${currentArchitecture.identifier}${s}3\.12\.0${s}${currentOS.identifier}-${currentArchitecture.identifier}${s}helm""")
        assertThat(result.output).containsPattern("""kubectl - .*build${s}tmp${s}test${s}work${s}\.gradle-test-kit${s}executablefetcher${s}kubectl${s}${currentOS.identifier}${s}${currentArchitecture.identifier}${s}1\.27\.3${s}kubectl""")
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

        val result = testProjectDir.executeGradle("listExecutables", "--info")

        assertThat(result.task(":listExecutables")!!.outcome).isIn(
            TaskOutcome.SUCCESS,
        )
        assertThat(result.output).containsIgnoringWhitespaces("The following executables are registered:")
        val s = "\\" + File.separator
        assertThat(result.output).containsPattern("""helm - .*build${s}tmp${s}test${s}work${s}\.gradle-test-kit${s}executablefetcher${s}helm${s}${currentOS.identifier}${s}${currentArchitecture.identifier}${s}3\.12\.0${s}${currentOS.identifier}-${currentArchitecture.identifier}${s}helm""")
        assertThat(result.output).containsPattern("""helm - .*build${s}tmp${s}test${s}work${s}\.gradle-test-kit${s}executablefetcher${s}helm${s}${currentOS.identifier}${s}${currentArchitecture.identifier}${s}3\.11\.3${s}${currentOS.identifier}-${currentArchitecture.identifier}${s}helm""")
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
                tasks.named("executeHelm", de.hanno.executablefetcher.tasks.ExecuteTask::class.java) {
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
    fun `executeHelm custom task prints errors`(@TempDir testProjectDir: File) {
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
                tasks.named("executeHelm", de.hanno.executablefetcher.tasks.ExecuteTask::class.java) {
                    args = "asdasdasd"
                    version = "3.11.3"
                }
            """.trimIndent()
                )
            }
        }

        val result = testProjectDir.executeGradle("executeHelm", expectFailure = true)

        assertThat(result.task(":executeHelm")!!.outcome).isIn(
            TaskOutcome.FAILED,
        )
        assertThat(result.output).containsIgnoringWhitespaces("""> Task :executeHelm FAILED""")
        assertThat(result.output).containsIgnoringWhitespaces("""Error: unknown command "asdasdasd" for "helm"""")
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
        vararg arguments: String = arrayOf("listExecutables"),
        expectFailure: Boolean = false
    ) = GradleRunner.create()
        .withProjectDir(this)
        .withArguments(*arguments)
        .withPluginClasspath()
        .run {
            if(expectFailure) {
                buildAndFail()
            } else {
                build()
            }
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
