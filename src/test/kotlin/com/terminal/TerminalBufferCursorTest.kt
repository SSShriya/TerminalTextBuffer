package test.kotlin.com.terminal

import com.terminal.TerminalBuffer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/* Unit tests for Terminal Buffer Cursor operations */
class TerminalBufferCursorTest {
    private val width = 80
    private val height = 24
    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width, height, maxScrollback = 100)
    }

    @Test
    fun `cursor should start at top left corner`() {
        val (x, y) = buffer.cursorPosition
        assertEquals(0, x)
        assertEquals(0, y)
    }

    @Test
    fun `sets cursor to a valid position`() {
        val newX = 12
        val newY = 3
        buffer.setCursorPosition(newX, newY)
        assertEquals(newX to newY, buffer.cursorPosition)
    }

    @Test
    fun `move cursor to a valid position`() {
        val xMov = 4
        val yMov = 5
        buffer.moveCursorBy(xMov, yMov)
        assertEquals(xMov to yMov, buffer.cursorPosition)

        buffer.moveCursorBy(xMov, yMov)
        assertEquals(2 * xMov to 2 * yMov, buffer.cursorPosition)
    }

    @Test
    fun `cursor should not be set outside of screen bounds`() {
        buffer.setCursorPosition(-1, -1)
        assertEquals(0 to 0, buffer.cursorPosition)

        buffer.setCursorPosition(width, height)
        assertEquals(width - 1 to height - 1, buffer.cursorPosition)

        buffer.setCursorPosition(width * 2, -1)
        assertEquals(width - 1 to 0, buffer.cursorPosition)

        buffer.setCursorPosition(-23, height * 2)
        assertEquals(0 to height - 1, buffer.cursorPosition)

        buffer.setCursorPosition(12, height * 2)
        assertEquals(12 to height - 1, buffer.cursorPosition)

        buffer.setCursorPosition(-14, 15)
        assertEquals(0 to 15, buffer.cursorPosition)
    }

    @Test
    fun `cursor should not be moved outside of screen bounds`() {
        buffer.moveCursorBy(-5, -5)
        assertEquals(0 to 0, buffer.cursorPosition)

        buffer.moveCursorBy(500, 500)
        assertEquals(width - 1 to height - 1, buffer.cursorPosition)
    }

    @Test
    fun `cursor should move when text is written on a line`() {
        buffer.writeText("Hi")
        assertEquals(2 to 0, buffer.cursorPosition)
    }

    @Disabled("Not implemented yet")
    @Test
    fun `cursor should move when text is inserted on a line`() {
        buffer.insertText("Hi")
        assertEquals(2 to 0, buffer.cursorPosition)
    }

    @Disabled("Not implemented yet")
    @Test
    fun `cursor should move when a line is filled with a character`() {
        buffer.fillLine('A')
        assertEquals(0 to 1, buffer.cursorPosition)

        buffer.fillLine('B')
        assertEquals(0 to 2, buffer.cursorPosition)
    }
}