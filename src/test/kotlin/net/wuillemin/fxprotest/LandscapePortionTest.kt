package net.wuillemin.fxprotest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals

class LandscapePortionTest {

    @Test
    fun `can create a LandscapePortion`() {
        val portion = LandscapePortion(1, 10)
        assertEquals(1, portion.startIndex)
        assertEquals(10, portion.endIndex)
    }

    @Test
    fun `can not create inverted LandscapePortion`() {

        assertThrows(IllegalArgumentException::class.java) {
            LandscapePortion(10, 1)
        }
    }

    @Test
    fun `can not create too small (lte 2 units) LandscapePortion`() {

        assertThrows(IllegalArgumentException::class.java) {
            LandscapePortion(10, 10)
        }

        assertThrows(IllegalArgumentException::class.java) {
            LandscapePortion(10, 11)
        }
    }
}