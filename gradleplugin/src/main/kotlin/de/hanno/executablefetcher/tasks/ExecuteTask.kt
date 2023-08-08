package de.hanno.executablefetcher.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

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