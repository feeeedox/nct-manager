package ovh.fedox.nct.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object HttpClient {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 600_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 600_000
        }
    }

    suspend fun fetchLatestVersion(): String? {
        val url = "https://api.github.com/repos/nextcloud-releases/talk-desktop/releases"

        val jsonArray: JsonArray = client.get(url).body()

        val firstRelease = jsonArray[0].jsonObject

        val tagName = firstRelease["tag_name"]?.jsonPrimitive?.content
        return tagName
    }

}