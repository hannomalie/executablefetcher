package de.hanno.executablefetcher

import de.hanno.executablefetcher.arch.currentArchitecture
import de.hanno.executablefetcher.executables.AlreadyCached
import de.hanno.executablefetcher.executables.Downloaded
import de.hanno.executablefetcher.executables.Executable
import de.hanno.executablefetcher.executables.NotFound
import de.hanno.executablefetcher.executables.builtin.helm
import de.hanno.executablefetcher.executables.builtin.kubectl
import de.hanno.executablefetcher.os.currentOS
import de.hanno.executablefetcher.variant.Variant
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.configurationcache.extensions.capitalized
import java.io.File


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
//    TODO: https://github.com/hannomalie/executablefetcher/issues/3
//    val process = ProcessBuilder().command(listOf(file.absolutePath, args)).inheritIO().start()
    val process = Runtime.getRuntime().exec(arrayOf(file.absolutePath, args))
    process.inputStream.use {
        println(String(it.readBytes()))
    }
    val errorString = String(process.errorStream.readBytes())
    if(errorString.isNotEmpty()) {
        project.logger.error(errorString)
    }

    val result = process.waitFor()
    project.logger.info("Executed with exit code $result")
    if(result != 0) {
        throw IllegalStateException("Execution failed for executable ${file.absolutePath} with args $args")
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