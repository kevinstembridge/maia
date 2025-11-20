package org.maiaframework.toggles

import com.fasterxml.jackson.annotation.JsonValue

@JvmInline
value class FeatureName(@JsonValue val value: String)
