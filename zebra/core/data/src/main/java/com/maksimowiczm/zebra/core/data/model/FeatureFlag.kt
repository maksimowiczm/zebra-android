package com.maksimowiczm.zebra.core.data.model

enum class FeatureFlag(
    val title: String,
    val defaultIsEnabled: Boolean,
) {
    FEATURE_SHARE(
        title = "FEATURE_SHARE",
        defaultIsEnabled = false,
    )
}

val FEATURE_FLAGS = listOf(
    FeatureFlag.FEATURE_SHARE,
)