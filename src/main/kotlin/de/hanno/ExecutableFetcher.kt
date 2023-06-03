package de.hanno

import org.gradle.api.Plugin
import org.gradle.api.Project

class ExecutableFetcher: Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("listExecutables") {
            println("The following executables are registered:")
        }
    }
}