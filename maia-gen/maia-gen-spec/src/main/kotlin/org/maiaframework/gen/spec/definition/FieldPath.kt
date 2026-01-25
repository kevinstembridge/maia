package org.maiaframework.gen.spec.definition

class FieldPath private constructor(
    private val segments: List<String>
) {

    val length: Int = segments.size


    fun head(): String {
        return this.segments.first()
    }


    fun isJustOneField(): Boolean {
        return this.segments.size == 1

    }


    fun tail(): FieldPath {
        return FieldPath(this.segments.drop(1))
    }


    override fun toString(): String {
        return "FieldPath[${this.segments}]"
    }


    companion object {

        fun of(path: String): FieldPath {
            return FieldPath(path.split("."))
        }

    }


}
