package net.wuillemin.fxprotest

/**
 * LandscapePortion is a continuous slice of the global landscape. By construction it is guaranteed to be at least
 * 3 unit wide. Trying to build a lower than 3 units LandscapePortion will raise an IllegalArgumentException
 * @param startIndex The start index
 * @param endIndex The end index
 */
data class LandscapePortion(
    val startIndex: Int,
    val endIndex: Int
) {

    init {
        // Ensure that portion are at least 3 unit wide
        if ((endIndex - startIndex) < 2) {
            throw IllegalArgumentException("Trying to create a portion that is less than 3 units wide ($startIndex->$endIndex)")
        }
    }
}