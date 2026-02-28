package test.kotlin.com.terminal

import com.terminal.TerminalBuffer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
}