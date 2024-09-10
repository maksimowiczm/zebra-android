package com.maksimowiczm.zebra.core.common_ui

interface ComposableScreen {
    fun toDestination() = this::class.qualifiedName.toString()
}