package org.maiaframework.gen.spec.definition


/**
 * When a Hazelcast-backed Blotter's map is filled from the database (ADR-0008). The global default comes from the
 * property `maia.hazelcast-blotter.population`; a [HazelcastSpec.population] on a Searchable DTO overrides it.
 *
 * @property propertyValue the value used for this mode in the property
 */
enum class HazelcastPopulation(val propertyValue: String) {

    /** The map is filled when the application starts. This is the default. */
    EAGER("eager"),

    /** The map is filled by the first fetch after start. */
    LAZY("lazy")

}
