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

    /* ATTRIBUTES */
    // The screen is a list of cell arrays. Each array is a row of cells
    private var screen = MutableList(height) { Array(width) { Cell() } }

    // The scrollback is the lines that have been scrolled away - a double ended queue
    // is used to allow efficient accesses to both the top and bottom of the scrollback
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

    /* ATTRIBUTE OPERATIONS */
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

    /* CURSOR OPERATIONS  */
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

    /* EDITING OPERATIONS */
    fun writeText(text: String) {
        text.forEach { char ->
            if (char == '\n') {
                writeNewLine()
            } else {
                val cell = createCell(char)
                screen[cursorY][cursorX] = cell
                advanceCursor()
            }
        }
    }

    fun insertText(text: String) {
        text.forEach { char ->
            // save the last char in row
            val lastCell = screen[cursorY][width - 1]

            // move all chars to right
            for (x in (width - 1) downTo (cursorX + 1)) {
                screen[cursorY][x] = screen[cursorY][x -1]
            }

            // add new char into current pos
            screen[cursorY][cursorX] = createCell(char)

            // if last cell wasn't empty then wrap last char
            if (lastCell.char != ' ') {
                wrapCell(lastCell)
            }

            advanceCursor()
        }
    }

    fun fillLine(char: Char) {
        for (x in 0..<width) {
            screen[cursorY][x] = createCell(char)
        }
    }

    fun insertEmptyLine() {
        screen.removeLast()
        val newline = Array(width) { createCell(' ') }

        screen.add(cursorY, newline)
    }

    // reset all cells to the current state of the buffer
    fun clearScreen() {
        for (y in 0..<height) {
            for (x in 0..<width) {
                screen[y][x] = createCell(' ')
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

    fun getLine(line: Int): String =
        screen[line].map { it.char }.joinToString("")

    fun getScrollbackLine(line: Int): String =
        scrollback[line].map { it.char }.joinToString("")

    fun getScreenContent(): String =
        (0..<height).joinToString("\n") { getLine(it) }

    fun getScreenScrollbackContent(): String {
        val sb = StringBuilder()

        // all scrollback lines
        for (i in 0..<scrollback.size) {
            sb.append(getScrollbackLine(i)).append("\n")
        }

        sb.append(getScreenContent())
        return sb.toString()
    }

    /* HELPER FUNCTIONS */
    private fun createCell(char: Char): Cell = Cell(char, fgCol, bgCol, isBold, isItalic, isUnderline)

    // Move cursor one place
    private fun advanceCursor() {
        cursorX++
        // If we have reached end of line then go to next one
        if (cursorX >= width) {
            writeNewLine()
        }
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

    private fun writeNewLine() {
        cursorX = 0
        if (cursorY >= height - 1) {
            scroll()
        } else {
            cursorY++
        }
    }

    // wraps overflowed chars to next row
    private fun wrapCell(cell: Cell) {
        // scroll if at bottom of screen
        if (cursorY >= height - 1) {
            scroll()
        }

        // shift the next line
        val nextY = (cursorY + 1).coerceAtMost(height - 1)
        for (x in (width - 1) downTo 1) {
            screen[nextY][x] = screen[nextY][x - 1]
        }
        screen[nextY][0] = cell
    }
}
