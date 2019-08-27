package hm.binkley.basilisk.location

import javax.validation.constraints.NotNull

data class LocationResource(
        @NotNull override val name: String,
        @NotNull override val code: String)
    : LocationDetails {
    constructor(location: PersistedLocation)
            : this(location.name, location.code)
}
