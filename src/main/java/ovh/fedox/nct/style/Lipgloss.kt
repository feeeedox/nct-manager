package ovh.fedox.nct.style

import com.williamcallahan.tui4j.compat.lipgloss.Position
import com.williamcallahan.tui4j.compat.lipgloss.Style
import com.williamcallahan.tui4j.compat.lipgloss.color.Color

val CONTAINER_STYLE: Style = Style.newStyle()
    .paddingTop(1)
    .paddingBottom(1)
    .paddingLeft(2)
    .paddingRight(2)

val CHECKMARK_STYLE: Style = Style.newStyle()
    .foreground(Color.color("42"))
    .setString("✓")

val X_STYLE: Style = Style.newStyle()
    .foreground(Color.color("#e32b2b"))
    .setString("✗")

val CREDITS: Style = Style.newStyle()
    .alignHorizontal(Position.Center)
    .align(Position.Center)
    .marginLeft(6)
    .marginTop(2)
    .foreground(Color.color("#6b6a6a"))
    .italic(true)
    .setString("Thanks for using NCT-Manager.\nhttps://github.com/feeeedox/\nhttps://fedox.ovh")

val SPINNER_STYLE: Style = Style.newStyle()
    .foreground(Lipgloss.PRIMARY)

object Lipgloss {
    val PRIMARY: Color = Color.color("#4ab1d4")
}