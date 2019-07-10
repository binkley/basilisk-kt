package hm.binkley.basilisk

interface Span<T> {
    val start: T
    val end: T
}

fun <T, S : Span<T>> sort(unsorted: Iterable<S>): Iterable<S> {
    class SpanList<T, S : Span<T>>(private val l: MutableList<S>)
        : MutableList<S> by l, Span<T> {
        constructor(s: S) : this(mutableListOf(s))

        override val start: T
            get() = l.first().start
        override val end: T
            get() = l.last().end

        override fun equals(other: Any?) = l.equals(other)
        override fun hashCode() = l.hashCode()
        override fun toString() = l.toString()
    }

    val toSort = unsorted.map { SpanList(it) }.toMutableList()
    when (toSort.size) {
        0, 1 -> return unsorted
    }

    var i = 0
    next@ while (i < toSort.size) {
        val curr = toSort[i]
        var j = 0
        while (j < toSort.size) {
            val sub = toSort[j]
            if (curr.start == sub.end) {
                sub.addAll(curr)
                toSort.removeAt(i)
                continue@next
            } else if (curr.end == sub.start) {
                curr.addAll(sub)
                toSort.removeAt(j)
                continue@next
            }
            ++j
        }
        ++i
    }

    return toSort[0]
}
