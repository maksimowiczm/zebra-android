package com.maksimowiczm.zebra.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.maksimowiczm.zebra.common_ui.theme.ZebraTheme

@Composable
fun ZebraApp() {
    ZebraTheme {
        Scaffold { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                Text(
                    text = "Hello Zebra!"
                )
            }
        }
    }
}