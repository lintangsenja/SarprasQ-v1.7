package com.lintang.sarprasq.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import com.lintang.sarprasq.util.CompressedImageResult
import com.lintang.sarprasq.util.ImageCompressor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lintang.sarprasq.data.local.SarprasDatabase
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.DamageReport
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.calculateCalculatedCategory
import com.lintang.sarprasq.data.model.calculateCalculatedUrgency
import com.lintang.sarprasq.data.model.formatCombinedDamageDeskripsi
import com.lintang.sarprasq.data.model.parseDamageItemsFromReport
import com.lintang.sarprasq.data.model.KategoriMaster
import com.lintang.sarprasq.data.model.SubKategoriMaster
import com.lintang.sarprasq.data.model.PeminjamanMakro
import com.lintang.sarprasq.data.model.ProjectTask
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.data.model.SatuanMaster
import com.lintang.sarprasq.data.model.StatusPenanganan
import com.lintang.sarprasq.data.model.SuratArsip
import com.lintang.sarprasq.data.model.UrgensiMaster
import com.lintang.sarprasq.data.repository.SarprasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RoomIncidentItemInput(
    val namaItemKerusakan: String = "",
    val kategori: String = "Elektronik & Audio Visual",
    val urgensi: String = "Sedang",
    val keterangan: String = "",
    val fotoUrl: String? = null
)

data class DashboardStats(
    val totalLaporan: Int = 0,
    val laporanSelesai: Int = 0,
    val laporanPending: Int = 0,
    val agendaProgres: Int = 0
)

data class ImageUploadState(
    val isUploading: Boolean = false,
    val progressMessage: String = "",
    val downloadUrl: String? = null,
    val compressedInfo: CompressedImageResult? = null,
    val errorMessage: String? = null
)

class SarprasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SarprasRepository
    private val prefs = application.getSharedPreferences("sarprasq_prefs", Context.MODE_PRIVATE)

    val imageUploadState = MutableStateFlow(ImageUploadState())

    fun uploadProofPhoto(
        context: Context,
        imageUri: Uri,
        folder: String = "bukti_kerusakan",
        onSuccess: (String, CompressedImageResult) -> Unit = { _, _ -> },
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            imageUploadState.value = ImageUploadState(
                isUploading = true,
                progressMessage = "Mengompresi gambar (max 400 KB) & memproses EXIF..."
            )
            val result = repository.uploadProofPhoto(context, imageUri, folder)
            result.onSuccess { (url, compressed) ->
                imageUploadState.value = ImageUploadState(
                    isUploading = false,
                    downloadUrl = url,
                    compressedInfo = compressed
                )
                onSuccess(url, compressed)
            }.onFailure { exception ->
                val err = exception.localizedMessage ?: "Gagal mengunggah foto ke Firebase Storage"
                imageUploadState.value = ImageUploadState(
                    isUploading = false,
                    errorMessage = err
                )
                onError(err)
            }
        }
    }

    fun clearImageUploadState() {
        imageUploadState.value = ImageUploadState()
    }

    // Helper to initialize and migrate profile preferences
    private fun getInitialPref(key: String, legacyDefault: String, newDefault: String): String {
        val valInPref = prefs.getString(key, null)
        return if (valInPref == null || valInPref == legacyDefault) {
            prefs.edit().putString(key, newDefault).apply()
            newDefault
        } else {
            valInPref
        }
    }

    // --- Profile Info ---
    val namaPetugas = MutableStateFlow(
        getInitialPref("petugas_name", "Lintang Prasetyo, S.Pd.", "Kevin Ricky Utama, S.Kom.")
    )
    val nipPetugas = MutableStateFlow(
        getInitialPref("petugas_nip", "19880512 201203 1 005", "1904982025211035")
    )
    val namaSekolah = MutableStateFlow(
        getInitialPref("sekolah_name", "SMA Negeri 1 Banjarnegara", "SMA Negeri 1 Bobotsari")
    )
    val namaProgram = MutableStateFlow(
        prefs.getString("program_name", "SarprasQ - Sistem Manajemen Sarana & Prasarana") ?: "SarprasQ - Sistem Manajemen Sarana & Prasarana"
    )
    val profileImagePath = MutableStateFlow(prefs.getString("profile_image_path", null))

    // --- Last Backup Info & Records ---
    val lastBackupInfo = MutableStateFlow(prefs.getString("last_backup_time", "Belum pernah backup") ?: "Belum pernah backup")
    val backupHistoryRecords = MutableStateFlow<List<BackupHistoryRecord>>(emptyList())

    // --- Cloud Sync & Connection Diagnostic ---
    val firebaseConnStatus = MutableStateFlow<FirebaseConnState>(FirebaseConnState.Idle)
    val lastCloudSyncTime = MutableStateFlow(
        prefs.getString("last_cloud_sync_time", "Belum pernah disinkronkan") ?: "Belum pernah disinkronkan"
    )
    val lastSyncTime: kotlinx.coroutines.flow.StateFlow<String> = lastCloudSyncTime
    val isSyncing = MutableStateFlow(false)

    init {
        val database = SarprasDatabase.getDatabase(application, viewModelScope)
        repository = SarprasRepository(database.sarprasDao())
        loadBackupHistoryRecords()

        // Fresh Start Purge: Ensure all legacy sample/dummy operational data is purged once
        val isFreshStartDone = prefs.getBoolean("fresh_start_done_v3", false)
        if (!isFreshStartDone) {
            viewModelScope.launch {
                repository.clearAllOperationalData()
                prefs.edit()
                    .remove("backup_history_records")
                    .remove("last_backup_time")
                    .putBoolean("fresh_start_done_v3", true)
                    .apply()
                backupHistoryRecords.value = emptyList()
                lastBackupInfo.value = "Belum pernah backup"
            }
        }
    }

    fun clearAllOperationalData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.clearAllOperationalData()
            prefs.edit()
                .remove("backup_history_records")
                .remove("last_backup_time")
                .apply()
            backupHistoryRecords.value = emptyList()
            lastBackupInfo.value = "Belum pernah backup"
            onComplete?.invoke()
        }
    }

    private fun loadBackupHistoryRecords() {
        val rawJson = prefs.getString("backup_history_records", null) ?: return
        try {
            val arr = JSONArray(rawJson)
            val list = mutableListOf<BackupHistoryRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    BackupHistoryRecord(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        timestamp = obj.optString("timestamp", ""),
                        totalRecords = obj.optInt("totalRecords", 0),
                        jsonString = obj.optString("jsonString", "")
                    )
                )
            }
            backupHistoryRecords.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveBackupHistoryRecords(records: List<BackupHistoryRecord>) {
        backupHistoryRecords.value = records
        try {
            val arr = JSONArray()
            for (r in records) {
                val obj = JSONObject()
                obj.put("id", r.id)
                obj.put("timestamp", r.timestamp)
                obj.put("totalRecords", r.totalRecords)
                obj.put("jsonString", r.jsonString)
                arr.put(obj)
            }
            prefs.edit().putString("backup_history_records", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteBackupHistoryRecord(id: String) {
        val current = backupHistoryRecords.value.filter { it.id != id }
        saveBackupHistoryRecords(current)
    }

    fun addBackupHistoryRecord(timestamp: String, totalRecords: Int, jsonString: String) {
        val newRecord = BackupHistoryRecord(
            timestamp = timestamp,
            totalRecords = totalRecords,
            jsonString = jsonString
        )
        val current = listOf(newRecord) + backupHistoryRecords.value
        saveBackupHistoryRecords(current)
    }

    // --- Firebase Connection Diagnostic & Cloud Sync ---
    fun testFirebaseConnection() {
        viewModelScope.launch {
            try {
                firebaseConnStatus.value = FirebaseConnState.Testing
                repository.testFirebaseConnection { isSuccess, msg, latency ->
                    val nowStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID")).format(java.util.Date()) + " WIB"
                    if (isSuccess) {
                        firebaseConnStatus.value = FirebaseConnState.Success(
                            message = "Status: Terhubung ke Firebase Console - Aman",
                            endpoint = "https://sarpras-134d7-default-rtdb.asia-southeast1.firebasedatabase.app",
                            latencyMs = latency,
                            timestamp = nowStr
                        )
                    } else {
                        val statusMsg = if (msg.contains("TIMEOUT", ignoreCase = true)) {
                            "Koneksi Timeout / Gagal Terhubung"
                        } else {
                            "Status: Terputus / Gangguan Koneksi Firebase"
                        }
                        firebaseConnStatus.value = FirebaseConnState.Error(
                            message = statusMsg,
                            errorDetail = msg
                        )
                    }
                }
            } catch (e: Exception) {
                firebaseConnStatus.value = FirebaseConnState.Error(
                    message = "Status: Terputus / Gangguan Koneksi Firebase",
                    errorDetail = e.localizedMessage ?: "Terjadi kesalahan jaringan atau sistem."
                )
            }
        }
    }

    fun manualSyncToFirestore(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            isSyncing.value = true
            try {
                repository.syncAllToFirebase { success, message ->
                    if (success) {
                        val nowStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID")).format(java.util.Date()) + " WIB"
                        prefs.edit().putString("last_cloud_sync_time", nowStr).apply()
                        lastCloudSyncTime.value = nowStr
                    }
                    isSyncing.value = false
                    onComplete?.invoke(success, message)
                }
            } catch (e: Exception) {
                isSyncing.value = false
                val errMsg = e.localizedMessage ?: "Gagal menyinkronkan data ke Firebase."
                onComplete?.invoke(false, errMsg)
            }
        }
    }

    fun triggerManualCloudSync(onComplete: (Boolean, String) -> Unit) {
        manualSyncToFirestore { success, message ->
            onComplete(success, message)
        }
    }

    fun updateProfileInfo(name: String, nip: String, sekolah: String, program: String) {
        namaPetugas.value = name
        nipPetugas.value = nip
        namaSekolah.value = sekolah
        namaProgram.value = program
        prefs.edit()
            .putString("petugas_name", name)
            .putString("petugas_nip", nip)
            .putString("sekolah_name", sekolah)
            .putString("program_name", program)
            .apply()
    }

    fun updateProfileImage(path: String?) {
        profileImagePath.value = path
        prefs.edit()
            .putString("profile_image_path", path)
            .apply()
    }

    // --- Search & Filters ---
    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow("Semua") // "Semua", "Darurat", "Pending", "Proses", "Selesai"

    // --- Flows ---
    val allReports: StateFlow<List<HelpdeskReport>> = repository.allHelpdeskReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredReports: StateFlow<List<HelpdeskReport>> = combine(
        allReports,
        searchQuery,
        selectedStatusFilter
    ) { reports, query, filter ->
        reports.filter { r ->
            val matchesQuery = query.isEmpty() ||
                    r.pelapor.contains(query, ignoreCase = true) ||
                    r.lokasi.contains(query, ignoreCase = true) ||
                    r.deskripsi.contains(query, ignoreCase = true) ||
                    r.tindakan.contains(query, ignoreCase = true)

            val matchesFilter = when {
                filter.isBlank() || filter.equals("Semua", ignoreCase = true) || filter.equals("Semua Status", ignoreCase = true) -> {
                    // Helpdesk Utama: Default display shows incoming/unhandled queue items AND "Catat" items.
                    // Items processed into "Proses", "Diproses", "Pending", "Selesai", "Ditolak" move to Pending/Proses screen.
                    // "Catat" status stays in Helpdesk Utama for early recording/recap without moving to field execution.
                    val isHandledAndMoved = r.status.equals("Proses", ignoreCase = true) ||
                            r.status.equals("Diproses", ignoreCase = true) ||
                            r.status.equals("Pending", ignoreCase = true) ||
                            r.status.equals("Selesai", ignoreCase = true) ||
                            r.status.equals("Ditolak", ignoreCase = true)
                    !isHandledAndMoved
                }
                filter.equals("Catat", ignoreCase = true) -> r.status.equals("Catat", ignoreCase = true)
                filter.equals("Darurat", ignoreCase = true) -> r.urgensi.equals("Darurat", ignoreCase = true) || r.status.equals("Darurat", ignoreCase = true)
                filter.equals("Diproses", ignoreCase = true) -> r.status.equals("Proses", ignoreCase = true) || r.status.equals("Diproses", ignoreCase = true)
                else -> r.status.equals(filter, ignoreCase = true) || r.status.contains(filter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReports: StateFlow<List<HelpdeskReport>> = repository.pendingReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val actionPlans: StateFlow<List<ActionPlan>> = repository.allActionPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suratArsipList: StateFlow<List<SuratArsip>> = repository.allSuratArsip
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val peminjamanList: StateFlow<List<PeminjamanMakro>> = repository.allPeminjamanMakro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Master Data Flows ---
    val allRuang: StateFlow<List<Ruang>> = repository.allRuang
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStatusPenanganan: StateFlow<List<StatusPenanganan>> = repository.allStatusPenanganan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUrgensi: StateFlow<List<UrgensiMaster>> = repository.allUrgensi
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKategori: StateFlow<List<KategoriMaster>> = repository.allKategori
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSatuan: StateFlow<List<SatuanMaster>> = repository.allSatuan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubKategori: StateFlow<List<SubKategoriMaster>> = repository.allSubKategori
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDamageReports: StateFlow<List<DamageReport>> = repository.allDamageReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjectTasks: StateFlow<List<ProjectTask>> = repository.allProjectTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dashboard Stats ---
    val dashboardStats: StateFlow<DashboardStats> = combine(
        allReports,
        actionPlans
    ) { reports, plans ->
        DashboardStats(
            totalLaporan = reports.size,
            laporanSelesai = reports.count { it.status.equals("Selesai", ignoreCase = true) },
            laporanPending = reports.count { it.status.equals("Pending", ignoreCase = true) },
            agendaProgres = plans.count { !it.statusProgres.equals("Selesai", ignoreCase = true) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // --- Actions: Helpdesk ---
    fun addHelpdeskReport(
        pelapor: String,
        lokasi: String,
        deskripsi: String,
        urgensi: String,
        status: String,
        tindakan: String,
        alasanPending: String? = null,
        estimasiEksekusi: String? = null,
        tanggal: String = "31/07/2026",
        kategori: String = "Elektronik & Audio Visual",
        fotoUrl: String? = null,
        catatan: String? = null,
        petugas: String? = null
    ) {
        viewModelScope.launch {
            val report = HelpdeskReport(
                tanggal = tanggal,
                pelapor = pelapor,
                lokasi = lokasi,
                deskripsi = deskripsi,
                urgensi = urgensi,
                status = status,
                tindakan = tindakan,
                alasanPending = if (status.equals("Pending", ignoreCase = true)) alasanPending else null,
                estimasiEksekusi = if (status.equals("Pending", ignoreCase = true)) estimasiEksekusi else null,
                kategori = kategori,
                fotoUrl = fotoUrl,
                catatan = catatan,
                petugas = petugas,
                timestamp = System.currentTimeMillis()
            )
            repository.insertHelpdeskReport(report)
        }
    }

    fun updateHelpdeskReport(report: HelpdeskReport) {
        viewModelScope.launch {
            repository.updateHelpdeskReport(report)
        }
    }

    fun updateHelpdeskReportStatus(
        report: HelpdeskReport,
        newStatus: String,
        newTindakan: String = report.tindakan,
        newAlasanPending: String? = report.alasanPending,
        newEstimasi: String? = report.estimasiEksekusi
    ) {
        viewModelScope.launch {
            val updated = report.copy(
                status = newStatus,
                tindakan = newTindakan,
                alasanPending = if (newStatus.equals("Pending", ignoreCase = true)) newAlasanPending else null,
                estimasiEksekusi = if (newStatus.equals("Pending", ignoreCase = true)) newEstimasi else null,
                progresPersen = if (newStatus.equals("Selesai", ignoreCase = true)) 100 else report.progresPersen
            )
            repository.updateHelpdeskReport(updated)
        }
    }

    fun processPartialHelpdeskReportItems(
        originalReport: HelpdeskReport,
        selectedIndices: Set<Int>,
        newStatus: String,
        newTindakan: String = "-",
        newAlasanPending: String? = null,
        newEstimasi: String? = null,
        newPetugas: String? = originalReport.petugas
    ) {
        viewModelScope.launch {
            val allItems = parseDamageItemsFromReport(originalReport)
            if (selectedIndices.isEmpty() || allItems.isEmpty()) return@launch

            if (selectedIndices.size >= allItems.size) {
                // All items selected -> update original report status directly
                val updated = originalReport.copy(
                    status = newStatus,
                    tindakan = if (newTindakan.isNotBlank()) newTindakan else originalReport.tindakan,
                    alasanPending = if (newStatus.equals("Pending", ignoreCase = true)) newAlasanPending else null,
                    estimasiEksekusi = if (newStatus.equals("Pending", ignoreCase = true)) newEstimasi else null,
                    petugas = newPetugas ?: originalReport.petugas,
                    progresPersen = if (newStatus.equals("Selesai", ignoreCase = true)) 100 else originalReport.progresPersen
                )
                repository.updateHelpdeskReport(updated)
            } else {
                // Partial items selected -> split into two separate reports
                val selectedItems = allItems.filterIndexed { index, _ -> index in selectedIndices }
                val remainingItems = allItems.filterIndexed { index, _ -> index !in selectedIndices }

                // 1. Insert new report for selected items with target status
                val selectedDeskripsi = formatCombinedDamageDeskripsi(selectedItems)
                val selectedUrgent = calculateCalculatedUrgency(selectedItems)
                val selectedCategory = calculateCalculatedCategory(selectedItems)

                val newReport = HelpdeskReport(
                    tanggal = originalReport.tanggal,
                    pelapor = originalReport.pelapor,
                    lokasi = originalReport.lokasi,
                    deskripsi = selectedDeskripsi,
                    urgensi = selectedUrgent,
                    status = newStatus,
                    tindakan = if (newTindakan.isNotBlank()) newTindakan else "-",
                    alasanPending = if (newStatus.equals("Pending", ignoreCase = true)) newAlasanPending else null,
                    estimasiEksekusi = if (newStatus.equals("Pending", ignoreCase = true)) newEstimasi else null,
                    kategori = selectedCategory,
                    fotoUrl = originalReport.fotoUrl,
                    catatan = originalReport.catatan,
                    petugas = newPetugas ?: originalReport.petugas,
                    progresPersen = if (newStatus.equals("Selesai", ignoreCase = true)) 100 else 0,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertHelpdeskReport(newReport)

                // 2. Update original report with remaining items in queue
                val remainingDeskripsi = formatCombinedDamageDeskripsi(remainingItems)
                val remainingUrgent = calculateCalculatedUrgency(remainingItems)
                val remainingCategory = calculateCalculatedCategory(remainingItems)

                val updatedOriginal = originalReport.copy(
                    deskripsi = remainingDeskripsi,
                    urgensi = remainingUrgent,
                    kategori = remainingCategory
                )
                repository.updateHelpdeskReport(updatedOriginal)
            }
        }
    }

    fun updateHelpdeskProgress(
        report: HelpdeskReport,
        newPercentage: Int,
        note: String,
        date: String
    ) {
        viewModelScope.launch {
            val cleanNote = if (note.isBlank()) "Pembaruan progres pengerjaan" else note
            val newEntry = "$date#$newPercentage#$cleanNote"
            val updatedLog = if (report.riwayatProgres.isBlank()) {
                newEntry
            } else {
                "${report.riwayatProgres};$newEntry"
            }
            val newStatus = when {
                newPercentage >= 100 -> "Selesai"
                newPercentage > 0 && report.status.equals("Ditolak", ignoreCase = true) -> "Proses"
                newPercentage > 0 && report.status.equals("Pending", ignoreCase = true) -> "Proses"
                else -> report.status
            }

            val updated = report.copy(
                progresPersen = newPercentage,
                riwayatProgres = updatedLog,
                status = newStatus
            )
            repository.updateHelpdeskReport(updated)
        }
    }

    fun deleteHelpdeskReport(report: HelpdeskReport) {
        viewModelScope.launch {
            repository.deleteHelpdeskReport(report)
        }
    }

    // --- Actions: Action Plan ---
    fun addActionPlan(
        agenda: String,
        jenisRencana: String = "Rencana Perbaikan / Perawatan",
        kategori: String,
        targetWaktu: String,
        statusProgres: String = "Belum Mulai",
        tanggalMulai: String = "-",
        tanggalRealisasi: String = "-",
        pelaksana: String = "Internal",
        namaTukang: String? = null,
        nomorWhatsapp: String? = null,
        detailAset: String? = null,
        lokasiRuang: String? = null,
        tingkatKerusakan: String? = null,
        spesifikasi: String? = null,
        jumlahSatuan: String? = null,
        estimasiAnggaran: String? = null,
        fotoUrl: String? = null
    ) {
        viewModelScope.launch {
            val plan = ActionPlan(
                agenda = agenda,
                jenisRencana = jenisRencana,
                kategori = kategori,
                targetWaktu = targetWaktu,
                tanggalMulai = tanggalMulai,
                tanggalRealisasi = tanggalRealisasi,
                statusProgres = statusProgres,
                pelaksana = pelaksana,
                namaTukang = namaTukang,
                nomorWhatsapp = nomorWhatsapp,
                detailAset = detailAset,
                lokasiRuang = lokasiRuang,
                tingkatKerusakan = tingkatKerusakan,
                spesifikasi = spesifikasi,
                jumlahSatuan = jumlahSatuan,
                estimasiAnggaran = estimasiAnggaran,
                fotoUrl = fotoUrl,
                timestamp = System.currentTimeMillis()
            )
            repository.insertActionPlan(plan)
        }
    }

    fun startActionPlan(
        plan: ActionPlan,
        tanggalMulai: String,
        pelaksana: String,
        namaTukang: String? = null,
        nomorWhatsapp: String? = null
    ) {
        viewModelScope.launch {
            val updated = plan.copy(
                statusProgres = "Dalam Proses",
                tanggalMulai = tanggalMulai,
                pelaksana = pelaksana,
                namaTukang = namaTukang?.ifBlank { null },
                nomorWhatsapp = nomorWhatsapp?.ifBlank { null }
            )
            repository.updateActionPlan(updated)
        }
    }

    fun updateActionPlan(plan: ActionPlan) {
        viewModelScope.launch {
            repository.updateActionPlan(plan)
        }
    }

    fun updateActionPlanStatus(plan: ActionPlan, newStatus: String, realisasiDate: String = "-") {
        viewModelScope.launch {
            val updated = plan.copy(
                statusProgres = newStatus,
                tanggalRealisasi = if (newStatus.equals("Selesai", ignoreCase = true) && realisasiDate == "-") "31/07/2026" else realisasiDate
            )
            repository.updateActionPlan(updated)
        }
    }

    fun deleteActionPlan(plan: ActionPlan) {
        viewModelScope.launch {
            repository.deleteActionPlan(plan)
        }
    }

    // --- Actions: Surat Arsip ---
    fun addSuratArsip(
        nomorSurat: String,
        tanggalSurat: String,
        perihal: String,
        jenisSurat: String,
        statusArsip: String
    ) {
        viewModelScope.launch {
            val surat = SuratArsip(
                nomorSurat = nomorSurat,
                tanggalSurat = tanggalSurat,
                perihal = perihal,
                jenisSurat = jenisSurat,
                statusArsip = statusArsip,
                timestamp = System.currentTimeMillis()
            )
            repository.insertSuratArsip(surat)
        }
    }

    fun deleteSuratArsip(surat: SuratArsip) {
        viewModelScope.launch {
            repository.deleteSuratArsip(surat)
        }
    }

    // --- Actions: Peminjaman Makro ---
    fun addPeminjamanMakro(
        bulanTahun: String,
        namaBarang: String,
        jumlahPeminjaman: Int,
        kondisi: String
    ) {
        viewModelScope.launch {
            val item = PeminjamanMakro(
                bulanTahun = bulanTahun,
                namaBarang = namaBarang,
                jumlahPeminjaman = jumlahPeminjaman,
                kondisi = kondisi,
                timestamp = System.currentTimeMillis()
            )
            repository.insertPeminjamanMakro(item)
        }
    }

    fun deletePeminjamanMakro(item: PeminjamanMakro) {
        viewModelScope.launch {
            repository.deletePeminjamanMakro(item)
        }
    }

    // --- Actions: Master Data Ruang ---
    fun addRuang(kodeRuang: String, namaRuang: String, kategori: String, penanggungJawab: String) {
        viewModelScope.launch {
            val ruang = Ruang(
                kodeRuang = kodeRuang,
                namaRuang = namaRuang,
                kategori = kategori,
                penanggungJawab = penanggungJawab,
                timestamp = System.currentTimeMillis()
            )
            repository.insertRuang(ruang)
        }
    }

    fun updateRuang(ruang: Ruang) {
        viewModelScope.launch {
            repository.updateRuang(ruang)
        }
    }

    fun deleteRuang(ruang: Ruang) {
        viewModelScope.launch {
            repository.deleteRuang(ruang)
        }
    }

    fun resetDefaultRuang() {
        viewModelScope.launch {
            repository.resetDefaultRuang()
        }
    }

    // --- Actions: Master Data Status Penanganan ---
    fun addStatusPenanganan(namaStatus: String, deskripsi: String) {
        viewModelScope.launch {
            val status = StatusPenanganan(
                namaStatus = namaStatus,
                deskripsi = deskripsi,
                timestamp = System.currentTimeMillis()
            )
            repository.insertStatusPenanganan(status)
        }
    }

    fun updateStatusPenanganan(status: StatusPenanganan) {
        viewModelScope.launch {
            repository.updateStatusPenanganan(status)
        }
    }

    fun deleteStatusPenanganan(status: StatusPenanganan) {
        viewModelScope.launch {
            repository.deleteStatusPenanganan(status)
        }
    }

    fun resetDefaultStatusPenanganan() {
        viewModelScope.launch {
            repository.resetDefaultStatusPenanganan()
        }
    }

    // --- Actions: Master Data Urgensi ---
    fun addUrgensi(namaUrgensi: String, deskripsi: String) {
        viewModelScope.launch {
            val urgensi = UrgensiMaster(
                namaUrgensi = namaUrgensi,
                deskripsi = deskripsi,
                timestamp = System.currentTimeMillis()
            )
            repository.insertUrgensi(urgensi)
        }
    }

    fun updateUrgensi(urgensi: UrgensiMaster) {
        viewModelScope.launch {
            repository.updateUrgensi(urgensi)
        }
    }

    fun deleteUrgensi(urgensi: UrgensiMaster) {
        viewModelScope.launch {
            repository.deleteUrgensi(urgensi)
        }
    }

    fun resetDefaultUrgensi() {
        viewModelScope.launch {
            repository.resetDefaultUrgensi()
        }
    }

    // --- Actions: Master Data Kategori ---
    fun addKategori(namaKategori: String, deskripsi: String = "") {
        viewModelScope.launch {
            val kategori = KategoriMaster(
                namaKategori = namaKategori,
                deskripsi = deskripsi,
                timestamp = System.currentTimeMillis()
            )
            repository.insertKategori(kategori)
        }
    }

    fun updateKategori(kategori: KategoriMaster) {
        viewModelScope.launch {
            repository.updateKategori(kategori)
        }
    }

    fun deleteKategori(kategori: KategoriMaster) {
        viewModelScope.launch {
            repository.deleteKategori(kategori)
        }
    }

    fun resetDefaultKategori() {
        viewModelScope.launch {
            repository.resetDefaultKategori()
        }
    }

    // --- Actions: Master Data Satuan ---
    fun addSatuan(namaSatuan: String, deskripsi: String) {
        viewModelScope.launch {
            val satuan = SatuanMaster(
                namaSatuan = namaSatuan,
                deskripsi = deskripsi,
                timestamp = System.currentTimeMillis()
            )
            repository.insertSatuan(satuan)
        }
    }

    fun updateSatuan(satuan: SatuanMaster) {
        viewModelScope.launch {
            repository.updateSatuan(satuan)
        }
    }

    fun deleteSatuan(satuan: SatuanMaster) {
        viewModelScope.launch {
            repository.deleteSatuan(satuan)
        }
    }

    fun resetDefaultSatuan() {
        viewModelScope.launch {
            repository.resetDefaultSatuan()
        }
    }

    // --- Actions: Master Data Sub-Kategori / Sifat Pekerjaan ---
    fun addSubKategori(namaSubKategori: String, deskripsi: String = "") {
        viewModelScope.launch {
            val subKategori = SubKategoriMaster(
                namaSubKategori = namaSubKategori,
                deskripsi = deskripsi,
                timestamp = System.currentTimeMillis()
            )
            repository.insertSubKategori(subKategori)
        }
    }

    fun updateSubKategori(subKategori: SubKategoriMaster) {
        viewModelScope.launch {
            repository.updateSubKategori(subKategori)
        }
    }

    fun deleteSubKategori(subKategori: SubKategoriMaster) {
        viewModelScope.launch {
            repository.deleteSubKategori(subKategori)
        }
    }

    fun resetDefaultSubKategori() {
        viewModelScope.launch {
            repository.resetDefaultSubKategori()
        }
    }

    // --- Actions: Room Damage Reports (Multi-Item) ---
    fun addDamageReport(
        roomId: Int,
        namaRuang: String,
        namaPelapor: String,
        namaItemKerusakan: String,
        tanggalLapor: String,
        statusPenanganan: String = "Segera ditangani",
        ditanganiOleh: String = "Internal",
        keterangan: String = "",
        kategori: String = "Elektronik & Audio Visual",
        urgensi: String = "Sedang",
        fotoUrl: String? = null
    ) {
        viewModelScope.launch {
            val report = DamageReport(
                roomId = roomId,
                namaRuang = namaRuang,
                namaPelapor = namaPelapor,
                namaItemKerusakan = namaItemKerusakan,
                tanggalLapor = tanggalLapor,
                statusPenanganan = statusPenanganan,
                ditanganiOleh = ditanganiOleh,
                tanggalSelesai = if (statusPenanganan.contains("Selesai", ignoreCase = true)) "01/08/2026" else "-",
                keterangan = keterangan,
                kategori = kategori,
                urgensi = urgensi,
                fotoUrl = fotoUrl,
                timestamp = System.currentTimeMillis()
            )
            repository.insertDamageReport(report)
        }
    }

    fun addMultiDamageReport(
        roomId: Int,
        namaRuang: String,
        namaPelapor: String,
        tanggalLapor: String,
        ditanganiOleh: String = "Internal",
        statusPenanganan: String = "Segera ditangani",
        items: List<RoomIncidentItemInput>
    ) {
        viewModelScope.launch {
            items.forEachIndexed { index, item ->
                if (item.namaItemKerusakan.isNotBlank()) {
                    val report = DamageReport(
                        roomId = roomId,
                        namaRuang = namaRuang,
                        namaPelapor = namaPelapor,
                        namaItemKerusakan = item.namaItemKerusakan,
                        tanggalLapor = tanggalLapor,
                        statusPenanganan = statusPenanganan,
                        ditanganiOleh = ditanganiOleh,
                        tanggalSelesai = if (statusPenanganan.contains("Selesai", ignoreCase = true)) "01/08/2026" else "-",
                        keterangan = item.keterangan,
                        kategori = item.kategori,
                        urgensi = item.urgensi,
                        fotoUrl = item.fotoUrl,
                        timestamp = System.currentTimeMillis() + index
                    )
                    repository.insertDamageReport(report)
                }
            }
        }
    }

    fun updateDamageReportStatus(
        report: DamageReport,
        newStatus: String,
        newDitanganiOleh: String,
        newTanggalSelesai: String,
        newKeterangan: String
    ) {
        viewModelScope.launch {
            val updated = report.copy(
                statusPenanganan = newStatus,
                ditanganiOleh = newDitanganiOleh,
                tanggalSelesai = newTanggalSelesai,
                keterangan = newKeterangan
            )
            repository.updateDamageReport(updated)
        }
    }

    fun deleteDamageReport(report: DamageReport) {
        viewModelScope.launch {
            repository.deleteDamageReport(report)
        }
    }

    fun seedDummyData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.seedDummyData()
            onComplete()
        }
    }

    // --- BACKUP & RESTORE LOGIC ---
    fun exportBackupJson(
        context: Context,
        onComplete: (String, File?) -> Unit
    ) {
        viewModelScope.launch {
            val helpdeskList = repository.getAllHelpdeskReportsList()
            val actionList = repository.getAllActionPlansList()
            val suratList = repository.getAllSuratArsipList()
            val peminjamanList = repository.getAllPeminjamanMakroList()
            val ruangList = repository.getAllRuangList()
            val statusList = repository.getAllStatusPenangananList()
            val urgensiList = repository.getAllUrgensiList()
            val kategoriList = repository.getAllKategoriList()
            val damageList = repository.getAllDamageReportsList()

            val rootJson = JSONObject()
            rootJson.put("app", "SarprasQ")
            rootJson.put("version", 3)

            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))
            val formattedTime = dateFormat.format(Date())
            rootJson.put("exportedAt", formattedTime)

            // Helpdesk
            val helpdeskArr = JSONArray()
            for (h in helpdeskList) {
                val obj = JSONObject()
                obj.put("id", h.id)
                obj.put("tanggal", h.tanggal)
                obj.put("pelapor", h.pelapor)
                obj.put("lokasi", h.lokasi)
                obj.put("deskripsi", h.deskripsi)
                obj.put("urgensi", h.urgensi)
                obj.put("status", h.status)
                obj.put("tindakan", h.tindakan)
                obj.put("alasanPending", h.alasanPending ?: "")
                obj.put("estimasiEksekusi", h.estimasiEksekusi ?: "")
                obj.put("timestamp", h.timestamp)
                helpdeskArr.put(obj)
            }
            rootJson.put("helpdeskReports", helpdeskArr)

            // Action Plans
            val actionArr = JSONArray()
            for (a in actionList) {
                val obj = JSONObject()
                obj.put("id", a.id)
                obj.put("agenda", a.agenda)
                obj.put("kategori", a.kategori)
                obj.put("targetWaktu", a.targetWaktu)
                obj.put("tanggalRealisasi", a.tanggalRealisasi)
                obj.put("statusProgres", a.statusProgres)
                obj.put("timestamp", a.timestamp)
                actionArr.put(obj)
            }
            rootJson.put("actionPlans", actionArr)

            // Surat Arsip
            val suratArr = JSONArray()
            for (s in suratList) {
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("nomorSurat", s.nomorSurat)
                obj.put("tanggalSurat", s.tanggalSurat)
                obj.put("perihal", s.perihal)
                obj.put("jenisSurat", s.jenisSurat)
                obj.put("statusArsip", s.statusArsip)
                obj.put("timestamp", s.timestamp)
                suratArr.put(obj)
            }
            rootJson.put("suratArsip", suratArr)

            // Peminjaman Makro
            val peminjamanArr = JSONArray()
            for (p in peminjamanList) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("bulanTahun", p.bulanTahun)
                obj.put("namaBarang", p.namaBarang)
                obj.put("jumlahPeminjaman", p.jumlahPeminjaman)
                obj.put("kondisi", p.kondisi)
                obj.put("timestamp", p.timestamp)
                peminjamanArr.put(obj)
            }
            rootJson.put("peminjamanMakro", peminjamanArr)

            // Master Ruang
            val ruangArr = JSONArray()
            for (r in ruangList) {
                val obj = JSONObject()
                obj.put("id", r.id)
                obj.put("kodeRuang", r.kodeRuang)
                obj.put("namaRuang", r.namaRuang)
                obj.put("kategori", r.kategori)
                obj.put("penanggungJawab", r.penanggungJawab)
                obj.put("timestamp", r.timestamp)
                ruangArr.put(obj)
            }
            rootJson.put("masterRuang", ruangArr)

            // Master Status Penanganan
            val statusArr = JSONArray()
            for (st in statusList) {
                val obj = JSONObject()
                obj.put("id", st.id)
                obj.put("namaStatus", st.namaStatus)
                obj.put("deskripsi", st.deskripsi)
                obj.put("timestamp", st.timestamp)
                statusArr.put(obj)
            }
            rootJson.put("masterStatusPenanganan", statusArr)

            // Master Urgensi
            val urgensiArr = JSONArray()
            for (u in urgensiList) {
                val obj = JSONObject()
                obj.put("id", u.id)
                obj.put("namaUrgensi", u.namaUrgensi)
                obj.put("deskripsi", u.deskripsi)
                obj.put("timestamp", u.timestamp)
                urgensiArr.put(obj)
            }
            rootJson.put("masterUrgensi", urgensiArr)

            // Master Kategori
            val katArr = JSONArray()
            for (k in kategoriList) {
                val obj = JSONObject()
                obj.put("id", k.id)
                obj.put("namaKategori", k.namaKategori)
                obj.put("deskripsi", k.deskripsi)
                obj.put("timestamp", k.timestamp)
                katArr.put(obj)
            }
            rootJson.put("masterKategori", katArr)

            // Room Damage Reports (Multi-item per room)
            val damageArr = JSONArray()
            for (dm in damageList) {
                val obj = JSONObject()
                obj.put("id", dm.id)
                obj.put("roomId", dm.roomId)
                obj.put("namaRuang", dm.namaRuang)
                obj.put("namaPelapor", dm.namaPelapor)
                obj.put("namaItemKerusakan", dm.namaItemKerusakan)
                obj.put("tanggalLapor", dm.tanggalLapor)
                obj.put("statusPenanganan", dm.statusPenanganan)
                obj.put("ditanganiOleh", dm.ditanganiOleh)
                obj.put("tanggalSelesai", dm.tanggalSelesai)
                obj.put("keterangan", dm.keterangan)
                obj.put("timestamp", dm.timestamp)
                damageArr.put(obj)
            }
            rootJson.put("damageReports", damageArr)

            val jsonString = rootJson.toString(2)

            var backupFile: File? = null
            try {
                backupFile = File(context.cacheDir, "sarprasq_backup.json")
                backupFile.writeText(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            lastBackupInfo.value = formattedTime
            prefs.edit().putString("last_backup_time", formattedTime).apply()

            val totalRecordsCount = helpdeskList.size + actionList.size + suratList.size + peminjamanList.size + ruangList.size + statusList.size + urgensiList.size + kategoriList.size + damageList.size
            addBackupHistoryRecord(formattedTime, totalRecordsCount, jsonString)

            onComplete(jsonString, backupFile)
        }
    }

    fun restoreBackupJson(
        jsonContent: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val rootObj = JSONObject(jsonContent)

                val reports = mutableListOf<HelpdeskReport>()
                if (rootObj.has("helpdeskReports")) {
                    val arr = rootObj.getJSONArray("helpdeskReports")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        reports.add(
                            HelpdeskReport(
                                id = obj.optInt("id", 0),
                                tanggal = obj.optString("tanggal", "31/07/2026"),
                                pelapor = obj.optString("pelapor", ""),
                                lokasi = obj.optString("lokasi", ""),
                                deskripsi = obj.optString("deskripsi", ""),
                                urgensi = obj.optString("urgensi", "Biasa"),
                                status = obj.optString("status", "Pending"),
                                tindakan = obj.optString("tindakan", "-"),
                                alasanPending = obj.optString("alasanPending").takeIf { !it.isNullOrEmpty() },
                                estimasiEksekusi = obj.optString("estimasiEksekusi").takeIf { !it.isNullOrEmpty() },
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val plans = mutableListOf<ActionPlan>()
                if (rootObj.has("actionPlans")) {
                    val arr = rootObj.getJSONArray("actionPlans")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        plans.add(
                            ActionPlan(
                                id = obj.optInt("id", 0),
                                agenda = obj.optString("agenda", ""),
                                kategori = obj.optString("kategori", "Umum"),
                                targetWaktu = obj.optString("targetWaktu", "-"),
                                tanggalRealisasi = obj.optString("tanggalRealisasi", "-"),
                                statusProgres = obj.optString("statusProgres", "Belum Mulai"),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val suratList = mutableListOf<SuratArsip>()
                if (rootObj.has("suratArsip")) {
                    val arr = rootObj.getJSONArray("suratArsip")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        suratList.add(
                            SuratArsip(
                                id = obj.optInt("id", 0),
                                nomorSurat = obj.optString("nomorSurat", ""),
                                tanggalSurat = obj.optString("tanggalSurat", ""),
                                perihal = obj.optString("perihal", ""),
                                jenisSurat = obj.optString("jenisSurat", "Masuk"),
                                statusArsip = obj.optString("statusArsip", "Tersimpan"),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val peminjamanList = mutableListOf<PeminjamanMakro>()
                if (rootObj.has("peminjamanMakro")) {
                    val arr = rootObj.getJSONArray("peminjamanMakro")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        peminjamanList.add(
                            PeminjamanMakro(
                                id = obj.optInt("id", 0),
                                bulanTahun = obj.optString("bulanTahun", ""),
                                namaBarang = obj.optString("namaBarang", ""),
                                jumlahPeminjaman = obj.optInt("jumlahPeminjaman", 1),
                                kondisi = obj.optString("kondisi", "Baik"),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val ruangList = mutableListOf<Ruang>()
                if (rootObj.has("masterRuang")) {
                    val arr = rootObj.getJSONArray("masterRuang")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        ruangList.add(
                            Ruang(
                                id = obj.optInt("id", 0),
                                kodeRuang = obj.optString("kodeRuang", ""),
                                namaRuang = obj.optString("namaRuang", ""),
                                kategori = obj.optString("kategori", "Ruang Kelas"),
                                penanggungJawab = obj.optString("penanggungJawab", "-"),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val statusList = mutableListOf<StatusPenanganan>()
                if (rootObj.has("masterStatusPenanganan")) {
                    val arr = rootObj.getJSONArray("masterStatusPenanganan")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        statusList.add(
                            StatusPenanganan(
                                id = obj.optInt("id", 0),
                                namaStatus = obj.optString("namaStatus", ""),
                                deskripsi = obj.optString("deskripsi", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val urgensiList = mutableListOf<UrgensiMaster>()
                if (rootObj.has("masterUrgensi")) {
                    val arr = rootObj.getJSONArray("masterUrgensi")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        urgensiList.add(
                            UrgensiMaster(
                                id = obj.optInt("id", 0),
                                namaUrgensi = obj.optString("namaUrgensi", ""),
                                deskripsi = obj.optString("deskripsi", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val kategoriList = mutableListOf<KategoriMaster>()
                if (rootObj.has("masterKategori")) {
                    val arr = rootObj.getJSONArray("masterKategori")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        kategoriList.add(
                            KategoriMaster(
                                id = obj.optInt("id", 0),
                                namaKategori = obj.optString("namaKategori", ""),
                                deskripsi = obj.optString("deskripsi", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val damageList = mutableListOf<DamageReport>()
                if (rootObj.has("damageReports")) {
                    val arr = rootObj.getJSONArray("damageReports")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        damageList.add(
                            DamageReport(
                                id = obj.optInt("id", 0),
                                roomId = obj.optInt("roomId", 0),
                                namaRuang = obj.optString("namaRuang", ""),
                                namaPelapor = obj.optString("namaPelapor", ""),
                                namaItemKerusakan = obj.optString("namaItemKerusakan", ""),
                                tanggalLapor = obj.optString("tanggalLapor", "01/08/2026"),
                                statusPenanganan = obj.optString("statusPenanganan", "Segera ditangani"),
                                ditanganiOleh = obj.optString("ditanganiOleh", "Internal"),
                                tanggalSelesai = obj.optString("tanggalSelesai", "-"),
                                keterangan = obj.optString("keterangan", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                repository.restoreAllData(
                    reports = reports,
                    plans = plans,
                    suratList = suratList,
                    peminjamanList = peminjamanList,
                    ruangList = ruangList,
                    statusPenangananList = statusList,
                    urgensiList = urgensiList,
                    kategoriList = kategoriList,
                    damageReportsList = damageList
                )
                val totalRestored = reports.size + plans.size + suratList.size + peminjamanList.size + ruangList.size + statusList.size + urgensiList.size + kategoriList.size + damageList.size
                onSuccess(totalRestored)
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "Format file JSON tidak valid")
            }
        }
    }

    // --- PROJECT TASKS / TO-DO LIST METHODS ---
    fun addProjectTask(
        title: String,
        type: String,
        subKategori: String,
        notes: String,
        startDate: String,
        endDate: String,
        durasiHari: Int,
        bobotPersen: Double
    ) {
        viewModelScope.launch {
            val task = ProjectTask(
                title = title,
                type = type,
                subKategori = subKategori,
                notes = notes,
                startDate = startDate,
                endDate = endDate,
                durasiHari = if (durasiHari <= 0) 1 else durasiHari,
                bobotPersen = bobotPersen,
                totalProgres = if (type == "Harian") bobotPersen else 0.0,
                isCompleted = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertProjectTask(task)
        }
    }

    fun toggleTaskCompletion(task: ProjectTask) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                totalProgres = if (!task.isCompleted) task.bobotPersen else 0.0
            )
            repository.updateProjectTask(updated)
        }
    }

    fun recordDailyProgress(
        task: ProjectTask,
        addedProgress: Double,
        notes: String,
        dateStr: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    ) {
        viewModelScope.launch {
            val newTotal = (task.totalProgres + addedProgress).coerceAtMost(100.0)
            val isCompleteNow = newTotal >= task.bobotPersen || (task.bobotPersen > 0 && (task.bobotPersen - newTotal) <= 0.00001) || newTotal >= 100.0
            val df = java.text.DecimalFormat("0.0######", java.text.DecimalFormatSymbols(Locale.US))
            val percentFormatted = df.format(addedProgress)
            val newLogEntry = "$dateStr: +$percentFormatted% - ${notes.ifBlank { "Progres harian" }}"
            val updatedHistory = if (task.riwayatProgres.isBlank()) newLogEntry else "${task.riwayatProgres}\n$newLogEntry"

            val updatedTask = task.copy(
                totalProgres = newTotal,
                isCompleted = isCompleteNow,
                riwayatProgres = updatedHistory
            )
            repository.updateProjectTask(updatedTask)
        }
    }

    fun updateProjectTask(task: ProjectTask) {
        viewModelScope.launch {
            repository.updateProjectTask(task)
        }
    }

    fun deleteProjectTask(task: ProjectTask) {
        viewModelScope.launch {
            repository.deleteProjectTask(task)
        }
    }

    // --- LOG ENTRY EDIT & DELETE METHODS ---
    fun editProgressLogEntry(
        task: ProjectTask,
        entryIndex: Int,
        newDate: String,
        newPercent: Double,
        newNotes: String
    ) {
        viewModelScope.launch {
            val currentEntries = parseProgressHistory(task.riwayatProgres).toMutableList()
            if (entryIndex in currentEntries.indices) {
                currentEntries[entryIndex] = ProgressLogItem(entryIndex, newDate, newPercent, newNotes)

                val newTotal = currentEntries.sumOf { it.percent }.coerceAtMost(100.0)
                val isCompleteNow = newTotal >= task.bobotPersen
                val df = java.text.DecimalFormat("0.0######", java.text.DecimalFormatSymbols(Locale.US))
                val newHistoryStr = currentEntries.joinToString("\n") {
                    "${it.date}: +${df.format(it.percent)}% - ${it.notes.ifBlank { "Progres harian" }}"
                }

                val updatedTask = task.copy(
                    totalProgres = newTotal,
                    isCompleted = isCompleteNow,
                    riwayatProgres = newHistoryStr
                )
                repository.updateProjectTask(updatedTask)
            }
        }
    }

    fun deleteProgressLogEntry(
        task: ProjectTask,
        entryIndex: Int
    ) {
        viewModelScope.launch {
            val currentEntries = parseProgressHistory(task.riwayatProgres).toMutableList()
            if (entryIndex in currentEntries.indices) {
                currentEntries.removeAt(entryIndex)

                val newTotal = currentEntries.sumOf { it.percent }.coerceAtMost(100.0)
                val isCompleteNow = newTotal >= task.bobotPersen
                val df = java.text.DecimalFormat("0.0######", java.text.DecimalFormatSymbols(Locale.US))
                val newHistoryStr = currentEntries.joinToString("\n") {
                    "${it.date}: +${df.format(it.percent)}% - ${it.notes.ifBlank { "Progres harian" }}"
                }

                val updatedTask = task.copy(
                    totalProgres = newTotal,
                    isCompleted = isCompleteNow,
                    riwayatProgres = newHistoryStr
                )
                repository.updateProjectTask(updatedTask)
            }
        }
    }
}

data class BackupHistoryRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: String,
    val totalRecords: Int,
    val jsonString: String
)

data class ProgressLogItem(
    val index: Int,
    val date: String,
    val percent: Double,
    val notes: String
)

fun parseProgressHistory(rawHistory: String): List<ProgressLogItem> {
    if (rawHistory.isBlank()) return emptyList()
    val lines = rawHistory.split("\n").filter { it.isNotBlank() }
    return lines.mapIndexed { index, line ->
        try {
            val colonIndex = line.indexOf(":")
            if (colonIndex != -1) {
                val date = line.substring(0, colonIndex).trim()
                val rest = line.substring(colonIndex + 1).trim()
                val percentIndex = rest.indexOf("%")
                if (percentIndex != -1) {
                    val percentStr = rest.substring(0, percentIndex).replace("+", "").trim().replace(",", ".")
                    val percent = percentStr.toDoubleOrNull() ?: 0.0
                    val dashIndex = rest.indexOf("-", percentIndex)
                    val notes = if (dashIndex != -1) rest.substring(dashIndex + 1).trim() else ""
                    ProgressLogItem(index, date, percent, notes)
                } else {
                    ProgressLogItem(index, date, 0.0, rest)
                }
            } else {
                ProgressLogItem(index, "", 0.0, line)
            }
        } catch (e: Exception) {
            ProgressLogItem(index, "", 0.0, line)
        }
    }
}

sealed class FirebaseConnState {
    object Idle : FirebaseConnState()
    object Testing : FirebaseConnState()
    data class Success(
        val message: String = "Status: Terhubung ke Firebase Console - Aman",
        val endpoint: String = "https://sarpras-134d7-default-rtdb.asia-southeast1.firebasedatabase.app",
        val latencyMs: Long = 24,
        val timestamp: String = ""
    ) : FirebaseConnState()
    data class Error(
        val message: String = "Status: Terputus / Gangguan Koneksi Firebase",
        val errorDetail: String = "Tidak dapat menjangkau server Firebase Console. Periksa jaringan atau aturan akses database."
    ) : FirebaseConnState()
}


