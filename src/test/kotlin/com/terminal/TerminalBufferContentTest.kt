package test.kotlin.com.terminal

import com.terminal.TerminalBuffer
import com.terminal.TerminalColour
import org.junit.jupiter.api.Assertions.assertEquals
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
        buffer.setBold()
        buffer.writeText("Old")

        // Height is 2, so 2 newlines will push "Old" into scrollback
        buffer.writeText("\n\n")

        val cell = buffer.scrollbackCellAtPos(0, 0)
        assertEquals('O', cell.char)
        assertEquals(TerminalColour.RED, cell.fgCol)
        assertEquals(true, cell.isBold)
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
}