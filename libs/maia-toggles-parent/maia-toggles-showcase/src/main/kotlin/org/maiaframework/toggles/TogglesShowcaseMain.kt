package org.maiaframework.toggles

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching


@SpringBootApplication(
    scanBasePackages = [
        "org.maiaframework.toggles",
        "org.maiaframework.hazelcast",
    ],
    exclude = [
        ErrorMvcAutoConfiguration::class
    ]
)
@EnableCaching
class TogglesShowcaseMain


fun main() {


    runApplication<TogglesShowcaseMain> {
        addListeners(ApplicationPidFileWriter())
    }


}




