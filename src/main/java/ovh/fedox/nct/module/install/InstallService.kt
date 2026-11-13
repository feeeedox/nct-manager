package ovh.fedox.nct.module.install

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ovh.fedox.nct.http.HttpClient
import ovh.fedox.nct.util.CLIUtil
import java.nio.file.Files
import java.nio.file.Path
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

            withContext(Dispatchers.IO) {
                Files.createDirectories(downloadsDir)
            }

            val destination = downloadsDir.resolve(filename)

            HttpClient.client.prepareGet(downloadUrl).execute { response ->
                check(response.status.isSuccess()) {
                    "Download failed: ${response.status.description}"
                }

                val total = response.contentLength() ?: -1L
                var downloaded = 0L

                val channel = response.bodyAsChannel()
                val buffer = ByteArray(8192)

                withContext(Dispatchers.IO) {
                    Files.newOutputStream(destination)
                }.use { output ->
                    while (true) {
                        val read = channel.readAvailable(buffer)
                        if (read == -1) break
                        if (read == 0) break

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

            onProgress(1.0)
            destination
        }
}