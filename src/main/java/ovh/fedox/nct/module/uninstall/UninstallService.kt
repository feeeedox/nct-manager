package ovh.fedox.nct.module.uninstall

import ovh.fedox.nct.util.CLIUtil

class UninstallService {

    suspend fun removeFlatpak(): Pair<Boolean, Any?> {
        val command = listOf("flatpak", "uninstall", "--user", "-y", "com.nextcloud.talk")
        val result = CLIUtil.run(command)
        return Pair(result.isSuccess, result.exceptionOrNull()?.localizedMessage)
    }
    
}