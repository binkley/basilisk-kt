package hm.binkley.basilisk

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import org.junit.jupiter.api.Test

class SpanTest {
    val span1 = TestSpan(1, 2)
    val span2 = TestSpan(2, 3)
    val span3 = TestSpan(3, 4)
    val span4 = TestSpan(4, 5)

    @Test
    fun shouldSort0() {
        expect(sort(listOf<TestSpan>()))
                .toBe(listOf())
    }

    @Test
    fun shouldSort1() {
        expect(sort(listOf(span1)))
                .toBe(listOf(span1))
    }

    @Test
    fun shouldSortA() {
        expect(sort(listOf(span1, span2, span3, span4)))
                .toBe(listOf(span1, span2, span3, span4))
    }

    @Test
    fun shouldSortB() {
        expect(sort(listOf(span3, span4, span1, span2)))
                .toBe(listOf(span1, span2, span3, span4))
    }

    data class TestSpan(override val start: Int, override val end: Int)
        : Span<Int>
}
