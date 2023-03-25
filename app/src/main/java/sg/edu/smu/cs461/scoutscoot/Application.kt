package sg.edu.smu.cs461.scoutscoot

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


const val DATABASE_URL = "https://scoutscoot-91367-default-rtdb.asia-southeast1.firebasedatabase.app/"

class MyApplication : Application(), CameraXConfig.Provider {

    override fun onCreate() {
        super.onCreate()
        Firebase.database(DATABASE_URL).setPersistenceEnabled(true)
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}