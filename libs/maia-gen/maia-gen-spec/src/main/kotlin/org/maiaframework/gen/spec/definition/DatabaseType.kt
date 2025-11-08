package org.maiaframework.gen.spec.definition

enum class DatabaseType {

    JDBC,
    MONGO;

    companion object {

        fun default(): DatabaseType {
            return MONGO
        }

    }

}
