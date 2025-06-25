package com.onthecrow.sharegram.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.onthecrow.sharegram.ui.instagram.InstagramWebView
import com.onthecrow.sharegram.ui.theme.SharegramTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SharegramTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InstagramWebView(
                        url = "https://www.instagram.com/",
                        onPageLoaded = {
                            Toast.makeText(this, "Page loaded", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                    )
                }
            }
        }
    }
}
