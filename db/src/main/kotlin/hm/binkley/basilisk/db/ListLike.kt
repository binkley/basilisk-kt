package hm.binkley.basilisk.db

import org.jetbrains.exposed.sql.SizedIterable

fun <T> SizedIterable<T>.asList(updateWith: (ListLike<T>) -> Unit = { }) =
        ListLike<T>(this, updateWith)

// TODO: There should be a nicer way
class ListLike<T>(
        field: SizedIterable<T>,
        private val updateWith: (ListLike<T>) -> Unit = { })
    : AbstractMutableList<T>() {
    private val backing = field.toMutableList()

    private fun update() = updateWith(this)

    override val size: Int
        get() = backing.size

    override fun add(index: Int, element: T) {
        backing.add(index, element)
        update()
    }

    override fun get(index: Int) = backing.get(index)

    override fun removeAt(index: Int): T {
        val removeAt = backing.removeAt(index)
        update()
        return removeAt
    }

    override fun set(index: Int, element: T): T {
        val set = backing.set(index, element)
        update()
        return set
    }
}
