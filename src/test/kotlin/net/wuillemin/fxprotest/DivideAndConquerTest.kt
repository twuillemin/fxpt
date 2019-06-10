package net.wuillemin.fxprotest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Duration.ofMinutes
import kotlin.random.Random
import kotlin.system.measureTimeMillis


class DivideAndConquerTest {

    // -----------------------------------------------------------------------------
    //
    // Tests on main function
    //
    // -----------------------------------------------------------------------------
    @Test
    fun `Can compute with two mountains`() {
        assertEquals(1, calculateWaterAmount(makeLandscape(1, 2, 1, 3, 3, 0, 0)))
    }

    @Test
    fun `Can compute with three mountains`() {

        //            #
        //    # . . . #
        //    # . # . #
        //  # # # # # # _
        //  0 1 2 3 4 5 6
        assertEquals(5, calculateWaterAmount(makeLandscape(1, 3, 1, 2, 1, 4, 0)))

        //        #
        //    # . #
        //    # . # . #
        //  # # # # # # _
        //  0 1 2 3 4 5 6
        assertEquals(3, calculateWaterAmount(makeLandscape(1, 3, 1, 4, 1, 2, 0)))
    }

    @Test
    fun `Can compute requested exercise`() {
        assertEquals(9, calculateWaterAmount(makeLandscape(5, 2, 3, 4, 5, 4, 0, 3, 1)))
    }

    @Test
    fun `Computed volume is 0 on inappropriate landscape`() {
        assertEquals(0, calculateWaterAmount(makeLandscape(2, 3, 4, 5, 6)))
    }

    @Test
    fun `Can compute 32K landscape in less than 10 minutes - wow dude, that's ambitious`() {

        val randomValues = List(32000) { Random.nextInt(0, 32001) }
        val landscape = makeLandscape(randomValues)

        assertTimeout(ofMinutes(10)) {
            calculateWaterAmount(landscape)
        }
    }

    // Put as disable to not break computer of people running test (just in case)
    @Test
    @Disabled
    fun `Run a 4 000 000 mountains`() {

        val randomValues = List(4_000_000) { Random.nextInt(0, 32001) }
        val landscape = makeLandscape(randomValues)

        var waterVolume = 0L
        val nbMillis = measureTimeMillis {
            waterVolume = calculateWaterAmount(landscape)
        }
        System.out.println("Time for 4_000_000 : $nbMillis ms (water was $waterVolume)")
    }

    // -----------------------------------------------------------------------------
    //
    // Tests on build initial portion
    //
    // -----------------------------------------------------------------------------
    @Test
    fun `Can find initial portion of landscape`() {
        val portion = buildInitialPortion(makeLandscape(1, 2, 1, 3, 3, 0, 0))
        assertNotNull(portion)
        assertEquals(1, portion?.startIndex)
        assertEquals(3, portion?.endIndex)
    }

    @Test
    fun `Initial portion of flat is not valid`() {
        assertNull(buildInitialPortion(makeLandscape(2, 2, 2, 2)))
    }

    @Test
    fun `Initial portion of central mountain is not valid`() {
        assertNull(buildInitialPortion(makeLandscape(2, 3, 4, 3, 2)))
    }

    @Test
    fun `Initial portion of monotonic is not valid`() {
        assertNull(buildInitialPortion(makeLandscape(1, 2, 2, 25, 25)))
        assertNull(buildInitialPortion(makeLandscape(10, 9, 8, 7, 6, 0)))
    }

    // -----------------------------------------------------------------------------
    //
    // Tests on remove from portion
    //
    // -----------------------------------------------------------------------------

    @Test
    fun `Can remove portion center`() {
        val portions = removeFromPortion(LandscapePortion(0, 9), 3, 6)
        assertEquals(2, portions.size)
        assertEquals(0, portions[0].startIndex)
        assertEquals(3, portions[0].endIndex)
        assertEquals(6, portions[1].startIndex)
        assertEquals(9, portions[1].endIndex)
    }

