package com.lintang.sarprasq.util.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manajer terpusat untuk verifikasi inisialisasi, pemeriksaan konfigurasi google-services.json,
 * serta akses aman instance Firebase (Firestore dan Realtime Database) di aplikasi SarprasQ.
 */
object FirebaseManager {

    private const val TAG = "FirebaseManager"
    const val RTDB_URL = "https://sarpras-134d7-default-rtdb.asia-southeast1.firebasedatabase.app"
    const val EXPECTED_PROJECT_ID = "sarpras-134d7"
    const val EXPECTED_PACKAGE_NAME = "com.lintang.sarprasq"

    @Volatile
    private var isInitialized = false

    private var cachedFirestore: FirebaseFirestore? = null
    private var cachedDatabase: FirebaseDatabase? = null

    /**
     * Memverifikasi dan menginisialisasi instance FirebaseApp serta mengatur konfigurasi
     * offline persistence untuk Firestore dan Realtime Database.
     */
    @Synchronized
    fun initialize(context: Context): Boolean {
        if (isInitialized) {
            Log.d(TAG, "[INIT] FirebaseManager sudah terinisialisasi sebelumnya.")
            return true
        }

        try {
            val appContext = context.applicationContext
            val app = if (FirebaseApp.getApps(appContext).isEmpty()) {
                val createdApp = FirebaseApp.initializeApp(appContext)
                Log.i(TAG, "[INIT SUCCESS] FirebaseApp berhasil diinisialisasi dari google-services.json. Name: ${createdApp?.name}")
                createdApp
            } else {
                val existingApp = FirebaseApp.getInstance()
                Log.i(TAG, "[INIT INFO] FirebaseApp sudah aktif: ${existingApp.name}")
                existingApp
            }

            if (app != null) {
                logAppConfiguration(app.options)
            }

            // Inisialisasi Realtime Database untuk region asia-southeast1
            initRealtimeDatabase()

            // Inisialisasi Firestore dengan setelan offline persistence
            initFirestore()

            isInitialized = true
            return true
        } catch (e: Exception) {
            Log.e(TAG, "[INIT ERROR] Gagal menginisialisasi Firebase: ${e.message}", e)
            return false
        }
    }

    private fun logAppConfiguration(options: FirebaseOptions) {
        Log.d(TAG, "=== VERIFIKASI KONFIGURASI FIREBASE ===")
        Log.d(TAG, "Application ID : ${options.applicationId}")
        Log.d(TAG, "Project ID     : ${options.projectId}")
        Log.d(TAG, "Database URL   : ${options.databaseUrl ?: RTDB_URL}")
        Log.d(TAG, "Storage Bucket : ${options.storageBucket}")
        Log.d(TAG, "========================================")

        if (options.projectId != EXPECTED_PROJECT_ID) {
            Log.w(TAG, "[CONFIG WARN] Project ID berbeda dari target: ditemukan '${options.projectId}', diharapkan '$EXPECTED_PROJECT_ID'")
        }
    }

    private fun initRealtimeDatabase() {
        try {
            val rtdb = FirebaseDatabase.getInstance(RTDB_URL)
            try {
                rtdb.setPersistenceEnabled(true)
                Log.d(TAG, "[RTDB] Offline persistence diaktifkan untuk $RTDB_URL")
            } catch (e: Exception) {
                // Persistence hanya dapat disetel sekali sebelum pemanggilan getReference pertama
                Log.d(TAG, "[RTDB] Catatan setPersistenceEnabled: ${e.message}")
            }
            cachedDatabase = rtdb
        } catch (e: Exception) {
            Log.e(TAG, "[RTDB INIT ERROR] Gagal inisialisasi FirebaseDatabase ($RTDB_URL): ${e.message}", e)
        }
    }

