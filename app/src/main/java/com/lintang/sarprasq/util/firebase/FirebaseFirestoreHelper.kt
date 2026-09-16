package com.lintang.sarprasq.util.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.SuratArsip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper khusus operasi Cloud Firestore untuk modul utama (Arsip Surat, Rencana Aksi, dsb)
 * dengan pemeriksaan jalur (path checking), konversi data anti-NullPointerException,
 * serta pencatatan Logcat terperinci.
 */
object FirebaseFirestoreHelper {

    private const val TAG = "FirestoreHelper"

    // Nama collection utama (Bahasa Indonesia & kompatibilitas sistem lama)
    const val COLLECTION_SURAT_SARPRAS = "surat_sarpras"
    const val COLLECTION_SURAT_ARSIP_COMPAT = "surat_arsip"

    const val COLLECTION_RENCANA_AKSI = "rencana_aksi"
    const val COLLECTION_ACTION_PLANS_COMPAT = "action_plans"

    const val COLLECTION_BARANG_SARPRAS = "barang_sarpras"
    const val COLLECTION_HELPDESK_TICKETS = "helpdesk_tickets"
    const val COLLECTION_PEMINJAMAN = "peminjaman_makro"

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) continuation.resumeWithException(exception)
        }
    }

    /**
     * Memeriksa keabsahan path Firestore (koleksi dan ID dokumen).
     * Mencegah karakter terlarang seperti slash beruntun, spasi kosong, atau string kosong.
     */
    fun validatePath(collection: String, documentId: String): Boolean {
        if (collection.isBlank() || documentId.isBlank()) {
            Log.e(TAG, "[PATH CHECK FAILED] Collection atau documentId kosong! col='$collection', docId='$documentId'")
            return false
        }
        if (collection.contains("//") || documentId.contains("/") || documentId.contains("..")) {
            Log.e(TAG, "[PATH CHECK FAILED] Karakter ilegal pada path: $collection/$documentId")
            return false
        }
        return true
    }

    // =========================================================================
    // CRUD: ARSIP SURAT (surat_sarpras & surat_arsip)
    // =========================================================================

    /**
     * Konversi aman dari SuratArsip ke Map untuk Firestore.
     * Mencegah crash jika terjadi null pointer pada properti opsional.
     */
    fun suratToMap(surat: SuratArsip): Map<String, Any?> {
        return mapOf(
            "id" to surat.id,
            "nomorSurat" to surat.nomorSurat.trim(),
            "tanggalSurat" to surat.tanggalSurat.trim(),
            "perihal" to surat.perihal.trim(),
            "deskripsiSurat" to surat.deskripsiSurat.trim(),
            "jenisSurat" to surat.jenisSurat.trim(),
            "statusArsip" to surat.statusArsip.trim(),
            "timestamp" to if (surat.timestamp > 0) surat.timestamp else System.currentTimeMillis(),
            "lastSyncedAt" to System.currentTimeMillis()
        )
    }

    /**
     * Konversi aman dari Dokumen Firestore ke entitas SuratArsip.
     * Menggunakan nilai fallback default jika terdapat field yang null/absen.
     */
    fun snapshotToSurat(doc: DocumentSnapshot): SuratArsip? {
        if (!doc.exists()) return null
        return try {
            val idVal = doc.getLong("id")?.toInt() ?: (doc.id.toIntOrNull() ?: 0)
            SuratArsip(
                id = idVal,
                nomorSurat = doc.getString("nomorSurat") ?: "",
                tanggalSurat = doc.getString("tanggalSurat") ?: "",
                perihal = doc.getString("perihal") ?: "",
                deskripsiSurat = doc.getString("deskripsiSurat") ?: "",
                jenisSurat = doc.getString("jenisSurat") ?: "Masuk",
                statusArsip = doc.getString("statusArsip") ?: "Arsip Fisik & Digital",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "[PARSE ERROR] Gagal konversi dokumen #${doc.id} ke SuratArsip: ${e.message}", e)
            null
        }
    }

    /**
     * Menyimpan/memperbarui data Arsip Surat ke Firestore dengan dual-collection
     * (surat_sarpras dan surat_arsip) agar kompatibel dengan seluruh integrasi.
     */
    suspend fun saveSuratArsip(
        surat: SuratArsip,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val fs = FirebaseManager.getFirestore()
        if (fs == null) {
            val err = "Firestore instance belum diinisialisasi"
            Log.e(TAG, "[Firestore WRITE ERROR] $err")
            onComplete?.invoke(false, err)
            return@withContext false
        }

        val docId = surat.id.toString()
        if (!validatePath(COLLECTION_SURAT_SARPRAS, docId)) {
            val err = "Path surat tidak valid: $COLLECTION_SURAT_SARPRAS/$docId"
            onComplete?.invoke(false, err)
            return@withContext false
        }

        val payload = suratToMap(surat)
        val path1 = "$COLLECTION_SURAT_SARPRAS/$docId"
        val path2 = "$COLLECTION_SURAT_ARSIP_COMPAT/$docId"

        Log.d(TAG, "[Firestore WRITE START] Menyimpan surat #${surat.id} (${surat.nomorSurat} - ${surat.perihal}) ke path: $path1 & $path2")

        return@withContext try {
            fs.collection(COLLECTION_SURAT_SARPRAS).document(docId)
                .set(payload, SetOptions.merge()).awaitTask()
            fs.collection(COLLECTION_SURAT_ARSIP_COMPAT).document(docId)
                .set(payload, SetOptions.merge()).awaitTask()

            Log.d(TAG, "[Firestore WRITE SUCCESS] Berhasil menyimpan surat #${surat.id} di $path1")
            onComplete?.invoke(true, null)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Unknown Firestore Error"
            Log.e(TAG, "[Firestore WRITE ERROR] Gagal menyimpan surat #${surat.id}: $msg", e)
            onComplete?.invoke(false, msg)
            false
        }
    }

    /**
     * Menghapus Arsip Surat dari Firestore.
     */
    suspend fun deleteSuratArsip(
        suratId: Int,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val fs = FirebaseManager.getFirestore()
        if (fs == null) {
            onComplete?.invoke(false, "Firestore instance null")
            return@withContext false
        }

        val docId = suratId.toString()
        Log.d(TAG, "[Firestore DELETE START] Menghapus surat #$docId dari $COLLECTION_SURAT_SARPRAS")

        return@withContext try {
            fs.collection(COLLECTION_SURAT_SARPRAS).document(docId).delete().awaitTask()
            fs.collection(COLLECTION_SURAT_ARSIP_COMPAT).document(docId).delete().awaitTask()
            Log.d(TAG, "[Firestore DELETE SUCCESS] Surat #$docId berhasil dihapus dari Firestore")
            onComplete?.invoke(true, null)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Unknown Error"
            Log.e(TAG, "[Firestore DELETE ERROR] Gagal menghapus surat #$docId: $msg", e)
            onComplete?.invoke(false, msg)
            false
        }
    }

    /**
     * Mengambil daftar Arsip Surat dari Firestore dengan penanganan error dan fallback aman.
     */
    suspend fun fetchSuratArsipList(): Result<List<SuratArsip>> = withContext(Dispatchers.IO) {
        val fs = FirebaseManager.getFirestore() ?: return@withContext Result.failure(
            IllegalStateException("Firestore belum terhubung")
        )

        Log.d(TAG, "[Firestore READ START] Mengambil data surat dari $COLLECTION_SURAT_SARPRAS...")
        return@withContext try {
            val snapshot = fs.collection(COLLECTION_SURAT_SARPRAS).get().awaitTask()
            val list = snapshot.documents.mapNotNull { snapshotToSurat(it) }
            Log.d(TAG, "[Firestore READ SUCCESS] Berhasil memuat ${list.size} surat dari Firestore")
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "[Firestore READ ERROR] Gagal mengambil surat: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // CRUD: RENCANA AKSI (rencana_aksi & action_plans)
    // =========================================================================

    /**
     * Konversi aman dari ActionPlan ke Map untuk Firestore.
     */
    fun actionPlanToMap(plan: ActionPlan): Map<String, Any?> {
        return mapOf(
            "id" to plan.id,
            "agenda" to plan.agenda.trim(),
            "jenisRencana" to plan.jenisRencana.trim(),
            "kategori" to plan.kategori.trim(),
            "targetWaktu" to plan.targetWaktu.trim(),
            "tanggalMulai" to plan.tanggalMulai.trim(),
            "tanggalRealisasi" to plan.tanggalRealisasi.trim(),
            "statusProgres" to plan.statusProgres.trim(),
            "pelaksana" to plan.pelaksana.trim(),
            "namaTukang" to (plan.namaTukang?.trim() ?: ""),
            "nomorWhatsapp" to (plan.nomorWhatsapp?.trim() ?: ""),
            "detailAset" to (plan.detailAset?.trim() ?: ""),
            "lokasiRuang" to (plan.lokasiRuang?.trim() ?: ""),
            "tingkatKerusakan" to (plan.tingkatKerusakan?.trim() ?: ""),
            "spesifikasi" to (plan.spesifikasi?.trim() ?: ""),
            "jumlahSatuan" to (plan.jumlahSatuan?.trim() ?: ""),
            "estimasiAnggaran" to (plan.estimasiAnggaran?.trim() ?: ""),
            "fotoUrl" to (plan.fotoUrl ?: ""),
            "timestamp" to if (plan.timestamp > 0) plan.timestamp else System.currentTimeMillis(),
            "lastSyncedAt" to System.currentTimeMillis()
        )
    }

    /**
     * Konversi aman dari Dokumen Firestore ke entitas ActionPlan.
     */
    fun snapshotToActionPlan(doc: DocumentSnapshot): ActionPlan? {
        if (!doc.exists()) return null
        return try {
            val idVal = doc.getLong("id")?.toInt() ?: (doc.id.toIntOrNull() ?: 0)
            ActionPlan(
                id = idVal,
                agenda = doc.getString("agenda") ?: "",
                jenisRencana = doc.getString("jenisRencana") ?: "Rencana Perbaikan / Perawatan",
                kategori = doc.getString("kategori") ?: "Pemeliharaan Berkala",
                targetWaktu = doc.getString("targetWaktu") ?: "",
                tanggalMulai = doc.getString("tanggalMulai") ?: "-",
                tanggalRealisasi = doc.getString("tanggalRealisasi") ?: "-",
                statusProgres = doc.getString("statusProgres") ?: "Belum Mulai",
                pelaksana = doc.getString("pelaksana") ?: "Internal",
                namaTukang = doc.getString("namaTukang"),
                nomorWhatsapp = doc.getString("nomorWhatsapp"),
                detailAset = doc.getString("detailAset"),
                lokasiRuang = doc.getString("lokasiRuang"),
                tingkatKerusakan = doc.getString("tingkatKerusakan"),
                spesifikasi = doc.getString("spesifikasi"),
                jumlahSatuan = doc.getString("jumlahSatuan"),
                estimasiAnggaran = doc.getString("estimasiAnggaran"),
                fotoUrl = doc.getString("fotoUrl"),
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "[PARSE ERROR] Gagal konversi dokumen #${doc.id} ke ActionPlan: ${e.message}", e)
            null
        }
    }

    /**
     * Menyimpan/memperbarui data Rencana Aksi ke Firestore dengan dual-collection
     * (rencana_aksi dan action_plans).
     */
    suspend fun saveActionPlan(
        plan: ActionPlan,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val fs = FirebaseManager.getFirestore()
        if (fs == null) {
            val err = "Firestore instance belum diinisialisasi"
            Log.e(TAG, "[Firestore WRITE ERROR] $err")
            onComplete?.invoke(false, err)
            return@withContext false
        }

        val docId = plan.id.toString()
        if (!validatePath(COLLECTION_RENCANA_AKSI, docId)) {
            val err = "Path rencana aksi tidak valid: $COLLECTION_RENCANA_AKSI/$docId"
            onComplete?.invoke(false, err)
            return@withContext false
        }

        val payload = actionPlanToMap(plan)
        val path1 = "$COLLECTION_RENCANA_AKSI/$docId"
        val path2 = "$COLLECTION_ACTION_PLANS_COMPAT/$docId"

        Log.d(TAG, "[Firestore WRITE START] Menyimpan rencana aksi #${plan.id} (${plan.agenda}) ke path: $path1 & $path2")

        return@withContext try {
            fs.collection(COLLECTION_RENCANA_AKSI).document(docId)
                .set(payload, SetOptions.merge()).awaitTask()
            fs.collection(COLLECTION_ACTION_PLANS_COMPAT).document(docId)
                .set(payload, SetOptions.merge()).awaitTask()

            Log.d(TAG, "[Firestore WRITE SUCCESS] Berhasil menyimpan rencana aksi #${plan.id} di $path1")
            onComplete?.invoke(true, null)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Unknown Firestore Error"
            Log.e(TAG, "[Firestore WRITE ERROR] Gagal menyimpan rencana aksi #${plan.id}: $msg", e)
            onComplete?.invoke(false, msg)
            false
        }
    }

    /**
     * Menghapus Rencana Aksi dari Firestore.
     */
    suspend fun deleteActionPlan(
        planId: Int,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val fs = FirebaseManager.getFirestore()
        if (fs == null) {
            onComplete?.invoke(false, "Firestore instance null")
            return@withContext false
        }

        val docId = planId.toString()
        Log.d(TAG, "[Firestore DELETE START] Menghapus rencana aksi #$docId dari $COLLECTION_RENCANA_AKSI")

        return@withContext try {
            fs.collection(COLLECTION_RENCANA_AKSI).document(docId).delete().awaitTask()
            fs.collection(COLLECTION_ACTION_PLANS_COMPAT).document(docId).delete().awaitTask()
            Log.d(TAG, "[Firestore DELETE SUCCESS] Rencana aksi #$docId berhasil dihapus dari Firestore")
            onComplete?.invoke(true, null)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Unknown Error"
            Log.e(TAG, "[Firestore DELETE ERROR] Gagal menghapus rencana aksi #$docId: $msg", e)
            onComplete?.invoke(false, msg)
            false
        }
    }

    /**
     * Mengambil daftar Rencana Aksi dari Firestore.
     */
    suspend fun fetchActionPlanList(): Result<List<ActionPlan>> = withContext(Dispatchers.IO) {
        val fs = FirebaseManager.getFirestore() ?: return@withContext Result.failure(
            IllegalStateException("Firestore belum terhubung")
        )

        Log.d(TAG, "[Firestore READ START] Mengambil rencana aksi dari $COLLECTION_RENCANA_AKSI...")
        return@withContext try {
            val snapshot = fs.collection(COLLECTION_RENCANA_AKSI).get().awaitTask()
            val list = snapshot.documents.mapNotNull { snapshotToActionPlan(it) }
            Log.d(TAG, "[Firestore READ SUCCESS] Berhasil memuat ${list.size} rencana aksi dari Firestore")
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "[Firestore READ ERROR] Gagal mengambil rencana aksi: ${e.message}", e)
            Result.failure(e)
        }
    }
}
