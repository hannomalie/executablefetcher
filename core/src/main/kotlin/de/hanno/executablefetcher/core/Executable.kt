package de.hanno.executablefetcher.core

import java.net.URL

sealed interface Executable {
    val name: String
    val operatingSystems: List<String>

    companion object {
        operator fun invoke(
            name: String,
            urlTemplate: String,
            operatingSystems: List<String>
        ): Executable = SimpleExecutable(
            name, urlTemplate, operatingSystems
        )

        operator fun invoke(
            name: String,
            operatingSystemAndUrls: List<OperatingSystemAndUrl>
        ): Executable = ExecutableWithUrlPerOperatingSystem(
            name, operatingSystemAndUrls
        )
    }
}
internal class SimpleExecutable(override val name: String, val urlTemplate: String, override val operatingSystems: List<String>): Executable
internal class ExecutableWithUrlPerOperatingSystem(override val name: String, val urlsForOperatingSystems: List<OperatingSystemAndUrl>): Executable {
    override val operatingSystems: List<String> = urlsForOperatingSystems.map { it.os }
}



data class OperatingSystemAndUrl(val os: String, val url: URL)