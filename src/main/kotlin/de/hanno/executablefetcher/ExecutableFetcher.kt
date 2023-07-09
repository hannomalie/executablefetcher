package de.hanno.executablefetcher

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.core.executables.AlreadyCached
import de.hanno.executablefetcher.core.executables.Downloaded
import de.hanno.executablefetcher.core.executables.Executable
import de.hanno.executablefetcher.core.executables.NotFound
import de.hanno.executablefetcher.core.executables.builtin.helm
import de.hanno.executablefetcher.core.executables.builtin.kubectl
import de.hanno.executablefetcher.core.variant.Variant
import de.hanno.executablefetcher.os.currentOS
import org.gradle.api.*
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.configurationcache.extensions.capitalized
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture


class ExecutableFetcher: Plugin<Project> {
    override fun apply(target: Project) {
        val extension = ExecutableFetcherExtension(target.gradle)
        target.extensions.add("executableFetcher", extension)

        target.registerListExecutableTask(extension)
        target.registerGlobalExecuteTask()
        target.registerExecutableTasks(extension)
    }

    private fun Project.registerGlobalExecuteTask() {
        tasks.register("execute", GlobalExecuteTask::class.java) { task ->
            task.group = "executable"
            task.description = "Executes a command. Supply --executable and optionally --version and --args."
        }
    }

    private fun Project.registerExecutableTasks(extension: ExecutableFetcherExtension) {
        val executableConfigsGroupedByExecutableName = extension.executables.entries.groupBy { it.key.name }

        executableConfigsGroupedByExecutableName.values.forEach {
            it.firstOrNull()?.let { (key, _) ->
                tasks.register("execute${key.name.capitalized()}", ExecuteTask::class.java) { task ->
                    task.group = "executable"
                    task.executableName = key.name
                    task.version = key.version
                    task.description = "Executes ${key.name} in version ${key.version}. Args can be overridden."
                }
            }
        }
    }

    private fun Project.registerListExecutableTask(
        extension: ExecutableFetcherExtension
    ) {
        tasks.register("listExecutables") { task ->
            group = "executable"
            task.outputs.upToDateWhen { false }

            task.doLast {
                println("The following executables are registered:")
                if (gradle.startParameter.logLevel in listOf(LogLevel.INFO, LogLevel.DEBUG)) {
                    extension.executables.forEach { (executableConfig, executable) ->
                        val executableFile = executable.resolveExecutableFile(
                            executableConfig.parentFolder,
                            Variant(currentOS, currentArchitecture, executableConfig.version)
                        )
                        println("${executableConfig.name} - $executableFile")
                    }
                } else {
                    println(extension.executables.keys.map { it.name }.distinct().joinToString(", "))
                }
            }
        }
    }
}

open class GlobalExecuteTask: DefaultTask() {
    @Input
    @Option(
        option = "executable",
        description = "The executable that should be used, like 'helm' or 'git'"
    )
    lateinit var executableName: String

    @Input
    @Optional
    @Option(
        option = "version",
        description = "The version in which the executable should be used"
    )
    var version: String? = null

    @Optional
    @InputDirectory
    var parentFolder: File? = null

    @Input
    @Optional
    @Option(
        option = "args",
        description = "The arguments that are passed into the executable call, for example 'version' for 'helm version'"
    )
    var args: String = ""

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute() {
        actualExecute(project, executableName, version, args, parentFolder)
    }
}

open class ExecuteTask: DefaultTask() {
    @Input
    lateinit var executableName: String

    @Input
    @Optional
    var version: String? = null

    @Optional
    @InputDirectory
    var parentFolder: File? = null

    @Input
    @Optional
    var args: String = ""

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute() {
        actualExecute(project, executableName, version, args, parentFolder)
    }
}

fun actualExecute(
    project: Project,
    executableName: String,
    version: String?,
    args: String,
    parentFolder: File?,
) {

    val extension = project.extensions.getByType(ExecutableFetcherExtension::class.java)
    val executables = extension.executables

    val executableConfig = executables.keys.firstOrNull { it.name == executableName }
        ?: throw IllegalStateException(
            "Can't find requested executable '$executableName'. Available: ${executables.map { it.key.name }.distinct().joinToString(", ")}"
        )
    val defaultVersion = executableConfig.version
    val defaultParentFolder = executableConfig.parentFolder

    val resultingParentFolder = parentFolder ?: defaultParentFolder
    val resultingVersion = version ?: defaultVersion
    val variant = Variant(currentOS, currentArchitecture, resultingVersion)

    val executable = executables[ExecutableConfig(executableName, resultingVersion, resultingParentFolder)]!!
    executable.downloadAndProcess(resultingParentFolder, variant).let { result ->
        when(result) {
            AlreadyCached -> project.logger.info("Executable $executableName is already cached")
            is Downloaded -> project.logger.info("Downloaded executable $executableName to ${result.file.absolutePath}")
            is NotFound -> throw IllegalStateException("Cannot download executable $executableName from ${result.url}")
        }
    }

    val file = executable.resolveExecutableFile(resultingParentFolder, variant)

    project.logger.info("Executing '${file.absolutePath} ${args}'")
    val process = ProcessBuilder().command(file.absolutePath, args).inheritIO().start()

    val stdOutFuture = process.inputStream.readAsync()
    val stdErrorFuture = process.errorStream.readAsync()
    stdOutFuture.thenCombine(stdErrorFuture) { stdOut: String, stdErr: String ->
        project.logger.error(stdErr)
        println(stdOut)
    }

    val result = process.waitFor()
    if(result != 0) {
        throw IllegalStateException("Execution failed for executable ${file.absolutePath} with args $args")
    }
}

// Code translated from
// https://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
fun InputStream.readAsync(): CompletableFuture<String> = CompletableFuture.supplyAsync {
    try {
        InputStreamReader(this).use { isr ->
            BufferedReader(isr).use { br ->
                val res = StringBuilder()
                var inputLine: String?
                while (br.readLine().also { inputLine = it } != null) {
                    res.append(inputLine).append(System.lineSeparator())
                }
                return@supplyAsync res.toString()
            }
        }
    } catch (e: Throwable) {
        throw RuntimeException("problem with executing program", e)
    }
}

data class ExecutableConfig(val name: String, val version: String, val parentFolder: File)
open class ExecutableFetcherExtension(private val gradle: Gradle) {
    var parentFolder = gradle.gradleUserHomeDir.resolve("executablefetcher")

    private val _executables: MutableMap<ExecutableConfig, Executable> by lazy {
        mutableMapOf(
            ExecutableConfig(helm.name, helm.defaultVersion, parentFolder) to helm,
            ExecutableConfig(kubectl.name, kubectl.defaultVersion, parentFolder) to kubectl,
        )
    }
    val executables: Map<ExecutableConfig, Executable> by ::_executables

    fun registerExecutable(executable: Executable, version: String, parentFolder: File? = null) = _executables.put(
        ExecutableConfig(executable.name, version, parentFolder ?: this.parentFolder), executable
    )
}