package org.maiaframework.toggles

class ToggleBootstrap(
    toggleRegistry: ToggleRegistry,
    toggleSyncer: ToggleSyncer
) {


    init {

        toggleRegistry.featuresByName.values.forEach(toggleSyncer::sync)

    }


}
