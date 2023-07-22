package de.hanno.executablefetcher.executables

import io.github.classgraph.ClassGraph
import org.junit.jupiter.api.Test


class BuiltInTest {
    
    @Test
    fun `builtin executables list contains all known builtins`() {
        ClassGraph().enableAllInfo().acceptPackages("com.xyz")
            .scan().use { scanResult ->
                val widgetClasses = scanResult.getClassesImplementing("com.xyz.Widget")
                val widgetClassNames = widgetClasses.names
            }
    }
}