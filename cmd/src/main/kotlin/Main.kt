package de.hanno.executablefetcher.cli

import picocli.CommandLine
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) : Unit = exitProcess(
    CommandLine(
        ListExecutables()
//        Execute().apply {
//            parentFolder = File("build/main").apply { mkdirs() }
//            name = "helm"
//        }
    ).execute(*args)
)
