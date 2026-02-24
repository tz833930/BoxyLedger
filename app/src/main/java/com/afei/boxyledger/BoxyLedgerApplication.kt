package com.afei.boxyledger

import android.app.Application
import com.afei.boxyledger.data.AppContainer
import com.afei.boxyledger.data.DefaultAppContainer

class BoxyLedgerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
