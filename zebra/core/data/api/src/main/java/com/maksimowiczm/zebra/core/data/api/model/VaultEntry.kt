package com.maksimowiczm.zebra.core.data.api.model

typealias VaultEntryIdentifier = String

data class VaultEntry(
    val identifier: VaultEntryIdentifier,
    val title: String = "<untitled>",
    val username: String? = null,
    val password: (() -> String)? = null,
    val url: String? = null,
)