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

    @Disabled("Not implemented yet")
    @Test
    fun `get character and attributes from scrollback`() {
        // TODO
    }

    @Disabled("Not implemented yet")
    @Test
    fun `get line as string from screen`() {
        buffer.writeText("Hello")
        val line = buffer.getLine(0)
        assertEquals("Hello     ", line)
    }

    @Disabled("Not implemented yet")
    @Test
    fun `get line as string from scrollback`() {
        // TODO
    }

    @Disabled("Not implemented yet")
    @Test
    fun `get entire screen content as string`() {
        buffer.writeText("A")
        buffer.setCursorPosition(0, 1)
        buffer.writeText("B")
        val expected = "A         \nB         "
        assertEquals(expected, buffer.getScreenContent())
    }

    @Disabled("Not implemented yet")
    @Test
    fun `get entire screen and scrollback content as string`() {
        // TODO
    }
}