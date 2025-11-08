package org.maiaframework.elasticsearch

import org.maiaframework.types.StringType


class SearchTerm(value: String): StringType<SearchTerm>(value.trim())
