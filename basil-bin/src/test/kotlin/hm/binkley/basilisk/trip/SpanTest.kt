package hm.binkley.basilisk.trip

import ch.tutteli.atrium.api.cc.en_GB.isLessOrEquals
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test

class SpanTest {
    @Test
    fun shouldSort0() {
        val (sorted, loops) = sort(listOf<TestSpan>())

        expect(sorted).toBe(listOf())
        expect(loops).toBe(0)
    }

    @Test
    fun shouldSort1() {
        val one = TestSpan(1, 2)

        val (sorted, loops) = sort(listOf(one))

        expect(sorted).toBe(listOf(one))
        expect(loops).toBe(0)
    }

    @Test
    fun shouldSortMany() {
        val unsorted = (1..100).map {
            TestSpan(it, it + 1)
        }.shuffled()

        val (sorted, loops) = sort(unsorted)

        expect(sorted.map { it.start }).toBe((1..100).toList())
        // TODO: The actual expectation is approximate with random input
        expect(loops).isLessOrEquals(2000)
    }

    data class TestSpan(override val start: Int, override val end: Int)
        : Span<Int>
}
