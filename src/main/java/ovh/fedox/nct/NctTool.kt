package ovh.fedox.nct

import com.williamcallahan.tui4j.compat.bubbletea.Program
import ovh.fedox.nct.view.MainView

fun main() {
    Entrypoint().run()
}

class Entrypoint {
    val program = Program(MainView())

    fun run() {
        program.run()
    }
}