package com.afei.boxyledger.data

import android.content.Context
import com.afei.boxyledger.data.local.AppDatabase

interface AppContainer {
    val database: AppDatabase
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
}
