package org.maiaframework.mail

data class EmailTemplateContext(
        val location: EmailTemplateLocation,
        val context: Map<String, Any> = emptyMap()
)
