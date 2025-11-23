package org.maiaframework.toggles

class ToggleBootstrap(
    toggleRegistry: ToggleRegistry,
    toggleSyncer: ToggleSyncer
) {


    init {

        toggleRegistry.features.forEach(toggleSyncer::sync)

    }


}
