package net.wuillemin.fxprotest

import kotlin.math.abs


/**
 * Compute the amount of water that can be retained by mountains. This is the main function of the class.
 * @param landscape The landscape to compute
 * @return the amount of water
 */
fun calculateWaterAmount(landscape: IntArray): Long {

    // Find the initial usable part of the landscape. If such a part is found, use it for computing.
    // Otherwise, there won't be any water.
    return buildInitialPortion(landscape)

        ?.let { (initialFrom, initialTo) ->
            calculateWaterAmount(
                landscape,
                listOf(LandscapePortion(initialFrom, initialTo)),
                0
            )
        }
        ?: 0
}

/**
 * Compute the amount of water in a collection of portions. Each portion is taken, its two extremes mountains located,
 * the volume of water computed and if applicable the remaining parts of the original portion are then reused.
 * As there could be a huge number of computation in bad cases, this function is tail call optimized, so that
 * recursive depth is not an issue.
 * @param landscape The landscape
 * @param portions The list of portions for which to compute the water amount
 * @param currentVolume The previous volume. the initial call should use 0.
 * @return The volume of water in the list of portions
 */
private tailrec fun calculateWaterAmount(
    landscape: IntArray,
    portions: List<LandscapePortion>,
    currentVolume: Long
): Long {

    return if (portions.isEmpty()) {
        currentVolume
    } else {
        val firstPortion = portions.first()
        val (from, to) = getTwoHighestMountainIndices(landscape, firstPortion)
        val volumeOfWaterToAdd = getVolumeBetween(landscape, from, to)
        val newPortions = removeFromPortion(firstPortion, from, to) + portions.drop(1)

        calculateWaterAmount(landscape, newPortions, currentVolume + volumeOfWaterToAdd)
    }
}

/**
 * Find the usable part of a landscape. The usable part is the landscape with left and right non usable zones that
 * can not retain water are removed. More formally, we remove the monotonic raising zone (for left) and lowering zone
 * (for right).
 * @param landscape The landscape
 * @return the usable portion of the landscape if any
 */
fun buildInitialPortion(landscape: IntArray): LandscapePortion? {

    // From the left, find the first element which is higher than the next. For example in the following example
    // it will be 2, because it allows water to be present from 3
    //     #
    //   _###....
    //   0123
    return landscape.indexOfFirstIndexed { idx, valueAtIdx -> idx < (landscape.size - 1) && valueAtIdx > landscape[idx + 1] }
        ?.let { from ->
            // From the right, find the first element which is higher than the previous.
            landscape.indexOfLastIndexed { idx, valueAtIdx -> idx > 0 && landscape[idx - 1] < valueAtIdx }
                ?.let { to ->
                    // Ensure that the usable zone found is at least 3 units large
                    if (to - from >= 2) {
                        LandscapePortion(from, to)
                    } else {
                        null
                    }
                }
        }
}

/**
 * Remove a zone (from-to) from a portion and return the new remaining portions if any. Note that the from and to limits
 * are included in the remaining portions.
 * @param portion The portion from which to remove the zone
 * @param from The beginning of the zone to remove
 * @param to The end of the zone to remove
 * @return a list of the remaining portions
 */
fun removeFromPortion(portion: LandscapePortion, from: Int, to: Int): List<LandscapePortion> {

    return if (from - portion.startIndex >= 2) {
        if (portion.endIndex - to >= 2) {
            listOf(
                LandscapePortion(portion.startIndex, from),
                LandscapePortion(to, portion.endIndex)
            )
        } else {
            listOf(LandscapePortion(portion.startIndex, from))
        }
    } else {
        if (portion.endIndex - to >= 2) {
            listOf(LandscapePortion(to, portion.endIndex))
        } else {
            emptyList()
        }
    }
}

/**
 * Compute the volume between two limits. Note that this functions does not check if the data are coherent, ie that
 * there is no higher point between given limits.
 * @param landscape The landscape
 * @param from The beginning of the zone to compute (excluded of the computation)
 * @param to The end of the zone to compute (excluded of the computation)
 * @return the volume.
 */
fun getVolumeBetween(landscape: IntArray, from: Int, to: Int): Long {

    // If nothing to compute, return 0
    return if (to - from <= 1) {
        0
    } else {

        val fromHeight = landscape[from]
        val toHeight = landscape[to]

        // The maximum volume is computed as if there is no landscape between the bounds
        // Add the toLong function to ensure computation will result as long and not to overflow Int
        val maximumVolume = minOf(fromHeight, toHeight).toLong() * (to - from - 1)

        // The landscape volume is just the sum of the height without including the bounds
        // Declare landscapeVolume to be Long to ensure computation will result as long and not to overflow Int
        var landscapeVolume = 0L
        for (i in (from + 1) until to) {
            landscapeVolume += landscape[i]
        }

        (maximumVolume - landscapeVolume)
    }
}

