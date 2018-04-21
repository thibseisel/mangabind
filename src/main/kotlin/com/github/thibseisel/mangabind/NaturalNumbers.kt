package com.github.thibseisel.mangabind

import java.util.concurrent.atomic.AtomicInteger

/**
 * An increasing sequence of natural numbers, starting at zero (or a given value)
 * and incrementing by 1 until reaching [Int.MAX_VALUE] (or an optional maximum).
 *
 * This sequence is thread safe and can be accessed concurrently.
 *
 * @constructor Builds a new sequence of natural numbers.
 * @param startValue The first value of the sequence, defaults to `0`. Should be equal of less than [maxValue].
 * @param maxValue The last value of the sequence, defaults to [Int.MAX_VALUE].
 */
class NaturalNumbers(
    startValue: Int = 0,
    private val maxValue: Int = Int.MAX_VALUE
) : IntIterator() {

    init {
        require(startValue <= maxValue) {
            "startValue should not be more than maxValue, but was $startValue > $maxValue"
        }
    }

    /**
     * Increasing counter, guaranteed to be thread-safe due to its built-in atomic increment operations.
     */
    private val counter = AtomicInteger(startValue)

    override fun hasNext(): Boolean = counter.get() <= maxValue

    override fun nextInt(): Int = counter.getAndIncrement()
}