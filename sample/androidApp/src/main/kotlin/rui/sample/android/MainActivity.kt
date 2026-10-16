package rui.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import rui.sample.CatalogApp
import rui.sample.SampleSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SampleSettings.init(applicationContext)
        setContent {
            CatalogApp()
        }
    }
}
