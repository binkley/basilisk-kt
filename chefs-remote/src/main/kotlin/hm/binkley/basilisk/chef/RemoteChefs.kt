package hm.binkley.basilisk.chef

import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.inject.Singleton
import javax.validation.constraints.NotNull

@Singleton
class RemoteChefs(private val client: ChefsOperations)
    : Chefs {
    override fun all() = client.all().map {
        from(it)
    }

    override fun byCode(code: String) = client.byCode(code)?.let {
        from(it)
    }

    override fun new(chef: ChefResource) = client.new(chef).body()!!.let {
        from(it)
    }

    fun update(resource: ChefResource) {
        client.update(resource)
    }

    fun delete(resource: ChefResource) {
        client.delete(resource)
    }

    private fun from(resource: ChefResource) = RemoteChef(resource, this)
}

class RemoteChef internal constructor(
        internal val resource: ChefResource,
        private val factory: RemoteChefs)
    : Chef,
        ChefDetails by resource {
    override fun update(block: MutableChef.() -> Unit) =
            update(ChefResource(this), block)

    internal inline fun update(
            snapshot: ChefResource?,
            block: MutableChef.() -> Unit) = apply {
        RemoteMutableChef(snapshot, MutableChefResource(resource),
                factory).block()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RemoteChef
        return resource == other.resource
    }

    override fun hashCode() = resource.hashCode()

    override fun toString() = "${super.toString()}{resource=$resource}"
}

@Introspected
data class MutableChefResource(
        @NotNull override var name: String,
        @NotNull override var code: String,
        @NotNull override var health: String) : MutableChefDetails {
    constructor(chef: ChefResource) : this(chef.name, chef.code, chef.health)

    fun freeze() = ChefResource(name, code, health)
}

class RemoteMutableChef internal constructor(
        private val snapshot: ChefResource?,
        private val resource: MutableChefResource,
        private val factory: RemoteChefs)
    : MutableChef,
        MutableChefDetails {
    override var name: String
        get() = resource.name
        set(update) {
            resource.name = update
        }
    override var code: String
        get() = resource.code
        set(update) {
            resource.code = update
        }
    override var health: String
        get() = resource.health
        set(update) {
            resource.health = update
        }

    override fun save() = apply {
        val frozen = resource.freeze()
        factory.update(frozen)
    }

    override fun delete() {
        factory.delete(resource.freeze())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RemoteMutableChef
        return snapshot == other.snapshot
                && resource == other.resource
    }

    override fun hashCode() = Objects.hash(snapshot, resource)

    override fun toString() =
            "${super.toString()}{snapshot=$snapshot, resource=$resource}"
}
