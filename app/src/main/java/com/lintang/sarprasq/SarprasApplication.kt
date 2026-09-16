package com.lintang.sarprasq

import android.app.Application
import android.util.Log
import com.lintang.sarprasq.util.firebase.FirebaseManager
import com.lintang.sarprasq.util.firebase.FirebaseRealtimeHelper

class SarprasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            val initialized = FirebaseManager.initialize(this)
            if (initialized) {
                FirebaseRealtimeHelper.startConnectionMonitoring()
                Log.i("SarprasApplication", "FirebaseManager dan Realtime Connection Monitor aktif.")
            }
        } catch (e: Exception) {
            Log.e("SarprasApplication", "FirebaseApp initialization error: ${e.message}", e)
        }
    }
}
