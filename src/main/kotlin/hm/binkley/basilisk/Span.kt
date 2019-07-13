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

        override fun equals(other: Any?) = l == other
        override fun hashCode() = l.hashCode()
        override fun toString() = l.toString()
    }

    val toSort = unsorted.map { SpanList(it) }.toMutableList()
    when (toSort.size) {
        0, 1 -> return unsorted
    }

    var iters = 0
    var i = 0
    top@ while (i < toSort.size) {
        ++iters
        val curr = toSort[i]
        var j = 0
        while (j < toSort.size) {
            ++iters
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

//    println("iters: ${iters}")
    return toSort[0]
}
