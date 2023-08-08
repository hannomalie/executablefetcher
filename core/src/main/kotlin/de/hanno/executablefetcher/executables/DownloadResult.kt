package de.hanno.executablefetcher.executables

import java.io.File
import java.net.URL

sealed interface DownloadResult
object AlreadyCached: DownloadResult
data class Downloaded(val file: File): DownloadResult
data class NotFound(val url: URL): DownloadResult