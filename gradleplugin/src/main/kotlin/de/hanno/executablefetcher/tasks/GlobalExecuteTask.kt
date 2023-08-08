package de.hanno.executablefetcher.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

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