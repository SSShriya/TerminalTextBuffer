package main.kotlin.com.terminal

/* A terminal buffer consists of a grid of character cells. Each cell can have:
- Character (or empty)
- Foreground color: default, or one of 16 standard terminal colors
- Background color: default, or one of 16 standard terminal colors
- Style flags: bold, italic, underline (at minimum)
*/

// Cell class contains necessary information for each character cell
data class Cell(
    val char: Char? = null,
    val fgCol: TerminalColour = TerminalColour.DEFAULT,
    val bgCol: TerminalColour = TerminalColour.DEFAULT,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
)
