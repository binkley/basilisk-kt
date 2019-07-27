package hm.binkley.basilisk.trip

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test

class SpanTest {
    @Test
    fun shouldSort0() {
        expect(sort(listOf<TestSpan>())).toBe(listOf())
    }

    @Test
    fun shouldSort1() {
        val one = TestSpan(1, 2)

        expect(sort(listOf(one))).toBe(listOf(one))
    }

    @Test
    fun shouldSortMany() {
        val unsorted = (1..100).map {
            TestSpan(it, it + 1)
        }.shuffled()

        expect(sort(unsorted).map { it.start }).toBe((1..100).toList())
    }

    data class TestSpan(override val start: Int, override val end: Int)
        : Span<Int>
}
