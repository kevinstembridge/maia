package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.singleKotlinFileIn
import java.io.File


class EsIndexMetaClassRendererTest {


    private val spec = EsRefreshListenerTestSpec()


    @Test
    fun `object mapping escapes the Kotlin keyword in the property builder call`(@TempDir dir: File) {

        EsIndexMetaClassRenderer(spec.leftDto.esDocDefs.single()).renderToDir(dir)
        val source = singleKotlinFileIn(dir)

        assertThat(source).contains("m.properties(\"rights\", Property.of { p -> p.`object` { it } })")
        assertThat(source).doesNotContain("p.object {")

    }


    @Test
    fun `non keyword mapping types are rendered unescaped`(@TempDir dir: File) {

        EsIndexMetaClassRenderer(spec.leftDto.esDocDefs.single()).renderToDir(dir)
        val source = singleKotlinFileIn(dir)

        assertThat(source).contains("m.properties(\"id\", Property.of { p -> p.keyword { it } })")

    }


}