    @Test
    fun `Can remove portion left`() {
        val portions = removeFromPortion(LandscapePortion(0, 9), 2, 8)
        assertEquals(1, portions.size)
        assertEquals(0, portions[0].startIndex)
        assertEquals(2, portions[0].endIndex)
    }

    @Test
    fun `Can remove portion right`() {
        val portions = removeFromPortion(LandscapePortion(0, 9), 1, 7)
        assertEquals(1, portions.size)
        assertEquals(7, portions[0].startIndex)
        assertEquals(9, portions[0].endIndex)
    }

    @Test
    fun `Can remove portion full`() {
        val portions = removeFromPortion(LandscapePortion(0, 9), 0, 9)
        assertTrue(portions.isEmpty())
    }

    // -----------------------------------------------------------------------------
    //
    // Tests on compute volume
    //
    // -----------------------------------------------------------------------------


    @Test
    fun `Can compute volume`() {
        val volume1 = getVolumeBetween(makeLandscape(5, 4, 3), 0, 2)
        assertEquals(-1, volume1)

        val volume2 = getVolumeBetween(makeLandscape(9, 2, 0, 0, 3, 9), 1, 4)
        assertEquals(4, volume2)

        val volume3 = getVolumeBetween(makeLandscape(9, 2, 0, 0, 3, 9), 1, 2)
        assertEquals(0, volume3)
    }

    @Test
    fun `Can compute volume is not happy with bad portion`() {
        assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            getVolumeBetween(makeLandscape(5, 4, 3), 0, 3)
        }
    }

    // -----------------------------------------------------------------------------
    //
    // Tests for highest mountains
    //
    // -----------------------------------------------------------------------------

    @Test
    fun `Can find two highest mountains`() {
        val (from1, to1) = getTwoHighestMountainIndices(makeLandscape(5, 4, 3), LandscapePortion(0, 2))
        assertEquals(0, from1)
        assertEquals(1, to1)

        val (from2, to2) = getTwoHighestMountainIndices(makeLandscape(2, 5, 3, 4, 2), LandscapePortion(0, 4))
        assertEquals(1, from2)
        assertEquals(3, to2)

        val (from3, to3) = getTwoHighestMountainIndices(makeLandscape(9, 3, 2, 4, 9), LandscapePortion(1, 3))
        assertEquals(1, from3)
        assertEquals(3, to3)
    }

    @Test
    fun `Tow Highest mountains should be eager`() {

        val (from1, to1) = getTwoHighestMountainIndices(makeLandscape(4, 0, 0, 5, 0, 4), LandscapePortion(0, 5))
        assertEquals(0, from1)
        assertEquals(3, to1)

        val (from2, to2) = getTwoHighestMountainIndices(makeLandscape(4, 0, 5, 0, 0, 4), LandscapePortion(0, 5))
        assertEquals(2, from2)
        assertEquals(5, to2)

        val (from3, to3) = getTwoHighestMountainIndices(makeLandscape(4, 0, 4, 0, 0, 4), LandscapePortion(0, 5))
        assertEquals(0, from3)
        assertEquals(5, to3)
    }

    @Test
    fun `Find two highest mountains is not happy with bad portion`() {

        assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            getTwoHighestMountainIndices(makeLandscape(9, 3, 2, 4, 9), LandscapePortion(10, 12))
        }
    }

    // -----------------------------------------------------------------------------
    //
    // Utilities
    //
    // -----------------------------------------------------------------------------

    private fun makeLandscape(vararg elements: Int): IntArray {
        val array = IntArray(elements.size)
        elements.forEachIndexed { idx, value -> array[idx] = value }
        return array
    }

    private fun makeLandscape(elements: List<Int>): IntArray {
        val array = IntArray(elements.size)
        elements.forEachIndexed { idx, value -> array[idx] = value }
        return array
    }
}


