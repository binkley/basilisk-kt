package hm.binkley.basilisk.trip

import ch.tutteli.atrium.api.cc.en_GB.contains
import ch.tutteli.atrium.api.cc.en_GB.hasSize
import ch.tutteli.atrium.api.cc.en_GB.inAnyOrder
import ch.tutteli.atrium.api.cc.en_GB.isLessOrEquals
import ch.tutteli.atrium.api.cc.en_GB.only
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.values
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test

class SpanTest {
    @Test
    fun shouldSort0() {
        val (sorted, cost) = sort(listOf<TestSpan>())

        expect(sorted).toBe(listOf(listOf()))
        expect(cost).toBe(0)
    }

    @Test
    fun shouldSort1() {
        val one = TestSpan(1, 2)

        val (sorted, cost) = sort(listOf(one))

        expect(sorted).toBe(listOf(listOf(one)))
        expect(cost).toBe(0)
    }

    @Test
    fun shouldSortMany() {
        val unsorted = (1..100).map {
            TestSpan(it, it + 1)
        }.shuffled()

        val (iter, cost) = sort(unsorted)
        val spans = iter.toList()

        expect(spans).hasSize(1)

        val sorted = spans.first()

        expect(sorted.map { it.start }).toBe((1..100).toList())
        // TODO: The actual expectation is approximate with random input
        expect(cost).isLessOrEquals(2000)
    }

    @Test
    fun shouldHandleBrokenSpans() {
        // No predictable ordering when spans are broken
        val a = TestSpan(0, 1)
        val b = TestSpan(2, 3)
        val unsorted = listOf(a, b)

        val (iter, _) = sort(unsorted)
        val spans = iter.toList()

        expect(spans).hasSize(2)
        expect(spans).contains.inAnyOrder.only.values(listOf(a), listOf(b))
    }

    data class TestSpan(override val start: Int, override val end: Int)
        : Span<Int>
}
