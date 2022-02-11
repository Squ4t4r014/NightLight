package io.github.squ4t4r014.nightlight

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

class NightLight : Application(), CameraXConfig.Provider {
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}