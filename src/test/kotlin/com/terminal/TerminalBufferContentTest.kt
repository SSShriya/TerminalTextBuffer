package test.kotlin.com.terminal

import com.terminal.TerminalBuffer
import com.terminal.TerminalColour
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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
        buffer.setForeground(TerminalColour.RED)
        buffer.setBackground(TerminalColour.GREEN)

        buffer.setBold()
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
        buffer.setForeground(TerminalColour.RED)
        buffer.setItalic()
        buffer.setUnderline()
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
        // Fill two lines: row 0: "AAAAAAAAAA",row 1: "BBBBBBBBB"
        buffer.writeText("AAAAAAAAAABBBBBBBBB")

        // Move to the beginning and insert a '!'
        buffer.setCursorPosition(0, 0)
        buffer.insertText("!")

        // Expected Row 0: "!AAAAAAAAA"
        assertEquals("!AAAAAAAAA", buffer.getLine(0))

        // Expected Row 1: "ABBBBBBBBB" because of wrapping
        assertEquals("ABBBBBBBBB", buffer.getLine(1))
    }
}