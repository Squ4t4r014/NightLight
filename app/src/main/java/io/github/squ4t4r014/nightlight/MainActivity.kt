package io.github.squ4t4r014.nightlight

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.github.squ4t4r014.nightlight.ui.theme.NightLightTheme
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface CameraInitialize {
    fun onCameraInitialized(camera: Camera)
}

class MainActivity : ComponentActivity(), CameraInitialize {

    private var camera: Camera? = null
    private var cameraManager: CameraManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NightLightTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CameraView(listener = this)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        var isTorchActive by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                this@MainActivity.camera?.cameraControl?.enableTorch(!isTorchActive)
                                isTorchActive = !isTorchActive
                            }
                        ) {
                            Text(text = if (!isTorchActive) "On" else "Off")
                        }
                    }
                }
            }
        }

        /*this.cameraManager = (applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager).also {
            it.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    super.onTorchModeChanged(cameraId, enabled)
                }
            }, Handler(Looper.getMainLooper()))
        }*/
    }

    override fun onCameraInitialized(camera: Camera) {
        this.camera = camera
    }
}

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    listener: CameraInitialize? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val previewUseCase = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            coroutineScope.launch {
                val cameraProvider = context.getCameraProvider()
                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase)
                    listener?.onCameraInitialized(camera)
                    //camera.cameraControl.enableTorch(true)
                } catch (e: Exception) {
                    Log.e("ShowingCameraView", "Failed binding use case", e)

                }
            }

            previewView
        }
    )
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { listenableFuture ->
        listenableFuture.addListener({
            continuation.resume(listenableFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NightLightTheme {
        CameraView()
    }
}