    private fun initFirestore() {
        try {
            val fs = FirebaseFirestore.getInstance()
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                fs.firestoreSettings = settings
                Log.d(TAG, "[FIRESTORE] Offline persistence diaktifkan")
            } catch (e: Exception) {
                Log.d(TAG, "[FIRESTORE] Catatan setPersistenceEnabled: ${e.message}")
            }
            cachedFirestore = fs
        } catch (e: Throwable) {
            Log.e(TAG, "[FIRESTORE INIT ERROR] Gagal inisialisasi FirebaseFirestore: ${e.message}", e)
        }
    }

    /**
     * Mengambil instance FirebaseFirestore yang aman dan teruji.
     */
    fun getFirestore(): FirebaseFirestore? {
        return try {
            cachedFirestore ?: FirebaseFirestore.getInstance().also { cachedFirestore = it }
        } catch (e: Throwable) {
            Log.e(TAG, "[FIRESTORE RESOLVE ERROR] Instance Firestore tidak tersedia: ${e.message}", e)
            null
        }
    }

    /**
     * Mengambil instance FirebaseDatabase region Asia-Southeast1.
     */
    fun getRealtimeDatabase(): FirebaseDatabase? {
        return try {
            cachedDatabase ?: FirebaseDatabase.getInstance(RTDB_URL).also { cachedDatabase = it }
        } catch (e: Exception) {
            Log.e(TAG, "[RTDB RESOLVE ERROR] Instance RealtimeDatabase tidak tersedia: ${e.message}", e)
            null
        }
    }

    /**
     * Data class hasil verifikasi status kesehatan dan jalur Firebase.
     */
    data class HealthStatus(
        val isAppInitialized: Boolean,
        val projectId: String,
        val isFirestoreReady: Boolean,
        val isRealtimeDbReady: Boolean,
        val rtdbUrl: String,
        val checkedPaths: Map<String, Boolean> = emptyMap(),
        val message: String = ""
    )

    /**
     * Melakukan pemeriksaan jalur (path checking) komprehensif pada Firestore dan Realtime Database.
     */
    suspend fun checkPathsHealth(): HealthStatus = withContext(Dispatchers.IO) {
        val fs = getFirestore()
        val rtdb = getRealtimeDatabase()
        val pathResults = mutableMapOf<String, Boolean>()

        // Periksa jalur Firestore
        val firestoreCollections = listOf(
            FirebaseFirestoreHelper.COLLECTION_SURAT_SARPRAS,
            FirebaseFirestoreHelper.COLLECTION_RENCANA_AKSI,
            FirebaseFirestoreHelper.COLLECTION_SURAT_ARSIP_COMPAT,
            FirebaseFirestoreHelper.COLLECTION_ACTION_PLANS_COMPAT
        )

        for (col in firestoreCollections) {
            try {
                if (fs != null) {
                    val colRef = fs.collection(col)
                    pathResults["firestore:$col"] = colRef.path.isNotEmpty()
                    Log.d(TAG, "[PATH CHECK SUCCESS] Firestore collection '$col' path verified: ${colRef.path}")
                } else {
                    pathResults["firestore:$col"] = false
                    Log.w(TAG, "[PATH CHECK WARN] Firestore instance null untuk collection '$col'")
                }
            } catch (e: Exception) {
                pathResults["firestore:$col"] = false
                Log.e(TAG, "[PATH CHECK ERROR] Firestore collection '$col' error: ${e.message}")
            }
        }

        // Periksa jalur Realtime Database
        val rtdbPaths = listOf(
            FirebaseRealtimeHelper.PATH_SURAT_ARSIP,
            FirebaseRealtimeHelper.PATH_ACTION_PLANS,
            FirebaseRealtimeHelper.PATH_ROOT
        )

        for (p in rtdbPaths) {
            try {
                if (rtdb != null) {
                    val nodeRef = rtdb.getReference(p)
                    pathResults["rtdb:$p"] = nodeRef.key != null
                    Log.d(TAG, "[PATH CHECK SUCCESS] RTDB path '$p' verified with key: ${nodeRef.key}")
                } else {
                    pathResults["rtdb:$p"] = false
                    Log.w(TAG, "[PATH CHECK WARN] RTDB instance null untuk path '$p'")
                }
            } catch (e: Exception) {
                pathResults["rtdb:$p"] = false
                Log.e(TAG, "[PATH CHECK ERROR] RTDB path '$p' error: ${e.message}")
            }
        }

        val allReady = (fs != null) && (rtdb != null) && pathResults.values.all { it }
        val msg = if (allReady) {
            "Semua jalur Firestore dan Realtime Database terverifikasi valid dan siap disinkronkan."
        } else {
            "Sebagian komponen atau jalur Firebase belum siap."
        }

        HealthStatus(
            isAppInitialized = isInitialized,
            projectId = EXPECTED_PROJECT_ID,
            isFirestoreReady = fs != null,
            isRealtimeDbReady = rtdb != null,
            rtdbUrl = RTDB_URL,
            checkedPaths = pathResults,
            message = msg
        )
    }
}
