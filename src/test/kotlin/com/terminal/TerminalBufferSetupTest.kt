package test.kotlin.com.terminal

import com.terminal.TerminalBuffer
import com.terminal.TerminalColour
import com.terminal.nullChar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/* Unit tests for Terminal Buffer Setup */
class TerminalBufferSetupTest {
    @Test
    fun `initialises a terminal buffer with correct dimensions`() {
        val width = 80
        val height = 24
        val buffer = TerminalBuffer(width, height, maxScrollback = 100)
        assertEquals(width, buffer.width)
        assertEquals(height, buffer.height)
    }

    @Test
    fun `initialises a screen with empty cells`() {
        val buffer = TerminalBuffer(20, 10, 5)
        val topLeft = buffer.cellAtPos(0, 0)
        val bottomRight = buffer.cellAtPos(19, 9)

        assertEquals(nullChar, topLeft.char)
        assertFalse(topLeft.isBold)
        assertFalse(topLeft.isItalic)
        assertFalse(topLeft.isUnderline)
        assertEquals(TerminalColour.DEFAULT, topLeft.fgCol)
        assertEquals(TerminalColour.DEFAULT, topLeft.bgCol)

        assertEquals(nullChar, bottomRight.char)
        assertFalse(bottomRight.isBold)
        assertFalse(bottomRight.isItalic)
        assertFalse(bottomRight.isUnderline)
        assertEquals(TerminalColour.DEFAULT, bottomRight.fgCol)
        assertEquals(TerminalColour.DEFAULT, bottomRight.bgCol)
    }

    @Test
    fun `throws exception when buffer initialised with invalid width`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            TerminalBuffer(0, 24, 100)
        }

        assertTrue(exception.message!!.contains("width"))
    }

    @Test
    fun `throws exception when buffer initialised with invalid height`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            TerminalBuffer(80, -24, 100)
        }
        println(exception.message)

        assertTrue(exception.message!!.contains("height"))
    }

    @Test
    fun `throws exception when buffer initialised with invalid scrollback`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            TerminalBuffer(80, 24, -100)
        }

        assertTrue(exception.message!!.contains("scrollback"))
    }
}