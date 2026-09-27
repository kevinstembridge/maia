package org.maiaframework.elasticsearch

import org.maiaframework.elasticsearch.index.ElasticIndexService
import org.maiaframework.elasticsearch.index.model.EsIndexBaseName
import org.maiaframework.elasticsearch.index.model.EsIndexStateResponseDto
import org.maiaframework.elasticsearch.index.model.EsIndexVersion
import org.maiaframework.elasticsearch.index.model.IndexBaseNameAndVersion
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


@RestController
@RequestMapping($$"${maia.elasticsearch.web.base-url:/api/ops}")
class ElasticSearchIndicesEndpoint(
    private val elasticIndexService: ElasticIndexService
) {


    @GetMapping("/elastic_indices_state")
    @PreAuthorize("hasAuthority('${EsConstants.Authority.MAIA_ELASTICSEARCH_SYS_OPS_READ}')")
    fun getElasticIndicesState(): List<EsIndexStateResponseDto> {

        return this.elasticIndexService.getIndicesState()

    }


    @PostMapping("/elastic_index/create/{indexBaseName}/{indexVersion}")
    @PreAuthorize("hasAuthority('${EsConstants.Authority.MAIA_ELASTICSEARCH_SYS_OPS_WRITE}')")
    fun createIndex(
        @PathVariable indexBaseName: String,
        @PathVariable indexVersion: Int,
        principal: Principal
    ) {

        val indexBaseNameAndVersion = IndexBaseNameAndVersion(EsIndexBaseName(indexBaseName), EsIndexVersion(indexVersion))
        this.elasticIndexService.createIndex(indexBaseNameAndVersion, principal)

    }


    @PostMapping("/elastic_index/set_active/{indexBaseName}/{indexVersion}")
    @PreAuthorize("hasAuthority('${EsConstants.Authority.MAIA_ELASTICSEARCH_SYS_OPS_WRITE}')")
    fun setIndexActiveVersion(
        @PathVariable indexBaseName: String,
        @PathVariable indexVersion: Int,
        principal: Principal
    ) {

        this.elasticIndexService.setIndexActiveVersion(
            EsIndexBaseName(indexBaseName),
            EsIndexVersion(indexVersion),
            principal
        )

    }


}
