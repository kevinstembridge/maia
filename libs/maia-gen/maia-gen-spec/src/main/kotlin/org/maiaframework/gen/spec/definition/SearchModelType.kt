package org.maiaframework.gen.spec.definition

enum class SearchModelType {

    AG_GRID,
    MAHANA;

    companion object {

        fun default(): SearchModelType {
            return AG_GRID
        }

    }

}
