package com.github.thibseisel.mangabind

import java.util.concurrent.atomic.AtomicInteger

class NaturalNumbers(
    startValue: Int = 0,
    private val maxValue: Int = Int.MAX_VALUE
) : IntIterator() {

    init {
        require(startValue <= maxValue) {
            "startValue should not be more than maxValue, but was $startValue > $maxValue"
        }
    }

    private val counter = AtomicInteger(startValue)

    override fun hasNext(): Boolean = counter.get() <= maxValue

    override fun nextInt(): Int = counter.getAndIncrement()
}