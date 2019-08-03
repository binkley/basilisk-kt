package hm.binkley.basilisk.trip

interface Span<T> {
    val start: T
    val end: T
}

fun <T, S : Span<T>> sort(
        unsorted: Iterable<S>): Pair<Iterable<Iterable<S>>, Int> {
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

    val sorted = unsorted.map { SpanList(it) }.toMutableList()
    when (sorted.size) {
        0, 1 -> return Pair(listOf(unsorted), 0)
    }

    // TODO: A better algorithm -- check Knuth
    var loops = 0
    var i = 0
    top@ while (i < sorted.size) {
        ++loops
        val curr = sorted[i]
        var j = 0
        while (j < sorted.size) {
            ++loops
            val sub = sorted[j]
            if (curr.end == sub.start) {
                curr.addAll(sub)
                sorted.removeAt(j)
                continue
            } else if (curr.start == sub.end) {
                sub.addAll(curr)
                sorted.removeAt(i)
                continue@top
            }
            ++j
        }
        ++i
    }

    return Pair(sorted, loops)
}
