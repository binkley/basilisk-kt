package hm.binkley.basilisk.location

import javax.validation.constraints.NotNull

data class LocationResource(
        @NotNull override val code: String,
        @NotNull override val name: String)
    : LocationDetails {
    constructor(location: LocationDetails)
            : this(location.code, location.name)
}
