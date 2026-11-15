package ovh.fedox.nct.util

object FlatpakUtil {

    suspend fun kill() {
        val command = listOf("flatpak", "kill", "com.nextcloud.talk")
        CLIUtil.run(command)
    }

}