package com.maksimowiczm.zebra.core.data.utility

import com.maksimowiczm.zebra.core.data.api.model.VaultEntry
import com.maksimowiczm.zebra.proto.MessageOuterClass
import com.maksimowiczm.zebra.proto.MessageOuterClass.Message

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