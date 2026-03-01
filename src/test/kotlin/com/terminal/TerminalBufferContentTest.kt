package test.kotlin.com.terminal

import com.terminal.TerminalBuffer
import com.terminal.TerminalColour
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/* Unit tests for terminal buffer content retrieval */
class TerminalBufferContentTest {
    private lateinit var buffer: TerminalBuffer
    private val width = 10
    private val height = 2

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width, height, maxScrollback = 5)
    }

    @Test
    fun `get character and attributes from screen`() {
        buffer.setAttributes(fg = TerminalColour.RED, bg = TerminalColour.GREEN, bold = true)
        buffer.writeText("Hi")

        val cellI = buffer.cellAtPos(1, 0)
        val cellH = buffer.cellAtPos(0, 0)
        assertEquals('H', cellH.char)
        assertEquals('i', cellI.char)

        assertEquals(TerminalColour.RED, cellI.fgCol)
        assertEquals(TerminalColour.GREEN, cellI.bgCol)
        assertEquals(true, cellH.isBold)
    }

    @Test
    fun `get line as string from screen`() {
        buffer.writeText("Hello")
        val line = buffer.getLine(0)
        assertEquals("Hello", line.trimEnd())
    }


    @Test
    fun `get entire screen content as string`() {
        buffer.writeText("A\n")
        buffer.writeText("B")
        val expected = "A         \nB         "
        assertEquals(expected, buffer.getScreenContent())
    }

    @Test
    fun `scrolling moves lines to scrollback`() {
        buffer.writeText("Line 1\n")
        buffer.writeText("Line 2\n")

        // this is larger than the height so it will move one thing into scrollback
        buffer.writeText("!")

        assertEquals("Line 1", buffer.getScrollbackLine(0).trimEnd())
        assertEquals("Line 2", buffer.getLine(0).trimEnd())
        assertEquals("!", buffer.getLine(1).trimEnd())
    }

    @Test
    fun `get character and attributes from scrollback`() {
        buffer.setAttributes(fg = TerminalColour.RED, italic = true, underline = true)
        buffer.writeText("Old")

        // Height is 2, so 2 newlines will push "Old" into scrollback
        buffer.writeText("\n\n")

        val cell = buffer.scrollbackCellAtPos(0, 0)
        assertEquals('O', cell.char)
        assertEquals(TerminalColour.RED, cell.fgCol)
        assertTrue(cell.isUnderline)
        assertTrue(cell.isItalic)
        assertFalse(cell.isBold)
    }

    @Test
    fun `get line as string from scrollback`() {
        buffer.writeText("History\n")
        buffer.writeText("Screen1\n")
        buffer.writeText("Screen2")

        // "History" should have been pushed out of the screen
        val line = buffer.getScrollbackLine(0)
        assertEquals("History", line.trimEnd())
    }

    @Test
    fun `get entire screen and scrollback content as string`() {
        buffer.writeText("History\n")
        buffer.writeText("Line 0\n")
        buffer.writeText("Line 1")

        val content = buffer.getScreenScrollbackContent()

        val lines = content.lines().map { it.trimEnd() }

        assertEquals("History", lines[0])
        assertEquals("Line 0", lines[1])
        assertEquals("Line 1", lines[2])
    }

    @Test
    fun `fill a line with a character`() {
        buffer.fillLine('A')
        assertEquals("A".repeat(width), buffer.getLine(0))

        buffer.fillLine('B')
        assertEquals("B".repeat(width), buffer.getLine(0))
    }

    @Test
    fun `writeText wraps to next line when width is exceeded`() {
        // Fill the first line exactly
        buffer.writeText("1234567890")

        // Write one more character
        buffer.writeText("A")

        assertEquals("1234567890", buffer.getLine(0))
        assertEquals("A", buffer.getLine(1).trimEnd())
    }

    @Test
    fun `insertText triggers recursive wrapping across multiple lines`() {
        // Fill almost 2 lines
        buffer.writeText("AAAAAAAAAA")
        buffer.writeText("BBBBBBBBB")

        // Move to the beginning and insert a '!'
        buffer.setCursorPosition(0, 0)
        buffer.insertText("!")

        assertEquals("!AAAAAAAAA", buffer.getLine(0))

        assertEquals("ABBBBBBBBB", buffer.getLine(1))
    }

    @Test
    fun `filling the last cell of the last row does not trigger a scroll`() {
        buffer.writeText("ABCDEFJHIJ")
        buffer.writeText("KLMNOPQRS")

        // Write the last char on the grid
        buffer.writeText("T")

        assertEquals("ABCDEFJHIJ", buffer.getLine(0))
        assertEquals("KLMNOPQRST", buffer.getLine(1))

        // Scrollback should still be empty
        assertEquals("", buffer.getScrollbackLine(0))
    }

    @Test
    fun `wrapping at the bottom of the screen triggers a scroll`() {
        // Fill both rows
        buffer.writeText("1234567890") // Row 0
        buffer.writeText("ABCDEFGHIJ") // Row 1

        // Insert at the very top
        buffer.setCursorPosition(0, 0)
        buffer.insertText("!")

        assertEquals("!123456789", buffer.getScrollbackLine(0).trimEnd())
        assertEquals("0ABCDEFGHI", buffer.getLine(0).trimEnd())
    }

    @Test
    fun `inserting into the last possible slot sets up the lazy wrap`() {
        buffer.writeText("A".repeat(width * height - 1))

        buffer.setCursorPosition(width - 1, height - 1)
        buffer.insertText("Z")

        assertEquals(width, buffer.cursorPosition.first)
        assertEquals("", buffer.getScrollbackLine(0))

        buffer.writeText("Next")
        assertEquals("A".repeat(width), buffer.getScrollbackLine(0))
    }

    @Test
    fun `modifying the screen does not change historical lines in scrollback`() {
        buffer.writeText("Original\n")
        buffer.writeText("Screen Line\n")
        buffer.writeText("Trigger Scroll")

        buffer.clearScreen()

        val scrollbackLine = buffer.getScrollbackLine(0).trim()
        assertEquals("Original", scrollbackLine)
    }

    @Test
    fun `insertEmptyLine pushes top line to scrollback and clears bottom`() {
        buffer.writeText("Top Line\n")
        buffer.writeText("BottomLine")

        buffer.insertEmptyLine()

        // "Top Line" should be in scrollback now
        assertEquals("Top Line", buffer.getScrollbackLine(0).trimEnd())

        assertEquals("BottomLine", buffer.getLine(0).trimEnd())

        // The new bottom line should be empty
        assertEquals("", buffer.getLine(1).trim())
    }

    @Test
    fun `insertEmptyLine works repeatedly across circular buffer boundaries`() {
        // Fill every line
        for (i in 0..<height) {
            buffer.writeText("Line $i\n")
        }

        // Insert more empty lines than the height of the screen
        repeat(height + 1) {
            buffer.insertEmptyLine()
        }

        // The screen should be entirely empty now
        for (i in 0..<height) {
            assertEquals("", buffer.getLine(i).trim())
        }

        // The very first line we wrote should be at the start of scrollback
        assertEquals("Line 0", buffer.getScrollbackLine(0).trimEnd())
    }

    @Test
    fun `clearScreenAndScrollback completely empties all buffers and resets cursor`() {
        buffer.writeText("Line 1\n")
        buffer.writeText("Line 2\n")
        buffer.writeText("Line 3")

        // Move cursor away from 0,0
        buffer.setCursorPosition(5, 1)

        buffer.clearScreenAndScrollback()

        // Scrollback should be empty
        assertEquals("", buffer.getScrollbackLine(0))

        // Screen should be empty
        for (i in 0..<height) {
            assertEquals("", buffer.getLine(i).trim())
        }

        // Cursor should be back at the start
        assertEquals(0 to 0, buffer.cursorPosition)
    }

    @Test
    fun `getLine and getScrollbackLine return empty string for invalid indices`() {
        buffer.writeText("Line 0\nLine 1")

        assertEquals("", buffer.getLine(-1))
        assertEquals("", buffer.getLine(2))
        assertEquals("", buffer.getLine(99))

        // Initially empty
        assertEquals("", buffer.getScrollbackLine(0))

        buffer.writeText("\nScroll")

        // Now index 0 in scrollback is valid, but 1 is not
        assertEquals("Line 0", buffer.getScrollbackLine(0).trim())
        assertEquals("", buffer.getScrollbackLine(1))
        assertEquals("", buffer.getScrollbackLine(-1))
    }

    @Test
    fun `scrollback maintains fixed size and evicts oldest lines first`() {
        val smallBuffer = TerminalBuffer(width = 10, height = 1, maxScrollback = 2)

        // Fill the scrollback to the limit
        smallBuffer.writeText("Line 1\n") // Goes to scrollback[0]
        smallBuffer.writeText("Line 2\n") // Goes to scrollback[1]

        assertEquals("Line 1", smallBuffer.getScrollbackLine(0).trim())
        assertEquals("Line 2", smallBuffer.getScrollbackLine(1).trim())

        // Trigger one more scroll - Line 1 should no longer be in scrollback
        smallBuffer.writeText("Line 3\n")

        assertEquals("Line 2", smallBuffer.getScrollbackLine(0).trim())
        assertEquals("Line 3", smallBuffer.getScrollbackLine(1).trim())
        assertEquals("", smallBuffer.getScrollbackLine(2))
    }
}