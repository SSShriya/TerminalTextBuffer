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
    // The screen is a 2d array of cells.
    private var screen = Array(height) { Array(width) { Cell() } }

    // The scrollback is the lines that have been scrolled away - a double ended queue
    // is used to allow efficient accesses to both the top and bottom of the scrollback
    private var scrollback = ArrayDeque<Array<Cell>>()

    // Pointer to the top row
    private var topIndex = 0

    // Current buffer attributes - foreground, background and styles
    private var fgCol = TerminalColour.DEFAULT
    private var bgCol = TerminalColour.DEFAULT
    private var isBold = false
    private var isItalic = false
    private var isUnderline = false

    // Cursor information
    private var cursorX = 0
    private var cursorY = 0

    // Returns the actual position of a row in the array, given a row number
    private fun physicalY(y: Int) = (topIndex + y) % height

    /* ATTRIBUTE OPERATIONS */
    fun setAttributes(
        fg: TerminalColour = fgCol,
        bg: TerminalColour = bgCol,
        bold: Boolean = isBold,
        italic: Boolean = isItalic,
        underline: Boolean = isUnderline,
    ) {
        fgCol = fg
        bgCol = bg
        isBold = bold
        isItalic = italic
        isUnderline = underline
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
                if (cursorX >= width) {
                    writeNewLine()
                }
                screen[physicalY(cursorY)][cursorX] = createCell(char)
                advanceCursor()
            }
        }
    }

    fun insertText(text: String) {
        text.forEach { char ->
            var curX = cursorX
            var curY = cursorY
            var cellToInsert = createCell(char)

            // need to shift all rows
            while (curY < height) {
                val pY = physicalY(curY)
                val row = screen[pY]
                val overflowCell = row[width - 1]

                // Shift row right
                System.arraycopy(row, curX, row, curX + 1, width - 1 - curX)
                row[curX] = cellToInsert

                // if the very last cell is empty, don't need to do anything
                if (overflowCell.char == ' ' || overflowCell.char == nullChar) {
                    break
                }

                // wrap to the start of the next line
                cellToInsert = overflowCell
                curX = 0
                curY++
                if (curY >= height) {
                    scroll()
                }
            }
            advanceCursor()
        }
    }

    fun fillLine(char: Char) {
        val pY = physicalY(cursorY)
        for (x in 0..<width) {
            screen[pY][x] = createCell(char)
        }
    }

    fun insertEmptyLine() =
        scroll()

    // reset all cells to the current state of the buffer
    fun clearScreen() {
        for (y in 0..<height) {
            val pY = physicalY(y)
            for (x in 0..<width) {
                screen[pY][x] = createCell(nullChar)
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
    ) : Cell {
        val pY = physicalY(y.coerceIn(0, height - 1))
        val pX = x.coerceIn(0, width - 1)
        return screen[pY][pX]
    }

    fun scrollbackCellAtPos(
        x: Int,
        y: Int,
    ): Cell {
        return scrollback[y][x]
    }

    fun getLine(line: Int): String {
        if (line !in 0..<height) return ""
        val sb = StringBuilder(width)
        val pY = physicalY(line)
        val row = screen[pY]
        for (cell in row) {
            sb.append(if (cell.char == nullChar) ' ' else cell.char)
        }
        return sb.toString()
    }

    fun getScrollbackLine(line: Int): String {
        if (line !in 0..<scrollback.size) return ""
        val row = scrollback[line]
        val sb = StringBuilder(width)
        for (cell in row) {
            sb.append(if (cell.char == nullChar || cell.char == '\u0000') ' ' else cell.char)
        }
        return sb.toString()
    }

    fun getScreenContent(): String {
        val sb = StringBuilder(height * (width - 1))

        for (y in 0..<height) {
            val row = screen[physicalY(y)]
            for (x in 0..<width) {
                val cell = row[x]
                sb.append(if (cell.char == nullChar) ' ' else cell.char)
            }

            if (y < height - 1) {
                sb.append('\n')
            }
        }
        return sb.toString()
    }

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

    // Move cursor one place forward as long as it is smaller than the width
    private fun advanceCursor() {
        if (cursorX < width) {
            cursorX++
        }
    }

    // Move top line from screen into scrollback
    private fun scroll() {
        val oldTopIndex = topIndex
        val rowToScroll = screen[oldTopIndex]
        scrollback.addLast(rowToScroll.copyOf())

        // remove the first thing in scroll history if size is too big
        if (scrollback.size > maxScrollback) {
            scrollback.removeFirst()
        }

        // replace the row with an empty row so it can be reused
        for (x in 0..<width) {
            screen[oldTopIndex][x] = createCell(nullChar)
        }

        // increment the top of the circular queue
        topIndex = (topIndex + 1) % height
    }

    // If a newline is written, then move to the next line/scroll
    private fun writeNewLine() {
        cursorX = 0
        if (cursorY >= height - 1) {
            scroll()
        } else {
            cursorY++
        }
    }
}
