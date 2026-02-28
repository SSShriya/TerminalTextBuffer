package com.terminal

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
    init {
        require(width > 0) { "Width must be positive: Found width: $width" }
        require(height > 0) { "Height must be positive: Found height: $height" }
        require(maxScrollback >= 0) { "Maximum scrollback must be non-negative: Found scrollback: $maxScrollback" }
    }

    // The screen is a list of cell arrays. Each array is a row of cells
    private var screen = MutableList(height) { Array(width) { Cell() } }

    // The scrollback is the lines that have been scrolled away
    private var scrollback = ArrayDeque<Array<Cell>>()

    // Current buffer attributes - foreground, background and styles
    private var fgCol = TerminalColour.DEFAULT
    private var bgCol = TerminalColour.DEFAULT
    private var isBold = false
    private var isItalic = false
    private var isUnderline = false

    // Cursor information
    private var cursorX = 0
    private var cursorY = 0

    /* Attribute Operations */
    fun setForeground(fg: TerminalColour) {
        fgCol = fg
    }

    fun setBackground(bg: TerminalColour) {
        bgCol = bg
    }

    fun setBold() {
        isBold = true
    }

    fun setItalic() {
        isItalic = true
    }

    fun setUnderline() {
        isUnderline = true
    }

    /* Cursor operations */
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

    // Move cursor one place
    private fun advanceCursor() {
        // If we have reached end of line then go to next one
        if (cursorX >= width - 1) {
            cursorX = 0
            if (cursorY >= height - 1) {
                scroll()
            } else {
                cursorY++
            }
        } else {
            cursorX++
        }
    }

    /* Editing Operations */
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

    // reset all cells to the current state of the buffer
    fun clearScreen() {
        for (y in 0..<height) {
            for (x in 0..<width) {
                screen[y][x] = Cell(null, fgCol, bgCol, isBold, isItalic, isUnderline)
            }
        }
        cursorX = 0
        cursorY = 0
    }

    fun clearScreenAndScrollback() {
        clearScreen()
        scrollback.clear()
    }

    // Content Access operations
    fun cellAtPos(
        x: Int,
        y: Int,
    ) = screen[y][x]

    fun scrollbackCellAtPos(
        x: Int,
        y: Int,
    ) = scrollback[y][x]

    fun getLine(line: Int): String {
        // TODO
        return ""
    }

    fun getScrollbackLine(line: Int): String {
        return ""
    }

    fun getScreenContent(): String {
        // TODO
        return ""
    }

    fun getScreenScrollbackContent(): String {
        // TODO
        return ""
    }

    // Move top line from screen into scrollback
    private fun scroll() {
        val removedLine = screen.removeFirst()
        scrollback.addLast(removedLine)

        // remove the first thing in scroll history if size is too big
        if (scrollback.size > maxScrollback) {
            scrollback.removeFirst()
        }

        // add new line of empty cells to screen
        screen.add(Array(width) { Cell() })
    }
}
