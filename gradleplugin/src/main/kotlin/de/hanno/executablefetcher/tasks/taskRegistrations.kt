package de.hanno.executablefetcher.tasks

import de.hanno.executablefetcher.executables.printExecutables
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.configurationcache.extensions.capitalized


internal fun Project.registerGlobalExecuteTask() {
    tasks.register("execute", GlobalExecuteTask::class.java) { task ->
        task.group = "executable"
        task.description = "Executes a command. Supply --executable and optionally --version and --args."
    }
}

internal fun Project.registerExecutableTasks(extension: ExecutableFetcherExtension) {
    val executableConfigsGroupedByExecutableName = extension.executables.entries.groupBy { it.value.name }

    executableConfigsGroupedByExecutableName.values.forEach {
        it.firstOrNull()?.let { (config, executable) ->
            tasks.register("execute${executable.name.capitalized()}", ExecuteTask::class.java) { task ->
                task.group = "executable"
                task.executableName = executable.name
                task.version = config.variant.version
                task.description = "Executes ${executable.name} in version ${config.variant.version}. Args can be overridden."
            }
        }
    }
}

internal fun Project.registerListExecutableTask(
    extension: ExecutableFetcherExtension
) {
    tasks.register("listExecutables") { task ->
        group = "executable"
        task.outputs.upToDateWhen { false }

        task.doLast {
            System.out.printExecutables(
                verbose = gradle.startParameter.logLevel in listOf(LogLevel.INFO, LogLevel.DEBUG),
                extension.executables
            )
        }
    }
}