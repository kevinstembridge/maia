package org.maiaframework.testing.postgresql

import org.testcontainers.containers.PostgreSQLContainer

class SingletonPostgresqlContainer(
    dockerImageName: String
): PostgreSQLContainer<SingletonPostgresqlContainer>(
    dockerImageName
) {


    override fun stop() {
        // do nothing
    }


    companion object {

        val instance = SingletonPostgresqlContainer("postgres:16")

    }


}
