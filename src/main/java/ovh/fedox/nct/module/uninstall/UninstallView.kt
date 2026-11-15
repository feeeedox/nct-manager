package ovh.fedox.nct.module.uninstall

import com.williamcallahan.tui4j.compat.bubbles.spinner.Spinner
import com.williamcallahan.tui4j.compat.bubbles.spinner.SpinnerType
import com.williamcallahan.tui4j.compat.bubbles.spinner.TickMessage
import com.williamcallahan.tui4j.compat.bubbletea.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ovh.fedox.nct.style.*
import ovh.fedox.nct.view.MainView
import ovh.fedox.nct.view.TITLE_STYLE
import java.time.Duration
import java.time.LocalDateTime

class StartRemoval : Message
class Removed : Message
class RemovedError(val error: String) : Message

class UninstallView : Model {
    private val logs = mutableListOf<String>()
    private val removeService = UninstallService()

    private val TICK_INTERVAL_MS = 200
    private var tickTag = 0
    private var status = "Initializing..."
    private var finished = false
    private var spinner: Spinner = Spinner(SpinnerType.POINTS).setStyle(SPINNER_STYLE)

    override fun init(): Command {
        return Command.batch(
            { StartRemoval() },
            Command.tick(Duration.ofMillis(TICK_INTERVAL_MS.toLong()), this::createTickMessage)
        )
    }

    private fun createTickMessage(time: LocalDateTime?): Message = TickMessage(time, tickTag, 0)

    override fun update(msg: Message): UpdateResult<out Model?> {
        when (msg) {
            is StartRemoval -> {
                status = "Removing NextCloud Talk..."

                return UpdateResult.from(
                    this,
                    Command {
                        val (success, result) = runBlocking(Dispatchers.IO) {
                            try {
                                val result = removeService.removeFlatpak()
                                Pair(result.first, result.second)
                            } catch (e: Exception) {
                                Pair(null, e)
                            }
                        }

                        if (success == true) {
                            Removed()
                        } else {
                            RemovedError(result.toString())
                        }
                    }
                )
            }

            is Removed -> {
                finished = true
                status = "${CHECKMARK_STYLE.render()} Successfully removed NextCloud Talk! ${CREDITS.render()}"
                return UpdateResult.from(this, Command.quit())
            }

            is RemovedError -> {
                "${X_STYLE.render()} Installation failed: ${msg.error} ${CREDITS.render()}"

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
                append(TITLE_STYLE.render("Uninstall"))
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