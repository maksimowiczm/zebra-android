package com.maksimowiczm.zebra.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

inline fun <T1, T2, R> combineN(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    crossinline transform: suspend (T1, T2) -> R,
): Flow<R> {
    return combine(
        flow,
        flow2,
    ) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
        )
    }
}

inline fun <T1, T2, T3, R> combineN(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    crossinline transform: suspend (T1, T2, T3) -> R,
): Flow<R> {
    return combine(
        flow,
        flow2,
        flow3,
    ) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
        )
    }
}

inline fun <T1, T2, T3, T4, R> combineN(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    crossinline transform: suspend (T1, T2, T3, T4) -> R,
): Flow<R> {
    return combine(
        flow,
        flow2,
        flow3,
        flow4,
    ) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
        )
    }
}

inline fun <T1, T2, T3, T4, T5, R> combineN(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    crossinline transform: suspend (T1, T2, T3, T4, T5) -> R,
): Flow<R> {
    return combine(
        flow,
        flow2,
        flow3,
        flow4,
        flow5,
    ) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
        )
    }
}