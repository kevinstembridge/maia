import org.maiaframework.elasticsearch.index.ElasticIndexService
import org.maiaframework.elasticsearch.index.EsIndexNameFactory
import org.maiaframework.elasticsearch.index.EsIndexStateDto
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


@RestController
@RequestMapping($$"${maia.elastic.web.base-url:/api/ops}")
class ElasticSearchIndicesEndpoint(
    private val elasticIndexService: ElasticIndexService,
    private val esIndexNameFactory: EsIndexNameFactory
) {


    @GetMapping("/elastic_indices_state")
    @PreAuthorize("hasAuthority('SYS__OPS')")
    fun getElasticIndicesDefinitions(): List<EsIndexStateDto> {

        return this.elasticIndexService.getIndicesState()

    }


    @PostMapping("/elastic_index/create/{indexName}")
    @PreAuthorize("hasAuthority('SYS__OPS')")
    fun createIndex(@PathVariable("indexName") indexNameRaw: String, principal: Principal) {

        val indexName = this.esIndexNameFactory.indexNameFrom(indexNameRaw)
        this.elasticIndexService.createIndex(indexName, principal)

    }


    @PostMapping("/elastic_index/set_active/{indexName}")
    @PreAuthorize("hasAuthority('SYS__OPS')")
    fun setIndexActiveVersion(@PathVariable indexName: String, principal: Principal) {

        val esIndexName = this.esIndexNameFactory.indexNameFrom(indexName)
        this.elasticIndexService.setIndexActiveVersion(esIndexName, principal)

    }


}
