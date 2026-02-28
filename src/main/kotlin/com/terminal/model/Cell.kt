package main.kotlin.com.terminal.model

/* A terminal buffer consists of a grid of character cells. Each cell can have:
- Character (or empty)
- Foreground color: default, or one of 16 standard terminal colors
- Background color: default, or one of 16 standard terminal colors
- Style flags: bold, italic, underline (at minimum)
*/

// Terminal Colour class that contains default colour, and the 16 standard terminal colours
enum class TerminalColour {
    DEFAULT,
    BLACK,
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    WHITE,
    BRIGHT_BLACK,
    BRIGHT_RED,
    BRIGHT_GREEN,
    BRIGHT_YELLOW,
    BRIGHT_BLUE,
    BRIGHT_MAGENTA,
    BRIGHT_CYAN,
    BRIGHT_WHITE,
}

// Cell class contains necessary information for each character cell
data class Cell(
    val char: Char? = null,
    val fgCol: TerminalColour = TerminalColour.DEFAULT,
    val bgCol: TerminalColour = TerminalColour.DEFAULT,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
)
