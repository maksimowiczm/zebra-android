package com.maksimowiczm.feature.share.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.maksimowiczm.feature.share.R
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import kotlinx.coroutines.delay

@Composable
internal fun QrScannerScreen(
    onNavigateUp: () -> Unit,
    onCode: (String) -> Unit,
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var calledOnCode by rememberSaveable { mutableStateOf(false) }
    // Create some breathing space between code scans
    LaunchedEffect(calledOnCode) {
        delay(1_000)
        calledOnCode = false
    }

    QrScannerScreen(
        hasPermissions = hasCameraPermission,
        onPermissionGranted = { hasCameraPermission = true },
        onClose = onNavigateUp,
        onCode = {
            if (!calledOnCode) {
                calledOnCode = true
                onCode(it)
            }
        },
    )
}

@Composable
private fun QrScannerScreen(
    hasPermissions: Boolean,
    onPermissionGranted: () -> Unit,
    onClose: () -> Unit,
    onCode: (String) -> Unit,
) {
    Header(onClose = onClose)
    var initialActive by rememberSaveable { mutableStateOf(false) }

    if (!hasPermissions) {
        RequestCameraPermissionsScreen(onPermissionGranted = {
            initialActive = true
            onPermissionGranted()
        })
    } else {
        CameraView(
            onCode = onCode,
            initialActive = initialActive,
        )
    }

    CameraFooter(onCode = onCode)
}

@Composable
private fun RequestCameraPermissionsScreen(
    onPermissionGranted: () -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        }
    }

    CameraContainer(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) })
}

@Composable
private fun CameraView(
    onCode: (String) -> Unit,
    initialActive: Boolean = false,
) {
    var active by rememberSaveable { mutableStateOf(initialActive) }

    if (active) {
        ActiveCamera(onCode = onCode)
    } else {
        CameraContainer(onClick = { active = true })
    }
}

@Composable
private fun ActiveCamera(
    onCode: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    val barcodeScanner = BarcodeScanning.getClient(options)

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                MlKitAnalyzer(
                    listOf(barcodeScanner),
                    COORDINATE_SYSTEM_VIEW_REFERENCED,
                    ContextCompat.getMainExecutor(context)
                ) { result: MlKitAnalyzer.Result? ->
                    val barcode = result?.getValue(barcodeScanner) ?: return@MlKitAnalyzer
                    if (barcode.size > 0 && barcode[0].rawValue != null) {
                        onCode(barcode[0].rawValue!!)
                    }
                }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                }
            },
        )
    }
}

@Composable
private fun Header(
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .zIndex(1f)
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        FilledIconButton(
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close)
            )
        }
    }
}

@Composable
private fun CameraContainer(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(fraction = .25f),
            painter = painterResource(id = R.drawable.ic_photo_camera),
            contentDescription = stringResource(R.string.request_camera_permission)
        )
    }
}

@Composable
private fun CameraFooter(
    onCode: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            var input by rememberSaveable { mutableStateOf("") }

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = input,
                onValueChange = { input = it },
                label = { Text(stringResource(R.string.session)) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { onCode(input) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send),
                        )
                    }
                },
                keyboardActions = KeyboardActions(onDone = { onCode(input) })
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun QrScannerScreenPreview() {
    ZebraTheme {
        Surface {
            QrScannerScreen(
                onClose = {},
                onPermissionGranted = {},
                onCode = {},
                hasPermissions = false,
            )
        }
    }
}