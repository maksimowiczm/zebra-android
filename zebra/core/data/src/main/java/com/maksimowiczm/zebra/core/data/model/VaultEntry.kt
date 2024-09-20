package com.maksimowiczm.zebra.core.data.model

import com.maksimowiczm.zebra.core.data.proto.VaultEntry as ProtoVaultEntry

typealias VaultEntryIdentifier = String

data class VaultEntry(
    val identifier: VaultEntryIdentifier,
    val title: String = "<untitled>",
    val username: String? = null,
    val password: (() -> String)? = null,
    val url: String? = null,
)

internal fun VaultEntry.toProto(): ProtoVaultEntry {
    val builder = ProtoVaultEntry.newBuilder()

    builder.setTitle(title)

    if (this.username != null) {
        builder.setUsername(username)
    }

    if (this.password != null) {
        builder.setPassword(this.password.invoke())
    }

    if (this.url != null) {
        builder.setUrl(this@toProto.url)
    }

    return builder.build()
}