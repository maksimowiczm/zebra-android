package com.maksimowiczm.zebra.core.data.model

data class VaultEntry(
    val title: String = "<untitled>",
    val username: String? = null,
    val password: (() -> String)? = null,
    val url: String? = null,
)