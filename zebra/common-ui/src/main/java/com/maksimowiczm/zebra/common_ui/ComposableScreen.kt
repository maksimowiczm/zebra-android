package com.maksimowiczm.zebra.common_ui

interface ComposableScreen {
    fun toDestination() = this::class.qualifiedName.toString()
}