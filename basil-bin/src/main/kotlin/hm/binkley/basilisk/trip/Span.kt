package hm.binkley.basilisk.trip

interface Span<T> {
    val start: T
    val end: T
}

fun <T, S : Span<T>> sort(unsorted: Iterable<S>): Pair<Iterable<S>, Int> {
    class SpanList<T, S : Span<T>>(private val backing: MutableList<S>)
        : MutableList<S> by backing,
            Span<T> {
        constructor(s: S) : this(mutableListOf(s))

        override val start: T
            get() = backing.first().start
        override val end: T
            get() = backing.last().end

        override fun equals(other: Any?) = backing == other
        override fun hashCode() = backing.hashCode()
        override fun toString() = backing.toString()
    }

    val toSort = unsorted.map { SpanList(it) }.toMutableList()
    when (toSort.size) {
        0, 1 -> return Pair(unsorted, 0)
    }

    // TODO: A better algorithm -- check Knuth
    var loops = 0
    var i = 0
    top@ while (i < toSort.size) {
        ++loops
        val curr = toSort[i]
        var j = 0
        while (j < toSort.size) {
            ++loops
            val sub = toSort[j]
            if (curr.end == sub.start) {
                curr.addAll(sub)
                toSort.removeAt(j)
                continue
            } else if (curr.start == sub.end) {
                sub.addAll(curr)
                toSort.removeAt(i)
                continue@top
            }
            ++j
        }
        ++i
    }

    return Pair(toSort[0], loops)
}