/**
 * Search the two highest mountains of a portion. This function, is selecting not only the two top points, but in
 * case of equality, the most separated points. This allows for eager consuming the landscape, which gives way
 * better performances
 * @param landscape The landscape
 * @param portion The portion of the landscape in which to look for
 * @return a pair of indices for the two highest points to consider. This points are always sorted by indices
 * (and not by height)
 */
fun getTwoHighestMountainIndices(landscape: IntArray, portion: LandscapePortion): Pair<Int, Int> {

    // This values will always be defined (as a portion is defined as being at least 3 Units). So
    // the values can be defined to whatever so that it is ensured that will never be null
    var firstMaxIndex = Int.MIN_VALUE
    var secondMaxIndex = Int.MIN_VALUE

    var firstMaxValue = Int.MIN_VALUE
    var secondMaxValue = Int.MIN_VALUE

    // Run all the elements to find the highest mountains
    for (currentIndex in portion.startIndex..portion.endIndex) {

        val currentValue = landscape[currentIndex]

        // As there are a lot of tests involved, start by filtering out the value that won't produce any change. This
        // should represent the majority of the values in fact.
        if (currentValue >= secondMaxValue) {

            // The idea is not only to find the highest separated mountains, but to find the two more farther points
            // if there is some equally high. This way the mountains can be consumed eagerly which offer a vast
            // performance improvement. But this leads to a lot of cases, so let's express them humanly before coding
            // them:
            //           1   max1 > max2   |    cur > max1     |     max2 = max1 AND max1 = cur
            //           2   max1 > max2   |    cur = max1     |     max2 = cur
            //           3   max1 > max2   |    cur > max2     |     max2 = cur
            //           4   max1 > max2   |    cur = max2     |     max2 = farthestFromMax1(max2, cur)
            //           5   max1 = max2   |    cur > max1     |     max2 = smallerIndex(max1, max2) AND max1 = cur
            //           6   max1 = max2   |    cur = max1     |     max2 = smallerIndex(max1, max2) AND max1 = cur
            //           7   max1 = max2   |    cur > max2     |     same as 5 as max1 = max2
            //           8   max1 = max2   |    cur = max2     |     same as 6 as max1 = max2
            //
            //  * For the case 4, max 1 is constant and the choice for max 2 is between cur and max2.
            //  * For the cases 5 and 6, as index are always going up, cur is necessarily a new extreme to be used. It
            //    is just needed to find the lower index.
            //    => So case 5, 6, 7 and 8 are the same

            if (firstMaxValue > secondMaxValue) {

                if (currentValue > firstMaxValue) {
                    // Case 1
                    secondMaxValue = firstMaxValue
                    secondMaxIndex = firstMaxIndex
                    firstMaxValue = currentValue
                    firstMaxIndex = currentIndex

                } else if ((currentValue == firstMaxValue) || (currentValue > secondMaxValue)) {
                    // Cases 2 and 3
                    secondMaxValue = currentValue
                    secondMaxIndex = currentIndex
                } else {
                    // case 4
                    if (abs(firstMaxIndex - currentIndex) > abs(firstMaxIndex - secondMaxIndex)) {
                        secondMaxValue = currentValue
                        secondMaxIndex = currentIndex
                    }
                }
            } else {
                // Cases 5, 6, 7and 8
                if (firstMaxIndex < secondMaxIndex) {
                    secondMaxValue = firstMaxValue
                    secondMaxIndex = firstMaxIndex
                }
                firstMaxValue = currentValue
                firstMaxIndex = currentIndex
            }
        }
    }

    // Return the two highest ordered
    return if (firstMaxIndex < secondMaxIndex) {
        Pair(firstMaxIndex, secondMaxIndex)
    } else {
        Pair(secondMaxIndex, firstMaxIndex)
    }
}

/**
 * Returns index of the first element matching the given [predicate], or null if the array does not contain such element.
 */
private fun IntArray.indexOfFirstIndexed(predicate: (Int, Int) -> Boolean): Int? {
    for (index in indices) {
        if (predicate(index, this[index])) {
            return index
        }
    }
    return null
}

/**
 * Returns index of the last element matching the given [predicate], or null if the array does not contain such element.
 */
private fun IntArray.indexOfLastIndexed(predicate: (Int, Int) -> Boolean): Int? {
    for (index in indices.reversed()) {
        if (predicate(index, this[index])) {
            return index
        }
    }
    return null
}

