package com.maksimowiczm.zebra.core.common_ui.composable

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class BooleanParameterPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(
        true,
        false
    )
}