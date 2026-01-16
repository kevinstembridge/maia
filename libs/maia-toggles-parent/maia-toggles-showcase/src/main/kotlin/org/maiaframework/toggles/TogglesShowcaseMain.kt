package org.maiaframework.toggles

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.runApplication
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration


@SpringBootApplication(
    scanBasePackages = [
        "org.maiaframework.toggles",
        "org.maiaframework.hazelcast",
    ],
    exclude = [
        ErrorMvcAutoConfiguration::class
    ]
)
class TogglesShowcaseMain


fun main() {


    runApplication<TogglesShowcaseMain> {
        addListeners(ApplicationPidFileWriter())
    }


}




