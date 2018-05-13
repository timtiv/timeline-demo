package com.tim.cubo

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun time_convert_isCorrect() {
        assertEquals(33, TimeConverter.getSeconds(1526230593))
        assertEquals(56, TimeConverter.getMinutes(1526230593))
        assertEquals(16, TimeConverter.getHours(1526230593))
        assertEquals(1555030593, TimeConverter.adjustTimestamp(1526230593))
    }
}
