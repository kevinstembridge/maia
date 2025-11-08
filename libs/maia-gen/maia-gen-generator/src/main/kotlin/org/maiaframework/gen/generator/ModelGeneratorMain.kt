package org.maiaframework.gen.generator

object ModelGeneratorMain {


    @JvmStatic
    fun main(args: Array<String>) {

        try {

            val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

            moduleGeneratorFixture.modelDefs.forEach {

                val modelGenerator = ModelGenerator(it, moduleGeneratorFixture.modelGeneratorContext)
                modelGenerator.generateSource()

            }

        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

    }


}
