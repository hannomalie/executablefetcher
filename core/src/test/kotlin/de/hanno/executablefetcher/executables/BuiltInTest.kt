package de.hanno.executablefetcher.executables

import de.hanno.executablefetcher.executables.builtin.BuiltIn
import io.github.classgraph.ClassGraph
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class BuiltInTest {
    
    @Test
    fun `builtin executables list contains all known builtins`() {
        ClassGraph().enableAllInfo()
            .scan().use { scanResult ->
                val builtInClassesNames = scanResult.getClassesImplementing(BuiltIn::class.java.name)
                val builtInClasses = builtInClassesNames.map { it.loadClass().kotlin }
                val objects = builtInClasses.map { it.objectInstance!! as BuiltIn }

                assertThat(objects.size).isEqualTo(builtInClassesNames.size)

                assertThat(BuiltIn.executables).containsExactlyElementsOf(objects)
            }
    }
}