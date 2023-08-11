package de.hanno.executablefetcher

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.source
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

// Translated from https://github.com/square/okhttp/blob/master/samples/static-server/src/main/java/okhttp3/sample/SampleServer.java
class LocalServer(private val root: String, private val port: Int) :
    Dispatcher() {
    @Throws(IOException::class)
    fun run() {
        val server = MockWebServer()
        server.dispatcher = this
        server.start(port)
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path
        return try {
            if (!path!!.startsWith("/") || path.contains("..")) throw FileNotFoundException()
            val file = File(root + path)
            if (file.isDirectory) directoryToResponse(path, file) else fileToResponse(path, file)
        } catch (e: FileNotFoundException) {
            MockResponse()
                .setStatus("HTTP/1.1 404")
                .addHeader("content-type: text/plain; charset=utf-8")
                .setBody("NOT FOUND: $path")
        } catch (e: IOException) {
            MockResponse()
                .setStatus("HTTP/1.1 500")
                .addHeader("content-type: text/plain; charset=utf-8")
                .setBody("SERVER ERROR: $e")
        }
    }

    private fun directoryToResponse(basePath: String?, directory: File): MockResponse {
        var basePath = basePath
        if (!basePath!!.endsWith("/")) basePath += "/"
        val response = StringBuilder()
        response.append(String.format("<html><head><title>%s</title></head><body>", basePath))
        response.append(String.format("<h1>%s</h1>", basePath))
        for (file in directory.list()) {
            response.append(
                String.format(
                    "<div class='file'><a href='%s'>%s</a></div>",
                    basePath + file, file
                )
            )
        }
        response.append("</body></html>")
        return MockResponse()
            .setStatus("HTTP/1.1 200")
            .addHeader("content-type: text/html; charset=utf-8")
            .setBody(response.toString())
    }

    @Throws(IOException::class)
    private fun fileToResponse(path: String?, file: File): MockResponse {
        return MockResponse()
            .setStatus("HTTP/1.1 200")
            .setBody(fileToBytes(file))
            .addHeader("content-type: " + contentType(path))
    }

    @Throws(IOException::class)
    private fun fileToBytes(file: File): Buffer {
        val result = Buffer()
        result.writeAll(file.source())
        return result
    }

    private fun contentType(path: String?): String {
        if (path!!.endsWith(".png")) return "image/png"
        if (path.endsWith(".jpg")) return "image/jpeg"
        if (path.endsWith(".jpeg")) return "image/jpeg"
        if (path.endsWith(".gif")) return "image/gif"
        if (path.endsWith(".zip")) return "application/zip"
        if (path.endsWith(".tar.gz")) return "application/tar+gzip"
        if (path.endsWith(".html")) return "text/html; charset=utf-8"
        return if (path.endsWith(".txt")) "text/plain; charset=utf-8" else "application/octet-stream"
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 4) {
                println("Usage: SampleServer <keystore> <password> <root file> <port>")
                return
            }
            val root = args[2]
            val port = args[3].toInt()
            val server = LocalServer(root, port)
            server.run()
        }

        @Throws(GeneralSecurityException::class, IOException::class)
        private fun sslContext(keystoreFile: String, password: String): SSLContext {
            val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
            FileInputStream(keystoreFile).use { `in` -> keystore.load(`in`, password.toCharArray()) }
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keystore, password.toCharArray())
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keystore)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(
                keyManagerFactory.keyManagers,
                trustManagerFactory.trustManagers,
                SecureRandom()
            )
            return sslContext
        }
    }
}