package de.hanno.executablefetcher.executables

import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals

class DownloadStrategyURLAdjustmentTest {
    @Test
    fun `downloadStrategy adjusts URL so that it points to localhost`() {
        val originalUrl = URL("https://www.google.de/foo/bar.txt")
        val adjustedUrl = originalUrl.adjustAccordingToStrategy(downloadStrategy = DownloadStrategy.AlwaysLocalHost())
        assertEquals(URL("http://localhost:1234/foo/bar.txt"), adjustedUrl)
    }
}