package com.maksimowiczm.zebra.core.data.model

import com.maksimowiczm.zebra.proto.MessageOuterClass
import com.maksimowiczm.zebra.proto.MessageOuterClass.Message

typealias VaultEntryIdentifier = String

data class VaultEntry(
    val identifier: VaultEntryIdentifier,
    val title: String = "<untitled>",
    val username: String? = null,
    val password: (() -> String)? = null,
    val url: String? = null,
)

internal val VaultEntry.protoTitle: Message
    get() = Message.newBuilder()
        .setDescription("title")
        .setContent(title)
        .setType(MessageOuterClass.MessageType.MESSAGE_TYPE_PUBLIC)
        .build()

internal val VaultEntry.protoUsername: Message?
    get() = username?.let {
        Message.newBuilder()
            .setDescription("username")
            .setContent(it)
            .setType(MessageOuterClass.MessageType.MESSAGE_TYPE_PUBLIC)
            .build()
    }

internal val VaultEntry.protoPassword: Message?
    get() = password?.let {
        Message.newBuilder()
            .setDescription("password")
            .setContent(it.invoke())
            .setType(MessageOuterClass.MessageType.MESSAGE_TYPE_PRIVATE)
            .build()
    }

internal val VaultEntry.protoUrl: Message?
    get() = url?.let {
        Message.newBuilder()
            .setDescription("url")
            .setContent(it)
            .setType(MessageOuterClass.MessageType.MESSAGE_TYPE_PUBLIC)
            .build()
    }