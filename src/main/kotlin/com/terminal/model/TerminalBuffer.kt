package main.kotlin.com.terminal.model

/*
The buffer maintains a cursor position — where the next character will be written.
The buffer has two logical parts:
- Screen — the last N lines that fit the screen dimensions (e.g., 80×24). This is the editable part and what users see.
- Scrollback — lines that scrolled off the top of the screen, preserved for history and unmodifiable. Users can scroll up to view them.
*/

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollback: Int,
) {
    // The screen is a list of cell arrays. Each array is a row of cells
    private var screen = MutableList(height) { Array(width) { Cell() } }

    // The scrollback is the lines that have been scrolled away
    private var scrollback = MutableList(width) { Array(height) { Cell() } }

    // Current buffer attributes - foreground, background and styles
    private var fgCol = TerminalColour.DEFAULT
    private var bgCol = TerminalColour.DEFAULT
    private var bold = false
    private var italic = false
    private var underline = false

    // Cursor information
    private var cursorX = 0
    private var cursorY = 0

    val cursorPosition: Pair<Int, Int>
        get() = cursorX to cursorY

    fun setCursorPosition(
        x: Int,
        y: Int,
    ) {
        cursorX = x
        cursorY = y
    }

    fun moveCursorBy(
        x: Int,
        y: Int,
    ) {
        setCursorPosition(cursorX + x, cursorY + y)
    }
}
