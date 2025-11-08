package org.maiaframework.common.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class FeatureKey(val value: String)
