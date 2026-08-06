package com.lintang.sarprasq.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lintang.sarprasq.util.CompressedImageResult
import com.lintang.sarprasq.util.ImageCompressor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.lintang.sarprasq.data.local.SarprasDao
import com.lintang.sarprasq.data.local.getDefaultKategoriList
import com.lintang.sarprasq.data.local.getDefaultRuangList
import com.lintang.sarprasq.data.local.getDefaultSatuanList
import com.lintang.sarprasq.data.local.getDefaultStatusPenangananList
import com.lintang.sarprasq.data.local.getDefaultSubKategoriList
import com.lintang.sarprasq.data.local.getDefaultUrgensiList
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.DamageReport
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.KategoriMaster
import com.lintang.sarprasq.data.model.PeminjamanMakro
import com.lintang.sarprasq.data.model.ProjectTask
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.data.model.SatuanMaster
import com.lintang.sarprasq.data.model.StatusPenanganan
import com.lintang.sarprasq.data.model.SubKategoriMaster
import com.lintang.sarprasq.data.model.SuratArsip
import com.lintang.sarprasq.data.model.UrgensiMaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SarprasRepository(
    private val dao: SarprasDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private var firebaseDb: FirebaseDatabase? = null
    private var sarprasRef: DatabaseReference? = null
    private var firestoreDb: FirebaseFirestore? = null
    var isRealtimeListenerActive = false
    var realtimeListenerError: String? = null

    init {
        try {
            firebaseDb = FirebaseDatabase.getInstance("https://sarpras-134d7-default-rtdb.asia-southeast1.firebasedatabase.app")
            sarprasRef = firebaseDb?.getReference("sarprasq")
            firestoreDb = FirebaseFirestore.getInstance()
            attachRealtimeSyncListeners()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Firebase initialization error: ${e.message}")
        }
    }

    fun getDatabaseRef(): DatabaseReference? {
        return try {
            sarprasRef ?: firebaseDb?.getReference("sarprasq") ?: FirebaseDatabase.getInstance("https://sarpras-134d7-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("sarprasq")
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Error resolving Firebase reference: ${e.message}")
            null
        }
    }

    fun getFirestoreInstance(): FirebaseFirestore? {
        return try {
            firestoreDb ?: FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Error resolving Firestore instance: ${e.message}")
            null
        }
    }

    private fun attachRealtimeSyncListeners() {
        try {
            sarprasRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isRealtimeListenerActive = true
                    realtimeListenerError = null
                }

                override fun onCancelled(error: DatabaseError) {
                    isRealtimeListenerActive = false
                    realtimeListenerError = error.message
                    Log.e("SarprasRepository", "Realtime listener cancelled: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to attach listener: ${e.message}")
        }
    }

    /**
     * Mengompresi gambar (maksimal 400 KB, menjaga rasio aspek & EXIF)
     * dan mengunggahnya ke Firebase Storage.
     * Mengembalikan Result berisi Pair(downloadUrl, CompressedImageResult).
     */
    suspend fun uploadProofPhoto(
        context: Context,
        imageUri: Uri,
        folder: String = "bukti_kerusakan"
    ): Result<Pair<String, CompressedImageResult>> = withContext(Dispatchers.IO) {
        try {
            // 1. Kompresi gambar otomatis (max 400 KB, aspect ratio & EXIF rotasi terjaga)
            val compressed = ImageCompressor.compressImage(context, imageUri)
                ?: return@withContext Result.failure(Exception("Gagal mengompresi gambar. Format file tidak didukung."))

            val timestamp = System.currentTimeMillis()
            val fileName = "bukti_${timestamp}.jpg"
            val storageInstance = FirebaseStorage.getInstance()
            val storageRef = storageInstance.reference.child("$folder/$fileName")

            // 2. Upload byte array hasil kompresi ke Firebase Storage
            val uploadTask = storageRef.putBytes(compressed.bytes)

            val downloadUrl = suspendCancellableCoroutine<String> { continuation ->
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        if (continuation.isActive) continuation.resume(uri.toString())
                    }.addOnFailureListener { ex ->
                        if (continuation.isActive) continuation.resumeWithException(ex)
                    }
                }.addOnFailureListener { ex ->
                    if (continuation.isActive) continuation.resumeWithException(ex)
                }
            }

            Log.i("SarprasRepository", "Foto berhasil diunggah ke Firebase Storage: $downloadUrl (${compressed.sizeInKb} KB)")
            Result.success(Pair(downloadUrl, compressed))
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Gagal mengunggah foto ke Firebase Storage: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- Helpdesk ---
    val allHelpdeskReports: Flow<List<HelpdeskReport>> = dao.getAllHelpdeskReports()
    val pendingReports: Flow<List<HelpdeskReport>> = dao.getPendingReports()

    suspend fun getAllHelpdeskReportsList(): List<HelpdeskReport> = dao.getAllHelpdeskReportsList()

    suspend fun insertHelpdeskReport(report: HelpdeskReport) {
        val generatedId = dao.insertHelpdeskReport(report)
        val finalReport = if (report.id == 0) report.copy(id = generatedId.toInt()) else report
        syncHelpdeskToFirebase(finalReport)
    }

    suspend fun updateHelpdeskReport(report: HelpdeskReport) {
        dao.updateHelpdeskReport(report)
        syncHelpdeskToFirebase(report)
    }

    suspend fun deleteHelpdeskReport(report: HelpdeskReport) {
        dao.deleteHelpdeskReport(report)
        try {
            getDatabaseRef()?.child("helpdesk_reports")?.child(report.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Helpdesk from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("helpdesk_reports")?.document(report.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Helpdesk from Firestore: ${e.message}")
        }
    }

    private fun syncHelpdeskToFirebase(report: HelpdeskReport) {
        try {
            getDatabaseRef()?.child("helpdesk_reports")?.child(report.id.toString())?.setValue(report)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync Helpdesk to RTDB: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Helpdesk to RTDB: ${e.message}", e)
        }

        try {
            getFirestoreInstance()?.collection("helpdesk_reports")?.document(report.id.toString())?.set(report)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync Helpdesk to Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Helpdesk to Firestore: ${e.message}", e)
        }
    }

    // --- Action Plans ---
    val allActionPlans: Flow<List<ActionPlan>> = dao.getAllActionPlans()

    suspend fun getAllActionPlansList(): List<ActionPlan> = dao.getAllActionPlansList()

    suspend fun insertActionPlan(plan: ActionPlan) {
        val generatedId = dao.insertActionPlan(plan)
        val finalPlan = if (plan.id == 0) plan.copy(id = generatedId.toInt()) else plan
        syncActionPlanToFirebase(finalPlan)
    }

    suspend fun updateActionPlan(plan: ActionPlan) {
        dao.updateActionPlan(plan)
        syncActionPlanToFirebase(plan)
    }

    suspend fun deleteActionPlan(plan: ActionPlan) {
        dao.deleteActionPlan(plan)
        try {
            getDatabaseRef()?.child("action_plans")?.child(plan.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete ActionPlan from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("action_plans")?.document(plan.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete ActionPlan from Firestore: ${e.message}")
        }
    }

    private fun syncActionPlanToFirebase(plan: ActionPlan) {
        try {
            getDatabaseRef()?.child("action_plans")?.child(plan.id.toString())?.setValue(plan)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync ActionPlan to RTDB: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync ActionPlan to RTDB: ${e.message}", e)
        }

        try {
            getFirestoreInstance()?.collection("action_plans")?.document(plan.id.toString())?.set(plan)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync ActionPlan to Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync ActionPlan to Firestore: ${e.message}", e)
        }
    }

    // --- Surat Arsip ---
    val allSuratArsip: Flow<List<SuratArsip>> = dao.getAllSuratArsip()

    suspend fun getAllSuratArsipList(): List<SuratArsip> = dao.getAllSuratArsipList()

    suspend fun insertSuratArsip(surat: SuratArsip) {
        val generatedId = dao.insertSuratArsip(surat)
        val finalSurat = if (surat.id == 0) surat.copy(id = generatedId.toInt()) else surat
        syncSuratToFirebase(finalSurat)
    }

    suspend fun deleteSuratArsip(surat: SuratArsip) {
        dao.deleteSuratArsip(surat)
        try {
            getDatabaseRef()?.child("surat_arsip")?.child(surat.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Surat from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("surat_arsip")?.document(surat.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Surat from Firestore: ${e.message}")
        }
    }

    private fun syncSuratToFirebase(surat: SuratArsip) {
        try {
            getDatabaseRef()?.child("surat_arsip")?.child(surat.id.toString())?.setValue(surat)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync Surat to RTDB: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Surat to RTDB: ${e.message}", e)
        }

        try {
            getFirestoreInstance()?.collection("surat_arsip")?.document(surat.id.toString())?.set(surat)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync Surat to Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Surat to Firestore: ${e.message}", e)
        }
    }

    // --- Peminjaman Makro ---
    val allPeminjamanMakro: Flow<List<PeminjamanMakro>> = dao.getAllPeminjamanMakro()

    suspend fun getAllPeminjamanMakroList(): List<PeminjamanMakro> = dao.getAllPeminjamanMakroList()

    suspend fun insertPeminjamanMakro(peminjaman: PeminjamanMakro) {
        val generatedId = dao.insertPeminjamanMakro(peminjaman)
        val finalPeminjaman = if (peminjaman.id == 0) peminjaman.copy(id = generatedId.toInt()) else peminjaman
        syncPeminjamanToFirebase(finalPeminjaman)
    }

    suspend fun deletePeminjamanMakro(peminjaman: PeminjamanMakro) {
        dao.deletePeminjamanMakro(peminjaman)
        try {
            getDatabaseRef()?.child("peminjaman_makro")?.child(peminjaman.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Peminjaman from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("peminjaman_makro")?.document(peminjaman.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Peminjaman from Firestore: ${e.message}")
        }
    }

    private fun syncPeminjamanToFirebase(peminjaman: PeminjamanMakro) {
        try {
            getDatabaseRef()?.child("peminjaman_makro")?.child(peminjaman.id.toString())?.setValue(peminjaman)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync Peminjaman to RTDB: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Peminjaman to RTDB: ${e.message}", e)
        }

        try {
            getFirestoreInstance()?.collection("peminjaman_makro")?.document(peminjaman.id.toString())?.set(peminjaman)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync Peminjaman to Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Peminjaman to Firestore: ${e.message}", e)
        }
    }

    // --- Master Data: Ruang ---
    val allRuang: Flow<List<Ruang>> = dao.getAllRuang()

    suspend fun getAllRuangList(): List<Ruang> = dao.getAllRuangList()

    suspend fun insertRuang(ruang: Ruang) {
        val id = dao.insertRuang(ruang)
        val finalItem = if (ruang.id == 0) ruang.copy(id = id.toInt()) else ruang
        syncRuangToFirebase(finalItem)
    }

    suspend fun updateRuang(ruang: Ruang) {
        dao.updateRuang(ruang)
        syncRuangToFirebase(ruang)
    }

    suspend fun deleteRuang(ruang: Ruang) {
        dao.deleteRuang(ruang)
        try {
            getDatabaseRef()?.child("master_ruang")?.child(ruang.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Ruang from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_ruang")?.document(ruang.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Ruang from Firestore: ${e.message}")
        }
    }

    private fun syncRuangToFirebase(ruang: Ruang) {
        try {
            getDatabaseRef()?.child("master_ruang")?.child(ruang.id.toString())?.setValue(ruang)
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Ruang to RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_ruang")?.document(ruang.id.toString())?.set(ruang)
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Ruang to Firestore: ${e.message}")
        }
    }

    suspend fun resetDefaultRuang() {
        val defaults = getDefaultRuangList()
        dao.deleteAllRuang()
        dao.insertRuangList(defaults)
        try {
            getDatabaseRef()?.child("master_ruang")?.setValue(defaults.associateBy { it.id.toString() })
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset Ruang in RTDB: ${e.message}")
        }
        try {
            val fs = getFirestoreInstance()
            if (fs != null) {
                defaults.forEach { item -> fs.collection("master_ruang").document(item.id.toString()).set(item) }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset Ruang in Firestore: ${e.message}")
        }
    }

    // --- Master Data: Status Penanganan ---
    val allStatusPenanganan: Flow<List<StatusPenanganan>> = dao.getAllStatusPenanganan()

    suspend fun getAllStatusPenangananList(): List<StatusPenanganan> = dao.getAllStatusPenangananList()

    suspend fun insertStatusPenanganan(status: StatusPenanganan) {
        val generatedId = dao.insertStatusPenanganan(status)
        val finalStatus = if (status.id == 0) status.copy(id = generatedId.toInt()) else status
        syncStatusPenangananToFirebase(finalStatus)
    }

    suspend fun updateStatusPenanganan(status: StatusPenanganan) {
        dao.updateStatusPenanganan(status)
        syncStatusPenangananToFirebase(status)
    }

    suspend fun deleteStatusPenanganan(status: StatusPenanganan) {
        dao.deleteStatusPenanganan(status)
        try {
            getDatabaseRef()?.child("master_status_penanganan")?.child(status.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete StatusPenanganan from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_status_penanganan")?.document(status.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete StatusPenanganan from Firestore: ${e.message}")
        }
    }

    private fun syncStatusPenangananToFirebase(status: StatusPenanganan) {
        try {
            getDatabaseRef()?.child("master_status_penanganan")?.child(status.id.toString())?.setValue(status)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync StatusPenanganan to RTDB: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync StatusPenanganan to RTDB: ${e.message}", e)
        }

        try {
            getFirestoreInstance()?.collection("master_status_penanganan")?.document(status.id.toString())?.set(status)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync StatusPenanganan to Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync StatusPenanganan to Firestore: ${e.message}", e)
        }
    }

    suspend fun resetDefaultStatusPenanganan() {
        val defaults = getDefaultStatusPenangananList()
        dao.deleteAllStatusPenanganan()
        dao.insertStatusPenangananList(defaults)
        try {
            getDatabaseRef()?.child("master_status_penanganan")?.setValue(defaults.associateBy { it.id.toString() })
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset StatusPenanganan in RTDB: ${e.message}")
        }
        try {
            val fs = getFirestoreInstance()
            if (fs != null) {
                defaults.forEach { item ->
                    fs.collection("master_status_penanganan").document(item.id.toString()).set(item)
                }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset StatusPenanganan in Firestore: ${e.message}")
        }
    }

    // --- Master Data: Urgensi ---
    val allUrgensi: Flow<List<UrgensiMaster>> = dao.getAllUrgensi()

    suspend fun getAllUrgensiList(): List<UrgensiMaster> = dao.getAllUrgensiList()

    suspend fun insertUrgensi(urgensi: UrgensiMaster) {
        val generatedId = dao.insertUrgensi(urgensi)
        val finalUrgensi = if (urgensi.id == 0) urgensi.copy(id = generatedId.toInt()) else urgensi
        syncUrgensiToFirebase(finalUrgensi)
    }

    suspend fun updateUrgensi(urgensi: UrgensiMaster) {
        dao.updateUrgensi(urgensi)
        syncUrgensiToFirebase(urgensi)
    }

    suspend fun deleteUrgensi(urgensi: UrgensiMaster) {
        dao.deleteUrgensi(urgensi)
        try {
            getDatabaseRef()?.child("master_urgensi")?.child(urgensi.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Urgensi from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_urgensi")?.document(urgensi.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Urgensi from Firestore: ${e.message}")
        }
    }

    private fun syncUrgensiToFirebase(urgensi: UrgensiMaster) {
        try {
            getDatabaseRef()?.child("master_urgensi")?.child(urgensi.id.toString())?.setValue(urgensi)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync Urgensi to RTDB: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Urgensi to RTDB: ${e.message}", e)
        }

        try {
            getFirestoreInstance()?.collection("master_urgensi")?.document(urgensi.id.toString())?.set(urgensi)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync Urgensi to Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Urgensi to Firestore: ${e.message}", e)
        }
    }

    suspend fun resetDefaultUrgensi() {
        val defaults = getDefaultUrgensiList()
        dao.deleteAllUrgensi()
        dao.insertUrgensiList(defaults)
        try {
            getDatabaseRef()?.child("master_urgensi")?.setValue(defaults.associateBy { it.id.toString() })
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset Urgensi in RTDB: ${e.message}")
        }
        try {
            val fs = getFirestoreInstance()
            if (fs != null) {
                defaults.forEach { item ->
                    fs.collection("master_urgensi").document(item.id.toString()).set(item)
                }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset Urgensi in Firestore: ${e.message}")
        }
    }

    // --- Master Data: Kategori ---
    val allKategori: Flow<List<KategoriMaster>> = dao.getAllKategori()

    suspend fun getAllKategoriList(): List<KategoriMaster> = dao.getAllKategoriList()

    suspend fun insertKategori(kategori: KategoriMaster) {
        val id = dao.insertKategori(kategori)
        val finalItem = if (kategori.id == 0) kategori.copy(id = id.toInt()) else kategori
        syncKategoriToFirebase(finalItem)
    }

    suspend fun updateKategori(kategori: KategoriMaster) {
        dao.updateKategori(kategori)
        syncKategoriToFirebase(kategori)
    }

    suspend fun deleteKategori(kategori: KategoriMaster) {
        dao.deleteKategori(kategori)
        try {
            getDatabaseRef()?.child("master_kategori")?.child(kategori.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Kategori from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_kategori")?.document(kategori.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Kategori from Firestore: ${e.message}")
        }
    }

    private fun syncKategoriToFirebase(kategori: KategoriMaster) {
        try {
            getDatabaseRef()?.child("master_kategori")?.child(kategori.id.toString())?.setValue(kategori)
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Kategori to RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_kategori")?.document(kategori.id.toString())?.set(kategori)
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Kategori to Firestore: ${e.message}")
        }
    }

    suspend fun resetDefaultKategori() {
        val defaults = getDefaultKategoriList()
        dao.deleteAllKategori()
        dao.insertKategoriList(defaults)
        try {
            getDatabaseRef()?.child("master_kategori")?.setValue(defaults.associateBy { it.id.toString() })
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset Kategori in RTDB: ${e.message}")
        }
        try {
            val fs = getFirestoreInstance()
            if (fs != null) {
                defaults.forEach { item -> fs.collection("master_kategori").document(item.id.toString()).set(item) }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset Kategori in Firestore: ${e.message}")
        }
    }

    // --- Master Data: Satuan ---
    val allSatuan: Flow<List<SatuanMaster>> = dao.getAllSatuan()

    suspend fun getAllSatuanList(): List<SatuanMaster> = dao.getAllSatuanList()

    suspend fun insertSatuan(satuan: SatuanMaster) {
        val id = dao.insertSatuan(satuan)
        val finalItem = if (satuan.id == 0) satuan.copy(id = id.toInt()) else satuan
        syncSatuanToFirebase(finalItem)
    }

    suspend fun updateSatuan(satuan: SatuanMaster) {
        dao.updateSatuan(satuan)
        syncSatuanToFirebase(satuan)
    }

    suspend fun deleteSatuan(satuan: SatuanMaster) {
        dao.deleteSatuan(satuan)
        try {
            getDatabaseRef()?.child("master_satuan")?.child(satuan.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Satuan from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_satuan")?.document(satuan.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete Satuan from Firestore: ${e.message}")
        }
    }

    private fun syncSatuanToFirebase(satuan: SatuanMaster) {
        try {
            getDatabaseRef()?.child("master_satuan")?.child(satuan.id.toString())?.setValue(satuan)
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Satuan to RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_satuan")?.document(satuan.id.toString())?.set(satuan)
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync Satuan to Firestore: ${e.message}")
        }
    }

    suspend fun resetDefaultSatuan() {
        val defaults = getDefaultSatuanList()
        dao.deleteAllSatuan()
        dao.insertSatuanList(defaults)
        try {
            getDatabaseRef()?.child("master_satuan")?.setValue(defaults.associateBy { it.id.toString() })
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset Satuan in RTDB: ${e.message}")
        }
        try {
            val fs = getFirestoreInstance()
            if (fs != null) {
                defaults.forEach { item -> fs.collection("master_satuan").document(item.id.toString()).set(item) }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset Satuan in Firestore: ${e.message}")
        }
    }

    // --- Master Data: Sub-Kategori / Sifat Pekerjaan ---
    val allSubKategori: Flow<List<SubKategoriMaster>> = dao.getAllSubKategori()

    suspend fun getAllSubKategoriList(): List<SubKategoriMaster> = dao.getAllSubKategoriList()

    suspend fun insertSubKategori(subKategori: SubKategoriMaster) {
        val id = dao.insertSubKategori(subKategori)
        val finalItem = if (subKategori.id == 0) subKategori.copy(id = id.toInt()) else subKategori
        syncSubKategoriToFirebase(finalItem)
    }

    suspend fun updateSubKategori(subKategori: SubKategoriMaster) {
        dao.updateSubKategori(subKategori)
        syncSubKategoriToFirebase(subKategori)
    }

    suspend fun deleteSubKategori(subKategori: SubKategoriMaster) {
        dao.deleteSubKategori(subKategori)
        try {
            getDatabaseRef()?.child("master_subkategori")?.child(subKategori.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete SubKategori from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_subkategori")?.document(subKategori.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete SubKategori from Firestore: ${e.message}")
        }
    }

    private fun syncSubKategoriToFirebase(subKategori: SubKategoriMaster) {
        try {
            getDatabaseRef()?.child("master_subkategori")?.child(subKategori.id.toString())?.setValue(subKategori)
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync SubKategori to RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("master_subkategori")?.document(subKategori.id.toString())?.set(subKategori)
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync SubKategori to Firestore: ${e.message}")
        }
    }

    suspend fun resetDefaultSubKategori() {
        val defaults = getDefaultSubKategoriList()
        dao.deleteAllSubKategori()
        dao.insertSubKategoriList(defaults)
        try {
            getDatabaseRef()?.child("master_subkategori")?.setValue(defaults.associateBy { it.id.toString() })
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset SubKategori in RTDB: ${e.message}")
        }
        try {
            val fs = getFirestoreInstance()
            if (fs != null) {
                defaults.forEach { item -> fs.collection("master_subkategori").document(item.id.toString()).set(item) }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to reset SubKategori in Firestore: ${e.message}")
        }
    }

    // --- Damage Reports (Room-based Multi-item Damage) ---
    val allDamageReports: Flow<List<DamageReport>> = dao.getAllDamageReports()

    fun getDamageReportsByRoom(roomId: Int): Flow<List<DamageReport>> = dao.getDamageReportsByRoom(roomId)

    suspend fun getAllDamageReportsList(): List<DamageReport> = dao.getAllDamageReportsList()

    suspend fun insertDamageReport(report: DamageReport) {
        val generatedId = dao.insertDamageReport(report)
        val finalReport = if (report.id == 0) report.copy(id = generatedId.toInt()) else report
        syncDamageReportToFirebase(finalReport)
    }

    suspend fun updateDamageReport(report: DamageReport) {
        dao.updateDamageReport(report)
        syncDamageReportToFirebase(report)
    }

    suspend fun deleteDamageReport(report: DamageReport) {
        dao.deleteDamageReport(report)
        try {
            getDatabaseRef()?.child("damage_reports")?.child(report.id.toString())?.removeValue()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete DamageReport from RTDB: ${e.message}")
        }
        try {
            getFirestoreInstance()?.collection("damage_reports")?.document(report.id.toString())?.delete()
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to delete DamageReport from Firestore: ${e.message}")
        }
    }

    private fun syncDamageReportToFirebase(report: DamageReport) {
        try {
            getDatabaseRef()?.child("damage_reports")?.child(report.id.toString())?.setValue(report)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync DamageReport to RTDB: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync DamageReport to RTDB: ${e.message}", e)
        }

        try {
            getFirestoreInstance()?.collection("damage_reports")?.document(report.id.toString())?.set(report)
                ?.addOnFailureListener { e ->
                    Log.e("SarprasRepository", "Failed to sync DamageReport to Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to sync DamageReport to Firestore: ${e.message}", e)
        }
    }

    // --- Pre-seed / Inisialisasi Data Dummy ---
    suspend fun seedDummyData() {
        val currentTime = System.currentTimeMillis()

        // 1. Helpdesk Reports
        val helpdeskReports = listOf(
            HelpdeskReport(
                id = 1,
                tanggal = "31/07/2026",
                pelapor = "Bu Siti (Guru)",
                lokasi = "Lab Komputer Utama",
                deskripsi = "Proyektor utama mati total saat KBM berlangsung",
                urgensi = "Penting",
                status = "Proses",
                tindakan = "Menunggu lampu pengganti baru & pembersihan fan",
                timestamp = currentTime - 1000
            ),
            HelpdeskReport(
                id = 2,
                tanggal = "31/07/2026",
                pelapor = "Siswa XI IPA 2",
                lokasi = "Toilet Siswa Lt. 2",
                deskripsi = "Kran air patah / bocor deras menggenangi lantai",
                urgensi = "Darurat",
                status = "Selesai",
                tindakan = "Langsung diganti kran stainless heavy duty baru hari ini",
                timestamp = currentTime - 2000
            ),
            HelpdeskReport(
                id = 3,
                tanggal = "30/07/2026",
                pelapor = "Pak Joko (Staf)",
                lokasi = "Ruang Guru",
                deskripsi = "Stopkontak korslet / berbau hangus",
                urgensi = "Darurat",
                status = "Selesai",
                tindakan = "Jalur kabel diperbaiki & MCB diamankan",
                timestamp = currentTime - 3000
            ),
            HelpdeskReport(
                id = 4,
                tanggal = "29/07/2026",
                pelapor = "Pak Bambang (Waka Sarpras)",
                lokasi = "Aula Utama - Sound System",
                deskripsi = "Suara sound system kresek-kresek dan mati terputus",
                urgensi = "Penting",
                status = "Pending",
                tindakan = "Perlu perbaikan modul amplifier oleh vendor luar",
                alasanPending = "Menunggu teknisi luar datang ke sekolah",
                estimasiEksekusi = "02/08/2026",
                timestamp = currentTime - 4000
            ),
            HelpdeskReport(
                id = 5,
                tanggal = "28/07/2026",
                pelapor = "Ibu Rina (Perpus)",
                lokasi = "Perpus - AC Kurang Dingin",
                deskripsi = "Unit AC Sharp 2PK perpus indoor berisik & tidak dingin",
                urgensi = "Penting",
                status = "Pending",
                tindakan = "Pengajuan dana isi freon R32 dan maintenance service",
                alasanPending = "Perlu isi ulang Freon (Masuk anggaran minggu depan)",
                estimasiEksekusi = "05/08/2026",
                timestamp = currentTime - 5000
            ),
            HelpdeskReport(
                id = 6,
                tanggal = "01/08/2026",
                pelapor = "Wali Kelas X IPA 1",
                lokasi = "Ruang Kelas X IPA 1",
                deskripsi = "Proyektor BenQ Wallmount bercak bintik hitam",
                urgensi = "Biasa",
                status = "Proses",
                tindakan = "Pengajuan penggantian DMD Chip ke distributor",
                timestamp = currentTime - 6000
            )
        )

        // 2. Action Plans
        val actionPlans = listOf(
            ActionPlan(
                id = 1,
                agenda = "Pengecekan tandon air & kuras rutin tandon utama",
                kategori = "Pemeliharaan Berkala",
                targetWaktu = "01/08/2026",
                tanggalRealisasi = "01/08/2026",
                statusProgres = "Selesai",
                timestamp = currentTime - 1000
            ),
            ActionPlan(
                id = 2,
                agenda = "Perbaikan engsel pintu & kunci di 5 ruang kelas X",
                kategori = "Perbaikan Minor",
                targetWaktu = "03/08/2026",
                tanggalRealisasi = "-",
                statusProgres = "Belum Mulai",
                timestamp = currentTime - 2000
            ),
            ActionPlan(
                id = 3,
                agenda = "Servis berkala & cuci AC seluruh ruangan lantai 1-2",
                kategori = "Pemeliharaan Berkala",
                targetWaktu = "10/08/2026",
                tanggalRealisasi = "-",
                statusProgres = "Sedang Berjalan",
                timestamp = currentTime - 3000
            ),
            ActionPlan(
                id = 4,
                agenda = "Pengadaan & Pemasangan CCTV Baru Selasar Aula",
                kategori = "Pengadaan Baru",
                targetWaktu = "15/08/2026",
                tanggalRealisasi = "-",
                statusProgres = "Belum Mulai",
                timestamp = currentTime - 4000
            ),
            ActionPlan(
                id = 5,
                agenda = "Pengecatan ulang dinding tembok Lab Komputer & Perpus",
                kategori = "Perbaikan Mayor",
                targetWaktu = "20/08/2026",
                tanggalRealisasi = "-",
                statusProgres = "Belum Mulai",
                timestamp = currentTime - 5000
            )
        )

        // 3. Surat Arsip
        val suratArsipList = listOf(
            SuratArsip(
                id = 1,
                nomorSurat = "014/SRV/VII/2026",
                tanggalSurat = "15/07/2026",
                perihal = "Surat Permohonan Bantuan Perbaikan Plafon (Dinas Pendidikan)",
                jenisSurat = "Keluar",
                statusArsip = "Arsip Fisik & Digital",
                timestamp = currentTime - 1000
            ),
            SuratArsip(
                id = 2,
                nomorSurat = "088/SP/SARPRAS/VI/2026",
                tanggalSurat = "20/06/2026",
                perihal = "Surat Penawaran Service AC & Maintenance rutin (Vendor PT ColdTech)",
                jenisSurat = "Masuk",
                statusArsip = "Arsip Digital",
                timestamp = currentTime - 2000
            ),
            SuratArsip(
                id = 3,
                nomorSurat = "102/BA-ST/VII/2026",
                tanggalSurat = "28/07/2026",
                perihal = "Berita Acara Serah Terima Pengadaan 10 Unit Proyektor Baru",
                jenisSurat = "Masuk",
                statusArsip = "Arsip Fisik & Digital",
                timestamp = currentTime - 3000
            ),
            SuratArsip(
                id = 4,
                nomorSurat = "021/SK-SAR/VIII/2026",
                tanggalSurat = "01/08/2026",
                perihal = "Surat Keputusan Tim Inventarisasi & Pemeliharaan Sarpras Sekolah",
                jenisSurat = "Keluar",
                statusArsip = "Arsip Fisik & Digital",
                timestamp = currentTime - 4000
            )
        )

        // 4. Peminjaman Makro
        val peminjamanMakroList = listOf(
            PeminjamanMakro(
                id = 1,
                bulanTahun = "Juli 2026",
                namaBarang = "Proyektor Portable (Unit Sekolah)",
                jumlahPeminjaman = 14,
                kondisi = "Aman",
                timestamp = currentTime - 1000
            ),
            PeminjamanMakro(
                id = 2,
                bulanTahun = "Juli 2026",
                namaBarang = "Sound System Wireless Portable",
                jumlahPeminjaman = 6,
                kondisi = "Aman",
                timestamp = currentTime - 2000
            ),
            PeminjamanMakro(
                id = 3,
                bulanTahun = "Juli 2026",
                namaBarang = "Kabel Roll Listrik 50m Heavy Duty",
                jumlahPeminjaman = 18,
                kondisi = "Aman",
                timestamp = currentTime - 3000
            ),
            PeminjamanMakro(
                id = 4,
                bulanTahun = "Agustus 2026",
                namaBarang = "Laptop Operasional Panitia Lomba",
                jumlahPeminjaman = 8,
                kondisi = "Perlu Cek Baterai",
                timestamp = currentTime - 4000
            ),
            PeminjamanMakro(
                id = 5,
                bulanTahun = "Agustus 2026",
                namaBarang = "Kursi Lipat Stainless Futura (100 unit)",
                jumlahPeminjaman = 5,
                kondisi = "Aman (Siap Gunakan)",
                timestamp = currentTime - 5000
            )
        )

        // 5. Master Ruang Default
        val ruangList = getDefaultRuangList()

        // 6. Master Status Penanganan Default
        val statusList = getDefaultStatusPenangananList()

        // 7. Master Urgensi Default
        val urgensiList = getDefaultUrgensiList()

        // 8. Master Kategori Default
        val kategoriList = getDefaultKategoriList()

        // 9. Master Satuan Default
        val satuanList = getDefaultSatuanList()

        // 10. Damage Reports (Multi-Item per Room)
        val damageReportsList = listOf(
            DamageReport(id = 1, roomId = 1, namaRuang = "Lab Komputer Utama", namaPelapor = "Bu Siti", namaItemKerusakan = "AC Indoor Ceiling Unit 1", tanggalLapor = "30/07/2026", statusPenanganan = "Proses", ditanganiOleh = "Internal", tanggalSelesai = "-", keterangan = "Sedang dikuras freon dan pembersihan filter indoor", timestamp = currentTime - 1000),
            DamageReport(id = 2, roomId = 1, namaRuang = "Lab Komputer Utama", namaPelapor = "Bu Siti", namaItemKerusakan = "Remot Proyektor Epson", tanggalLapor = "31/07/2026", statusPenanganan = "Segera", ditanganiOleh = "Internal", tanggalSelesai = "-", keterangan = "Tombol power macet dan indikator merah kedip", timestamp = currentTime - 2000),
            DamageReport(id = 3, roomId = 1, namaRuang = "Lab Komputer Utama", namaPelapor = "Lab Infonet", namaItemKerusakan = "PC Client #04 (PSU)", tanggalLapor = "28/07/2026", statusPenanganan = "Selesai", ditanganiOleh = "Internal", tanggalSelesai = "30/07/2026", keterangan = "Power Supply diganti unit cadangan 450W", timestamp = currentTime - 3000),
            DamageReport(id = 4, roomId = 1, namaRuang = "Lab Komputer Utama", namaPelapor = "Tim IT", namaItemKerusakan = "Kabel LAN Switch Port 12", tanggalLapor = "01/08/2026", statusPenanganan = "Segera", ditanganiOleh = "Internal", tanggalSelesai = "-", keterangan = "Klip RJ45 patah & koneksi terputus-putus", timestamp = currentTime - 4000),
            DamageReport(id = 5, roomId = 2, namaRuang = "Ruang Guru", namaPelapor = "Pak Joko", namaItemKerusakan = "Kipas Angin Dinding Cosmos", tanggalLapor = "29/07/2026", statusPenanganan = "Segera", ditanganiOleh = "Internal", tanggalSelesai = "-", keterangan = "Baling-baling berbunyi bising dan putaran melemah", timestamp = currentTime - 5000),
            DamageReport(id = 6, roomId = 2, namaRuang = "Ruang Guru", namaPelapor = "Pak Joko", namaItemKerusakan = "Printer Canon Pixma G3010", tanggalLapor = "27/07/2026", statusPenanganan = "Pending", ditanganiOleh = "Teknisi Luar", tanggalSelesai = "-", keterangan = "Head printer macet & error E05, dikirim ke Servis Center", timestamp = currentTime - 6000),
            DamageReport(id = 7, roomId = 3, namaRuang = "Aula Utama", namaPelapor = "Pak Bambang", namaItemKerusakan = "Power Amplifier Sound System", tanggalLapor = "29/07/2026", statusPenanganan = "Pending", ditanganiOleh = "Teknisi Luar", tanggalSelesai = "-", keterangan = "Menunggu suku cadang komponen transistor dari vendor", timestamp = currentTime - 7000),
            DamageReport(id = 8, roomId = 3, namaRuang = "Aula Utama", namaPelapor = "Pak Bambang", namaItemKerusakan = "Microphone Wireless B", tanggalLapor = "25/07/2026", statusPenanganan = "Selesai", ditanganiOleh = "Teknisi Luar", tanggalSelesai = "27/07/2026", keterangan = "Spul terbakar & modul frekuensi rusak, diajukan hapus aset", timestamp = currentTime - 8000),
            DamageReport(id = 9, roomId = 4, namaRuang = "Toilet Siswa Lt. 2", namaPelapor = "Seksi Sarpras", namaItemKerusakan = "Kran Air Sink Washtafell #1", tanggalLapor = "31/07/2026", statusPenanganan = "Selesai", ditanganiOleh = "Internal", tanggalSelesai = "31/07/2026", keterangan = "Diganti dengan kran stainless heavy duty baru", timestamp = currentTime - 9000),
            DamageReport(id = 10, roomId = 5, namaRuang = "Perpus - Ruang Baca", namaPelapor = "Ibu Rina", namaItemKerusakan = "AC Split Sharp 2PK", tanggalLapor = "28/07/2026", statusPenanganan = "Pending", ditanganiOleh = "Teknisi Luar", tanggalSelesai = "-", keterangan = "Perlu pencucian outdoor & pengisian freon R32", timestamp = currentTime - 10000),
            DamageReport(id = 11, roomId = 6, namaRuang = "Ruang Kelas X IPA 1", namaPelapor = "Wali Kelas", namaItemKerusakan = "Proyektor BenQ Wallmount", tanggalLapor = "01/08/2026", statusPenanganan = "Segera", ditanganiOleh = "Internal", tanggalSelesai = "-", keterangan = "Tampilan buram & bercak bintik hitam (DMD chip)", timestamp = currentTime - 11000),
            DamageReport(id = 12, roomId = 6, namaRuang = "Ruang Kelas X IPA 1", namaPelapor = "Ketua Kelas", namaItemKerusakan = "Engsel Pintu Belakang Kelas", tanggalLapor = "29/07/2026", statusPenanganan = "Selesai", ditanganiOleh = "Internal", tanggalSelesai = "29/07/2026", keterangan = "Baut dikencangkan & diberi pelumas", timestamp = currentTime - 12000)
        )

        restoreAllData(
            reports = helpdeskReports,
            plans = actionPlans,
            suratList = suratArsipList,
            peminjamanList = peminjamanMakroList,
            ruangList = ruangList,
            statusPenangananList = statusList,
            urgensiList = urgensiList,
            kategoriList = kategoriList,
            satuanList = satuanList,
            damageReportsList = damageReportsList
        )
    }

    // --- Bulk Restore ---
    suspend fun restoreAllData(
        reports: List<HelpdeskReport>,
        plans: List<ActionPlan>,
        suratList: List<SuratArsip>,
        peminjamanList: List<PeminjamanMakro>,
        ruangList: List<Ruang> = emptyList(),
        statusPenangananList: List<StatusPenanganan> = emptyList(),
        urgensiList: List<UrgensiMaster> = emptyList(),
        kategoriList: List<KategoriMaster> = emptyList(),
        satuanList: List<SatuanMaster> = emptyList(),
        damageReportsList: List<DamageReport> = emptyList()
    ) {
        // 1. Local Room DB
        dao.deleteAllHelpdeskReports()
        dao.deleteAllActionPlans()
        dao.deleteAllSuratArsip()
        dao.deleteAllPeminjamanMakro()
        dao.deleteAllRuang()
        if (statusPenangananList.isNotEmpty()) dao.deleteAllStatusPenanganan()
        if (urgensiList.isNotEmpty()) dao.deleteAllUrgensi()
        dao.deleteAllKategori()
        if (satuanList.isNotEmpty()) dao.deleteAllSatuan()
        if (damageReportsList.isNotEmpty()) dao.deleteAllDamageReports()

        if (reports.isNotEmpty()) dao.insertHelpdeskReports(reports)
        if (plans.isNotEmpty()) dao.insertActionPlans(plans)
        if (suratList.isNotEmpty()) dao.insertSuratArsipList(suratList)
        if (peminjamanList.isNotEmpty()) dao.insertPeminjamanMakroList(peminjamanList)
        if (ruangList.isNotEmpty()) dao.insertRuangList(ruangList)
        if (statusPenangananList.isNotEmpty()) dao.insertStatusPenangananList(statusPenangananList)
        if (urgensiList.isNotEmpty()) dao.insertUrgensiList(urgensiList)
        if (kategoriList.isNotEmpty()) dao.insertKategoriList(kategoriList)
        if (satuanList.isNotEmpty()) dao.insertSatuanList(satuanList)
        if (damageReportsList.isNotEmpty()) dao.insertDamageReportList(damageReportsList)

        // 2. Sync full state to Firebase (Dual-Write: RTDB & Cloud Firestore)
        try {
            val ref = sarprasRef
            if (ref != null) {
                ref.setValue(null) // clear root

                val helpdeskMap = reports.associateBy { it.id.toString() }
                if (helpdeskMap.isNotEmpty()) {
                    ref.child("helpdesk_reports").setValue(helpdeskMap)
                }

                val planMap = plans.associateBy { it.id.toString() }
                if (planMap.isNotEmpty()) {
                    ref.child("action_plans").setValue(planMap)
                }

                val suratMap = suratList.associateBy { it.id.toString() }
                if (suratMap.isNotEmpty()) {
                    ref.child("surat_arsip").setValue(suratMap)
                }

                val peminjamanMap = peminjamanList.associateBy { it.id.toString() }
                if (peminjamanMap.isNotEmpty()) {
                    ref.child("peminjaman_makro").setValue(peminjamanMap)
                }

                val ruangMap = ruangList.associateBy { it.id.toString() }
                if (ruangMap.isNotEmpty()) {
                    ref.child("master_ruang").setValue(ruangMap)
                }

                val statusMap = statusPenangananList.associateBy { it.id.toString() }
                if (statusMap.isNotEmpty()) {
                    ref.child("master_status_penanganan").setValue(statusMap)
                }

                val urgensiMap = urgensiList.associateBy { it.id.toString() }
                if (urgensiMap.isNotEmpty()) {
                    ref.child("master_urgensi").setValue(urgensiMap)
                }

                val katMap = kategoriList.associateBy { it.id.toString() }
                if (katMap.isNotEmpty()) {
                    ref.child("master_kategori").setValue(katMap)
                }

                val damageMap = damageReportsList.associateBy { it.id.toString() }
                if (damageMap.isNotEmpty()) {
                    ref.child("damage_reports").setValue(damageMap)
                }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to restore full state to RTDB: ${e.message}")
        }

        try {
            val fs = getFirestoreInstance()
            if (fs != null) {
                reports.forEach { item -> fs.collection("helpdesk_reports").document(item.id.toString()).set(item) }
                plans.forEach { item -> fs.collection("action_plans").document(item.id.toString()).set(item) }
                suratList.forEach { item -> fs.collection("surat_arsip").document(item.id.toString()).set(item) }
                peminjamanList.forEach { item -> fs.collection("peminjaman_makro").document(item.id.toString()).set(item) }
                ruangList.forEach { item -> fs.collection("master_ruang").document(item.id.toString()).set(item) }
                statusPenangananList.forEach { item -> fs.collection("master_status_penanganan").document(item.id.toString()).set(item) }
                urgensiList.forEach { item -> fs.collection("master_urgensi").document(item.id.toString()).set(item) }
                kategoriList.forEach { item -> fs.collection("master_kategori").document(item.id.toString()).set(item) }
                damageReportsList.forEach { item -> fs.collection("damage_reports").document(item.id.toString()).set(item) }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to restore full state to Firestore: ${e.message}")
        }
    }

    // --- PROJECT TASKS / TO-DO LIST ---
    val allProjectTasks: Flow<List<ProjectTask>> = dao.getAllProjectTasks()

    suspend fun insertProjectTask(task: ProjectTask): Long = dao.insertProjectTask(task)

    suspend fun updateProjectTask(task: ProjectTask) = dao.updateProjectTask(task)

    suspend fun deleteProjectTask(task: ProjectTask) = dao.deleteProjectTask(task)

    // --- CLEAR ALL OPERATIONAL / DUMMY DATA (FRESH START) ---
    suspend fun clearAllOperationalData() {
        dao.deleteAllHelpdeskReports()
        dao.deleteAllActionPlans()
        dao.deleteAllSuratArsip()
        dao.deleteAllPeminjamanMakro()
        dao.deleteAllDamageReports()
        dao.deleteAllProjectTasks()

        try {
            val ref = sarprasRef
            if (ref != null) {
                ref.child("helpdesk_reports").removeValue()
                ref.child("action_plans").removeValue()
                ref.child("surat_arsip").removeValue()
                ref.child("peminjaman_makro").removeValue()
                ref.child("damage_reports").removeValue()
                ref.child("project_tasks").removeValue()
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to clear operational data in RTDB: ${e.message}")
        }

        try {
            val fs = getFirestoreInstance()
            if (fs != null) {
                val collections = listOf("helpdesk_reports", "action_plans", "surat_arsip", "peminjaman_makro", "damage_reports", "project_tasks")
                collections.forEach { colName ->
                    fs.collection(colName).get().addOnSuccessListener { snapshot ->
                        snapshot.documents.forEach { doc -> doc.reference.delete() }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SarprasRepository", "Failed to clear operational data in Firestore: ${e.message}")
        }
    }

    // --- FIREBASE CONNECTION DIAGNOSTIC & MANUAL CLOUD SYNC ---
    fun testFirebaseConnection(onResult: (Boolean, String, Long) -> Unit) {
        externalScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                val listenerErr = realtimeListenerError
                if (!listenerErr.isNullOrBlank() && (listenerErr.contains("PERMISSION_DENIED") || listenerErr.contains("SECURITY RULES"))) {
                    withContext(Dispatchers.Main) {
                        onResult(false, listenerErr, 0)
                    }
                    return@launch
                }

                val ref = getDatabaseRef()

                if (ref == null) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "[INIT ERROR] Inisialisasi FirebaseDatabase belum siap / berkas google-services.json tidak valid.", 0)
                    }
                    return@launch
                }

                // Direct real write/ping test on Firebase Realtime Database
                ref.child("_connection_diagnostic")
                    .setValue(com.google.firebase.database.ServerValue.TIMESTAMP)
                    .addOnSuccessListener {
                        val latency = System.currentTimeMillis() - startTime
                        val finalLatency = if (latency <= 0) 12L else latency
                        isRealtimeListenerActive = true
                        realtimeListenerError = null
                        externalScope.launch(Dispatchers.Main) {
                            onResult(true, "Terhubung ke Firebase Realtime Database & Firestore - Sync Dual-Database Aktif.", finalLatency)
                        }
                    }
                    .addOnFailureListener { ex ->
                        val rawMsg = ex.localizedMessage ?: ex.message ?: "Unknown Firebase Error"
                        val formattedError = when {
                            rawMsg.contains("Permission denied", ignoreCase = true) || rawMsg.contains("PERMISSION_DENIED", ignoreCase = true) -> {
                                "[SECURITY RULES ERROR] Akses ditolak oleh Firebase Security Rules (Permission Denied). Buka Firebase Console -> Realtime Database -> Rules, pastikan node 'sarprasq' diset { \".read\": true, \".write\": true }."
                            }
                            rawMsg.contains("network", ignoreCase = true) || rawMsg.contains("unavailable", ignoreCase = true) -> {
                                "[NETWORK ERROR] Gagal terhubung ke server Firebase Console. Periksa koneksi internet atau ketersediaan jaringan."
                            }
                            else -> {
                                "[DIAGNOSTIC FAILURE] $rawMsg"
                            }
                        }
                        realtimeListenerError = formattedError
                        externalScope.launch(Dispatchers.Main) {
                            onResult(false, formattedError, 0)
                        }
                    }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Kesalahan koneksi jaringan atau konfigurasi Firebase."
                val formattedError = "[SYSTEM EXCEPTION] $errorMsg"
                realtimeListenerError = formattedError
                Log.e("SarprasRepository", "Exception in testFirebaseConnection: $errorMsg", e)
                withContext(Dispatchers.Main) {
                    onResult(false, formattedError, 0)
                }
            }
        }
    }

    fun syncAllToFirebase(onComplete: (Boolean, String) -> Unit) {
        externalScope.launch {
            try {
                val ref = getDatabaseRef()
                val fs = getFirestoreInstance()

                if (ref == null && fs == null) {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "[INIT ERROR] Firebase Database & Firestore tidak dapat diakses.")
                    }
                    return@launch
                }

                val reports = dao.getAllHelpdeskReportsList()
                val plans = dao.getAllActionPlansList()
                val suratList = dao.getAllSuratArsipList()
                val peminjamanList = dao.getAllPeminjamanMakroList()
                val damageReportsList = dao.getAllDamageReportsList()

                // 1. Write to RTDB
                if (ref != null) {
                    val helpdeskMap = reports.associateBy { it.id.toString() }
                    if (helpdeskMap.isNotEmpty()) ref.child("helpdesk_reports").setValue(helpdeskMap)

                    val planMap = plans.associateBy { it.id.toString() }
                    if (planMap.isNotEmpty()) ref.child("action_plans").setValue(planMap)

                    val suratMap = suratList.associateBy { it.id.toString() }
                    if (suratMap.isNotEmpty()) ref.child("surat_arsip").setValue(suratMap)

                    val peminjamanMap = peminjamanList.associateBy { it.id.toString() }
                    if (peminjamanMap.isNotEmpty()) ref.child("peminjaman_makro").setValue(peminjamanMap)

                    val damageMap = damageReportsList.associateBy { it.id.toString() }
                    if (damageMap.isNotEmpty()) ref.child("damage_reports").setValue(damageMap)
                }

                // 2. Write to Cloud Firestore
                if (fs != null) {
                    reports.forEach { item -> fs.collection("helpdesk_reports").document(item.id.toString()).set(item) }
                    plans.forEach { item -> fs.collection("action_plans").document(item.id.toString()).set(item) }
                    suratList.forEach { item -> fs.collection("surat_arsip").document(item.id.toString()).set(item) }
                    peminjamanList.forEach { item -> fs.collection("peminjaman_makro").document(item.id.toString()).set(item) }
                    damageReportsList.forEach { item -> fs.collection("damage_reports").document(item.id.toString()).set(item) }
                }

                withContext(Dispatchers.Main) {
                    onComplete(true, "Data lokal berhasil disinkronkan ke Realtime Database & Cloud Firestore!")
                }
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Gagal menyinkronkan data ke Firebase."
                Log.e("SarprasRepository", "Exception in syncAllToFirebase: $err", e)
                withContext(Dispatchers.Main) {
                    onComplete(false, "[SYNC ERROR] $err")
                }
            }
        }
    }
}
