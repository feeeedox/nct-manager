package ovh.fedox.nct.http

import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object HttpClient {

    val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    suspend fun fetchLatestVersion(): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/nextcloud-releases/talk-desktop/releases/latest"))
            .timeout(Duration.ofSeconds(60))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "nct-cli")
            .GET()
            .build()

        val response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        check(response.statusCode() in 200..299) {
            "GitHub API failed: ${response.statusCode()} ${response.body()}"
        }
        
        return JSONObject(response.body())
            .optString("tag_name")
            .takeIf { it.isNotBlank() }
    }

}