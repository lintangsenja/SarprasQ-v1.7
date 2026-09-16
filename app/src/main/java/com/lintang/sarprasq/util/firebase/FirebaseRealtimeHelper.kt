package com.lintang.sarprasq.util.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.SuratArsip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper khusus Firebase Realtime Database:
 * 1. Pengecekan status koneksi live menggunakan listener node khusus ".info/connected".
 * 2. Operasi sync / CRUD Realtime Database dengan pemeriksaan jalur (path checking).
 * 3. Logcat logging mendalam (Log.d / Log.e) untuk kemudahan penelusuran masalah (debugging).
 */
object FirebaseRealtimeHelper {

    private const val TAG = "RealtimeDbHelper"

    const val PATH_ROOT = "sarprasq"
    const val PATH_SURAT_ARSIP = "sarprasq/surat_arsip"
    const val PATH_ACTION_PLANS = "sarprasq/action_plans"
    const val PATH_INFO_CONNECTED = ".info/connected"
    const val PATH_DIAGNOSTIC = "sarprasq/_connection_diagnostic"

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var connectedListener: ValueEventListener? = null
    private var connectedRef: DatabaseReference? = null

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) continuation.resumeWithException(exception)
        }
    }

    /**
     * Memulai pemantauan status koneksi server Firebase secara real-time via ".info/connected".
     * Node ini otomatis disediakan oleh Firebase SDK untuk mendeteksi apakah klien terhubung ke socket server.
     */
    fun startConnectionMonitoring() {
        if (connectedListener != null) return

        try {
            val rtdb = FirebaseManager.getRealtimeDatabase() ?: return
            connectedRef = rtdb.getReference(PATH_INFO_CONNECTED)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    _isConnected.value = connected
                    if (connected) {
                        Log.i(TAG, "[RTDB CONNECTED] Terhubung langsung ke socket Firebase Realtime Database (Online)")
                    } else {
                        Log.w(TAG, "[RTDB DISCONNECTED] Terputus dari socket Firebase Realtime Database (Offline/Reconnecting)")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _isConnected.value = false
                    Log.w(TAG, "[RTDB LISTENER CANCELLED] Listener .info/connected dibatalkan: ${error.message} (Code: ${error.code})")
                }
            }

            connectedListener = listener
            connectedRef?.addValueEventListener(listener)
            Log.d(TAG, "[RTDB] Listener pemantauan koneksi .info/connected aktif")
        } catch (e: Exception) {
            Log.e(TAG, "[RTDB MONITOR ERROR] Gagal memulai listener koneksi: ${e.message}", e)
        }
    }

    /**
     * Menghentikan listener pemantauan koneksi saat tidak lagi dibutuhkan.
     */
    fun stopConnectionMonitoring() {
        connectedListener?.let { listener ->
            connectedRef?.removeEventListener(listener)
            connectedListener = null
            connectedRef = null
            Log.d(TAG, "[RTDB] Listener pemantauan koneksi .info/connected dihentikan")
        }
    }

    /**
     * Melakukan uji ping dan ukur latensi server secara aktif.
     */
    suspend fun pingServer(): Result<Long> = withContext(Dispatchers.IO) {
        val rtdb = FirebaseManager.getRealtimeDatabase() ?: return@withContext Result.failure(
            IllegalStateException("FirebaseDatabase instance tidak tersedia")
        )

        val startTime = System.currentTimeMillis()
        val diagRef = rtdb.getReference(PATH_DIAGNOSTIC)

        Log.d(TAG, "[RTDB PING START] Mengirim uji ping ke path: $PATH_DIAGNOSTIC")
        return@withContext try {
            diagRef.setValue(ServerValue.TIMESTAMP).awaitTask()
            val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(10L)
            Log.d(TAG, "[RTDB PING SUCCESS] Server merespons dalam $latency ms")
            Result.success(latency)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ping failure"
            Log.e(TAG, "[RTDB PING ERROR] Gagal melakukan ping: $msg", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // CRUD: ARSIP SURAT (sarprasq/surat_arsip/{id})
    // =========================================================================

    /**
     * Menyimpan Arsip Surat ke Realtime Database pada path 'sarprasq/surat_arsip/{id}'.
     */
    suspend fun saveSuratArsip(
        surat: SuratArsip,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val rtdb = FirebaseManager.getRealtimeDatabase()
        if (rtdb == null) {
            val err = "Realtime Database belum diinisialisasi"
            Log.e(TAG, "[RTDB WRITE ERROR] $err")
            onComplete?.invoke(false, err)
            return@withContext false
        }

        val path = "$PATH_SURAT_ARSIP/${surat.id}"
        val payload = FirebaseFirestoreHelper.suratToMap(surat)

        Log.d(TAG, "[RTDB WRITE START] Menyimpan surat #${surat.id} ke path: $path")
        return@withContext try {
            rtdb.getReference(path).setValue(payload).awaitTask()
            Log.d(TAG, "[RTDB WRITE SUCCESS] Surat #${surat.id} tersimpan di $path")
            onComplete?.invoke(true, null)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Unknown RTDB Error"
            Log.e(TAG, "[RTDB WRITE ERROR] Gagal menyimpan surat #${surat.id} ke $path: $msg", e)
            onComplete?.invoke(false, msg)
            false
        }
    }

    /**
     * Menghapus Arsip Surat dari Realtime Database.
     */
    suspend fun deleteSuratArsip(
        suratId: Int,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val rtdb = FirebaseManager.getRealtimeDatabase() ?: return@withContext false
        val path = "$PATH_SURAT_ARSIP/$suratId"

        Log.d(TAG, "[RTDB DELETE START] Menghapus surat #$suratId dari $path")
        return@withContext try {
            rtdb.getReference(path).removeValue().awaitTask()
            Log.d(TAG, "[RTDB DELETE SUCCESS] Surat #$suratId dihapus dari $path")
            onComplete?.invoke(true, null)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Unknown Error"
            Log.e(TAG, "[RTDB DELETE ERROR] Gagal menghapus surat #$suratId: $msg", e)
            onComplete?.invoke(false, msg)
            false
        }
    }

    // =========================================================================
    // CRUD: RENCANA AKSI (sarprasq/action_plans/{id})
    // =========================================================================

    /**
     * Menyimpan Rencana Aksi ke Realtime Database pada path 'sarprasq/action_plans/{id}'.
     */
    suspend fun saveActionPlan(
        plan: ActionPlan,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val rtdb = FirebaseManager.getRealtimeDatabase()
        if (rtdb == null) {
            val err = "Realtime Database belum diinisialisasi"
            Log.e(TAG, "[RTDB WRITE ERROR] $err")
            onComplete?.invoke(false, err)
            return@withContext false
        }

        val path = "$PATH_ACTION_PLANS/${plan.id}"
        val payload = FirebaseFirestoreHelper.actionPlanToMap(plan)

        Log.d(TAG, "[RTDB WRITE START] Menyimpan rencana aksi #${plan.id} (${plan.agenda}) ke path: $path")
        return@withContext try {
            rtdb.getReference(path).setValue(payload).awaitTask()
            Log.d(TAG, "[RTDB WRITE SUCCESS] Rencana aksi #${plan.id} tersimpan di $path")
            onComplete?.invoke(true, null)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Unknown RTDB Error"
            Log.e(TAG, "[RTDB WRITE ERROR] Gagal menyimpan rencana aksi #${plan.id} ke $path: $msg", e)
            onComplete?.invoke(false, msg)
            false
        }
    }

    /**
     * Menghapus Rencana Aksi dari Realtime Database.
     */
    suspend fun deleteActionPlan(
        planId: Int,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val rtdb = FirebaseManager.getRealtimeDatabase() ?: return@withContext false
        val path = "$PATH_ACTION_PLANS/$planId"

        Log.d(TAG, "[RTDB DELETE START] Menghapus rencana aksi #$planId dari $path")
        return@withContext try {
            rtdb.getReference(path).removeValue().awaitTask()
            Log.d(TAG, "[RTDB DELETE SUCCESS] Rencana aksi #$planId dihapus dari $path")
            onComplete?.invoke(true, null)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Unknown Error"
            Log.e(TAG, "[RTDB DELETE ERROR] Gagal menghapus rencana aksi #$planId: $msg", e)
            onComplete?.invoke(false, msg)
            false
        }
    }
}
