package ovh.fedox.nct.module.install

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ovh.fedox.nct.http.HttpClient
import ovh.fedox.nct.util.CLIUtil
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import kotlin.io.path.deleteIfExists

class InstallService {

    private val downloadsDir: Path = Path.of(
        System.getProperty("user.home"),
        "Downloads"
    )

    suspend fun getLatestVersion(): String {
        return HttpClient.fetchLatestVersion() ?: throw IllegalStateException("N/A")
    }

    suspend fun installFlatpak(path: Path): Pair<Boolean, Any?> {
        val command = listOf("flatpak", "install", "--user", "-y", "$path")
        val result = CLIUtil.run(command)

        path.deleteIfExists()

        return Pair(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
    }

    suspend fun downloadVersion(tag: String, filename: String, onProgress: (Double) -> Unit = {}): Path =
        withContext(Dispatchers.IO) {
            val downloadUrl =
                "https://github.com/nextcloud-releases/talk-desktop/releases/download/${tag}/Nextcloud.Talk-linux-x64.flatpak"

            Files.createDirectories(downloadsDir)

            val destination = downloadsDir.resolve(filename)
            val temporary = downloadsDir.resolve("$filename.part")

            val request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .timeout(Duration.ofMinutes(10))
                .GET().build()

            try {
                val response = HttpClient.client.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
                )
                
                if (response.statusCode() !in 200..299) {
                    response.body().close()
                    error("Download failed: ${response.statusCode()} ${response.body()}")
                }

                val total = response.headers()
                    .firstValueAsLong("Content-Length")
                    .orElse(-1L)

                var downloaded = 0L
                val buffer = ByteArray(64 * 1024) // 64 KB buffer

                response.body().use { input ->
                    Files.newOutputStream(temporary).use { output ->
                        while (true) {
                            val read = input.read(buffer)

                            if (read == -1) break

                            output.write(buffer, 0, read)
                            downloaded += read

                            if (total > 0) {
                                onProgress(
                                    (downloaded.toDouble() / total).coerceIn(0.0, 1.0)
                                )
                            }
                        }
                    }
                }

                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING)

                onProgress(1.0)
                destination
            } catch (e: Exception) {
                Files.deleteIfExists(temporary)
                throw e
            }
        }
}