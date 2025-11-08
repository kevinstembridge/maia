package mahana.gen

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration


@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class],
    scanBasePackages = ["org.maiaframework.dao.mongo", "org.maiaframework.gen"]
)
class MaiaGenTestingMain {


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            SpringApplication.run(MaiaGenTestingMain::class.java, *args)

        }

    }


}
