package com.maksimowiczm.zebra.core.data.model

enum class FeatureFlag(
    val title: String,
    val defaultIsEnabled: Boolean,
) {
    FEATURE_SEND(
        title = "FEATURE_SEND",
        defaultIsEnabled = false,
    )
}

val FEATURE_FLAGS = listOf(
    FeatureFlag.FEATURE_SEND,
)