package com.afei.boxyledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.afei.boxyledger.ui.BoxyLedgerApp
import com.afei.boxyledger.ui.theme.BoxyLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoxyLedgerTheme {
                BoxyLedgerApp()
            }
        }
    }
}
