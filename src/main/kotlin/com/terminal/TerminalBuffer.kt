package main.kotlin.com.terminal

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
    private var isBold = false
    private var isItalic = false
    private var isUnderline = false

    // Cursor information
    private var cursorX = 0
    private var cursorY = 0

    // Cursor operations
    val cursorPosition: Pair<Int, Int>
        get() = cursorX to cursorY

    fun setCursorPosition(
        x: Int,
        y: Int,
    ) {
        // Cursor should not move outside screen bounds
        cursorX = x.coerceIn(0, width - 1)
        cursorY = y.coerceIn(0, height - 1)
    }

    fun moveCursorBy(
        x: Int,
        y: Int,
    ) {
        setCursorPosition(cursorX + x, cursorY + y)
    }

    private fun advanceCursor() {
        // if we have reached end of line
        if (cursorX >= width - 1) {
            cursorX = 0
            cursorY++
        } else {
            moveCursorBy(1, 0)
        }
    }

    // Editing Operations
    fun writeText(text: String) {
        text.forEach { char ->
            val cell = Cell(char, fgCol, bgCol, isBold, isItalic, isUnderline)
            screen[cursorY][cursorX] = cell
            advanceCursor()
        }
    }

    fun insertText(text: String) {
        // TODO
    }

    fun fillLine(char: Char) {
        // TODO
    }

    fun insertEmptyLine() {
        // TODO
    }

    fun clearScreen() {
        // TODO
    }

    fun clearScreenAndScrollback() {
        // TODO
    }

    // Content Access operations
    fun charAtPos(
        x: Int,
        y: Int,
    ) = screen[x][y]

    fun attrsAtPos(
        x: Int,
        y: Int,
    ) {
        // TODO
    }

    fun getLine() {
        // TODO
    }

    fun getScreenContent() {
        // TODO
    }

    fun getScreenScrollbackContent() {
        // TODO
    }
}
