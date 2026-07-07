package org.maiaframework.gen.spec.definition.flags

enum class SearchModelType {

    AG_GRID,
    MAIA;

    companion object {

        fun default(): SearchModelType {
            return AG_GRID
        }

    }

}
