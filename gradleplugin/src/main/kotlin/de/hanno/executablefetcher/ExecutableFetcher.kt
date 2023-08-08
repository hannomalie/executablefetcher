package de.hanno.executablefetcher

import de.hanno.executablefetcher.tasks.registerExecutableTasks
import de.hanno.executablefetcher.tasks.registerGlobalExecuteTask
import de.hanno.executablefetcher.tasks.registerListExecutableTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ExecutableFetcher: Plugin<Project> {
    override fun apply(target: Project) {
        val extension = ExecutableFetcherExtension(target.gradle)
        target.extensions.add("executableFetcher", extension)

        target.registerListExecutableTask(extension)
        target.registerGlobalExecuteTask()
        target.registerExecutableTasks(extension)
    }
}
