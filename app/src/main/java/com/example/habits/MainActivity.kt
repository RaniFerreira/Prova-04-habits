package com.example.habits

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.habits.ui.navigation.HabitsApp
import com.example.habits.ui.theme.HabitsTheme

/** FragmentActivity (em vez de ComponentActivity) porque o BiometricPrompt exige. */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
        )
        val container = (application as HabitsApplication).container
        setContent {
            HabitsTheme {
                HabitsApp(container)
            }
        }
    }
}
