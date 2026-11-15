package ovh.fedox.nct.module.install

import com.williamcallahan.tui4j.compat.bubbles.progress.Progress
import com.williamcallahan.tui4j.compat.bubbles.spinner.Spinner
import com.williamcallahan.tui4j.compat.bubbles.spinner.SpinnerType
import com.williamcallahan.tui4j.compat.bubbles.spinner.TickMessage
import com.williamcallahan.tui4j.compat.bubbletea.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import ovh.fedox.nct.style.*
import ovh.fedox.nct.view.MainView
import ovh.fedox.nct.view.TITLE_STYLE
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime

class FetchingLatestVersion : Message
class VersionFetched(val tag: String) : Message
class FileDownloaded(val tag: String, val path: Path) : Message
class InstallFinished(val error: String? = null) : Message

class InstallView : Model {
    private val logs = mutableListOf<String>()
    private val installService = InstallService()

    private val TICK_INTERVAL_MS = 200
    private var tickTag = 0
    private var status = "Initializing..."
    private var finished = false
    private var spinner: Spinner = Spinner(SpinnerType.POINTS).setStyle(SPINNER_STYLE)
    private var progress: Progress = Progress()
        .withShowPercentage(true)
        .withGradient("#4ab1d4", "#d0edf7")
        .withWidth(40)

    override fun init(): Command {
        return Command.batch(
            { FetchingLatestVersion() },
            Command.tick(Duration.ofMillis(TICK_INTERVAL_MS.toLong()), this::createTickMessage)
        )
    }

    private fun createTickMessage(time: LocalDateTime?): Message = TickMessage(time, tickTag, 0)

    override fun update(msg: Message): UpdateResult<out Model?> {
        when (msg) {
            is FetchingLatestVersion -> {
                status = "Fetching latest version..."

                return UpdateResult.from(
                    this,
                    Command {
                        val result = runBlocking(Dispatchers.IO) {
                            try {
                                Result.success(installService.getLatestVersion())
                            } catch (e: Exception) {
                                Result.failure(e)
                            }
                        }

                        if (result.isSuccess) {
                            VersionFetched(result.getOrThrow())
                        } else {
                            InstallFinished(error = result.exceptionOrNull()?.localizedMessage)
                        }
                    }
                )
            }

            is VersionFetched -> {
                val tag = msg.tag
                logs.add("Found version: $tag")

                status = "Download NextCloud $tag..."

                return UpdateResult.from(
                    this,
                    Command {
                        val (errorMsg, installedPath) = runBlocking(Dispatchers.IO) {
                            try {
                                val path = installService.downloadVersion(tag, "talk.flatpak", onProgress = {
                                    status = "Download NextCloud Talk $tag...\n\n${progress.viewAs(it)}"
                                })
                                Pair(null, path)
                            } catch (e: Exception) {
                                if (e is CancellationException) {
                                    ensureActive()
                                }
                                Pair(e.localizedMessage, null)
                            }
                        }

                        if (installedPath != null) {
                            FileDownloaded(tag, path = installedPath)
                        } else {
                            InstallFinished(error = errorMsg)
                        }
                    }
                )
            }

            is FileDownloaded -> {
                logs.add("Downloaded flatpak file at ${msg.path.toAbsolutePath()}")
                status = "Installing NextCloud Talk ${msg.tag}"
                return UpdateResult.from(
                    this,
                    Command {
                        val (success, value) = runBlocking(Dispatchers.IO) {
                            try {
                                val pair = installService.installFlatpak(msg.path)
                                Pair(pair.first, pair.second)
                            } catch (e: Exception) {
                                if (e is CancellationException) {
                                    ensureActive()
                                }
                                Pair(null, null)
                            }
                        }

                        if (success == true) {
                            InstallFinished(error = null)
                        } else {
                            val errorMessage = value.toString().lineSequence()
                                .map { it.trim() }
                                .firstOrNull { it.startsWith("error:", ignoreCase = true) }
                                ?: value.toString().lines()
                                    .lastOrNull { it.isNotBlank() }
                                ?: "Unknown error"

                            InstallFinished(error = errorMessage)
                        }
                    }
                )
            }

            is InstallFinished -> {
                finished = true
                status = if (msg.error == null) {
                    "${CHECKMARK_STYLE.render()} Installation completed successfully! ${CREDITS.render()}"
                } else {
                    "${X_STYLE.render()} Installation failed: ${msg.error} ${CREDITS.render()}"
                }
                return UpdateResult.from(this, Command.quit())
            }

            is TickMessage -> {
                if (finished) return UpdateResult.from(this)
                tickTag++
                spinner.update(msg)
                return UpdateResult.from(
                    this,
                    Command.tick(Duration.ofMillis(TICK_INTERVAL_MS.toLong()), this::createTickMessage)
                )
            }

            is KeyPressMessage -> {
                if (msg.key() == "esc" && finished) {
                    val mainView = MainView()
                    return UpdateResult.from(mainView, mainView.init())
                }
                if (msg.key() == "ctrl+c") {
                    return UpdateResult.from(this, Command.quit())
                }
            }
        }
        return UpdateResult.from(this)
    }

    override fun view(): String {
        return CONTAINER_STYLE.render(
            buildString {
                append(TITLE_STYLE.render("Installer"))
                appendLine()
                logs.forEach { appendLine(it) }
                appendLine()
                if (!finished) {
                    append(spinner.view())
                    append(" ")
                }
                append(status)
            }
        )
    }
}