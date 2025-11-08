package org.maiaframework.gen.spec.definition

enum class SearchModelType {

    AG_GRID,
    MAIA;

    companion object {

        fun default(): SearchModelType {
            return AG_GRID
        }

    }

}
