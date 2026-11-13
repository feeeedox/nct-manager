package ovh.fedox.nct.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CLIUtil {

    suspend fun run(command: List<String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Result.success(output)
            } else {
                Result.failure(Exception("Command failed $exitCode: $output"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}