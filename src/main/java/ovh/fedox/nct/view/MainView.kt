package ovh.fedox.nct.view

import com.williamcallahan.tui4j.compat.bubbles.list.FilteredItem
import com.williamcallahan.tui4j.compat.bubbles.list.List
import com.williamcallahan.tui4j.compat.bubbles.list.Styles
import com.williamcallahan.tui4j.compat.bubbletea.*
import com.williamcallahan.tui4j.compat.lipgloss.Style
import ovh.fedox.nct.module.install.InstallView
import ovh.fedox.nct.module.uninstall.UninstallView
import ovh.fedox.nct.module.update.UpdateView
import ovh.fedox.nct.style.CONTAINER_STYLE
import ovh.fedox.nct.style.Lipgloss

val TITLE_STYLE: Style = Style.newStyle().bold(true).italic(true).marginRight(2).marginTop(2)
val ITEM_STYLE: Style = Style.newStyle().paddingLeft(2)
val SELECTED_ITEM_STYLE: Style = Style.newStyle().paddingLeft(2).foreground(Lipgloss.PRIMARY)
val PAGINATION_STYLE: Style =
    Styles.defaultStyles().paginationStyle().paddingLeft(4)
val HELP_STYLE: Style = Styles.defaultStyles().helpStyle().paddingLeft(4).paddingBottom(1)

class MainView : Model {

    class Item(val value: String) : com.williamcallahan.tui4j.compat.bubbles.list.Item {
        override fun filterValue(): String {
            return ""
        }
    }

    class ItemDelegate : com.williamcallahan.tui4j.compat.bubbles.list.ItemDelegate {
        override fun render(output: StringBuilder, list: List, index: Int, filteredItem: FilteredItem) {
            val item = filteredItem.item()
            if (item is Item) {
                val customItem = item
                val str = "${index + 1}. ${item.value}"
                if (index == list.index()) {
                    output.append(SELECTED_ITEM_STYLE.render("»", str))
                } else {
                    output.append(ITEM_STYLE.render(str))
                }
            }
        }

        override fun height(): Int {
            return 1
        }

        override fun spacing(): Int {
            return 0
        }

        override fun update(msg: Message?, listModel: List?): Command? {
            return null
        }
    }

    var list: List = List(
        arrayOf(
            Item("Install"),
            Item("Update"),
            Item("Uninstall"),
            Item("Quit")
        ),
        ItemDelegate(),
        20,
        14
    )

    override fun init(): Command? {
        val listInitCommand = list.init()

        list.setFilteringEnabled(false)
        list.setTitle("Choose an option:")
        list.styles().setTitle(TITLE_STYLE)
        list.styles().setPaginationStyle(PAGINATION_STYLE)
        list.styles().setHelpStyle(HELP_STYLE)

        return Command.sequence(listInitCommand, {
            list.setShowStatusBar(false)
            null
        })
    }

    override fun update(msg: Message): UpdateResult<out Model?>? {
        if (msg is WindowSizeMessage) {
            return UpdateResult.from(this, list.setSize(msg.width(), msg.height()))
        }

        if (msg is KeyPressMessage) {
            if (msg.key().equals("q") || msg.key().equals("ctrl+c")) {
                return UpdateResult.from(this, Command.quit())
            } else if (msg.key() == "enter") {
                val item = list.selectedItem() as? Item
                    ?: return UpdateResult.from(this)

                return when (item.value) {
                    "Install" -> {
                        val installView = InstallView()
                        UpdateResult.from(installView, installView.init())
                    }

                    "Update" -> {
                        val updateView = UpdateView()
                        UpdateResult.from(updateView, updateView.init())
                    }

                    "Uninstall" -> {
                        val uninstallView = UninstallView()
                        UpdateResult.from(uninstallView, uninstallView.init())
                    }

                    "Quit" -> UpdateResult.from(this, Command.quit())

                    else -> UpdateResult.from(this)
                }
            }
        }

        val listUpdateResult = list.update(msg)
        return UpdateResult.from(this, listUpdateResult.command)
    }

    override fun view(): String {
        return "${CONTAINER_STYLE.render("\n${list.view()}")}"
    }

}