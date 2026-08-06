package com.lintang.sarprasq.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "helpdesk_reports")
data class HelpdeskReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tanggal: String = "",
    val pelapor: String = "",
    val lokasi: String = "",
    val deskripsi: String = "",
    val urgensi: String = "Sedang", // "Darurat", "Penting", "Sedang", "Rendah"
    val status: String = "Pending",   // "Pending", "Proses", "Selesai"
    val tindakan: String = "-",
    val alasanPending: String? = null,
    val estimasiEksekusi: String? = null,
    val kategori: String = "Elektronik & Audio Visual",
    val fotoUrl: String? = null,
    val catatan: String? = null,
    val petugas: String? = null,
    val progresPersen: Int = 0,
    val riwayatProgres: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class DynamicDamageItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val kategori: String = "Elektronik & Audio Visual",
    val deskripsi: String = "",
    val urgensi: String = "Sedang"
)

fun parseDamageItemsFromReport(
    report: HelpdeskReport?,
    defaultKat: String = "Elektronik & Audio Visual",
    defaultUrg: String = "Sedang"
): List<DynamicDamageItem> {
    if (report == null || report.deskripsi.isBlank()) {
        return listOf(DynamicDamageItem(kategori = defaultKat, urgensi = defaultUrg))
    }
    val lines = report.deskripsi.lines().filter { it.isNotBlank() }
    val items = mutableListOf<DynamicDamageItem>()
    for (line in lines) {
        val cleanLine = line.trim().removePrefix("•").removePrefix("-").trim()
            .replace(Regex("^\\d+\\.\\s*"), "")

        var kat = report.kategori.ifBlank { defaultKat }
        var urg = report.urgensi.ifBlank { defaultUrg }
        var desc = cleanLine

        val katMatch = Regex("^\\[(.*?)\\]\\s*(.*)").find(cleanLine)
        if (katMatch != null) {
            kat = katMatch.groupValues[1]
            desc = katMatch.groupValues[2]
        }

        val urgMatch = Regex("(.*?)\\s*\\((?:Urgensi:\\s*)?(Darurat|Penting|Sedang|Rendah|Biasa)\\)$", RegexOption.IGNORE_CASE).find(desc)
        if (urgMatch != null) {
            desc = urgMatch.groupValues[1].trim()
            urg = urgMatch.groupValues[2].trim()
        }

        if (desc.isNotBlank()) {
            items.add(DynamicDamageItem(kategori = kat, deskripsi = desc, urgensi = urg))
        }
    }

    return if (items.isNotEmpty()) items else listOf(
        DynamicDamageItem(
            kategori = report.kategori.ifBlank { defaultKat },
            deskripsi = report.deskripsi,
            urgensi = report.urgensi.ifBlank { defaultUrg }
        )
    )
}

fun formatCombinedDamageDeskripsi(items: List<DynamicDamageItem>): String {
    return if (items.size == 1) {
        val single = items.first()
        "[${single.kategori}] ${single.deskripsi}"
    } else {
        items.joinToString("\n") { item ->
            "• [${item.kategori}] ${item.deskripsi} (Urgensi: ${item.urgensi})"
        }
    }
}

fun calculateCalculatedUrgency(items: List<DynamicDamageItem>): String {
    val hasDarurat = items.any { it.urgensi.equals("Darurat", ignoreCase = true) }
    val hasPenting = items.any { it.urgensi.equals("Penting", ignoreCase = true) }
    return when {
        hasDarurat -> "Darurat"
        hasPenting -> "Penting"
        else -> items.firstOrNull()?.urgensi ?: "Sedang"
    }
}

fun calculateCalculatedCategory(items: List<DynamicDamageItem>): String {
    val distinctCategories = items.map { it.kategori }.distinct()
    return if (distinctCategories.size == 1) {
        distinctCategories.first()
    } else {
        "Multi-Kerusakan (${distinctCategories.size} Jenis)"
    }
}

@Entity(tableName = "action_plans")
data class ActionPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agenda: String = "",
    val jenisRencana: String = "Rencana Perbaikan / Perawatan", // "Rencana Perbaikan / Perawatan", "Rencana Pengadaan", "Rencana Belanja Barang / Jasa"
    val kategori: String = "Pemeliharaan Berkala", // "Pemeliharaan Berkala", "Perbaikan Minor", "Perbaikan Mayor", "Lainnya"
    val targetWaktu: String = "",
    val tanggalMulai: String = "-",
    val tanggalRealisasi: String = "-",
    val statusProgres: String = "Belum Mulai", // "Belum Mulai", "Dalam Proses", "Selesai"
    val pelaksana: String = "Internal", // "Internal", "Eksternal"
    val namaTukang: String? = null,
    val nomorWhatsapp: String? = null,
    val detailAset: String? = null,
    val lokasiRuang: String? = null,
    val tingkatKerusakan: String? = null,
    val spesifikasi: String? = null,
    val jumlahSatuan: String? = null,
    val estimasiAnggaran: String? = null,
    val fotoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "surat_arsip")
data class SuratArsip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nomorSurat: String = "",
    val tanggalSurat: String = "",
    val perihal: String = "",
    val jenisSurat: String = "Masuk", // "Masuk", "Keluar"
    val statusArsip: String = "Arsip Fisik & Digital",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "peminjaman_makro")
data class PeminjamanMakro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bulanTahun: String = "", // e.g. "Juli 2026"
    val namaBarang: String = "",
    val jumlahPeminjaman: Int = 0,
    val kondisi: String = "Aman", // "Aman", "Perlu Maintenance", "Kurang Baik"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "master_ruang")
data class Ruang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val kodeRuang: String = "",
    val namaRuang: String = "",
    val kategori: String = "Ruang Kelas", // "Ruang Kelas", "Laboratorium", "Ruang Kerja", "Fasilitas Umum"
    val penanggungJawab: String = "-",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "master_status_penanganan")
data class StatusPenanganan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaStatus: String = "",
    val deskripsi: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "master_urgensi")
data class UrgensiMaster(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaUrgensi: String = "",
    val deskripsi: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "master_kategori")
data class KategoriMaster(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaKategori: String = "",
    val deskripsi: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "master_sub_kategori")
data class SubKategoriMaster(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaSubKategori: String = "",
    val deskripsi: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "master_satuan")
data class SatuanMaster(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaSatuan: String = "",
    val deskripsi: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "damage_reports")
data class DamageReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomId: Int = 0,
    val namaRuang: String = "",
    val namaPelapor: String = "",
    val namaItemKerusakan: String = "",
    val tanggalLapor: String = "",
    val statusPenanganan: String = "Segera ditangani", // "Segera ditangani", "Dalam Perbaikan", "Pending", "Selesai diperbaiki", "Tidak bisa diperbaiki"
    val ditanganiOleh: String = "Internal", // "Internal" / "Teknisi Luar"
    val tanggalSelesai: String = "-",
    val keterangan: String = "",
    val fotoUrl: String? = null,
    val kategori: String = "Elektronik & Audio Visual",
    val urgensi: String = "Sedang",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_tasks")
data class ProjectTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val type: String = "Harian", // "Harian" vs "Proyek Revitalisasi"
    val subKategori: String = "", // e.g. "Pekerjaan Persiapan", "Pekerjaan Plafon & Atap", "Pekerjaan Pengecatan"
    val notes: String = "",
    val startDate: String = "", // dd/MM/yyyy (e.g. "04/08/2026")
    val endDate: String = "",   // dd/MM/yyyy (e.g. "06/08/2026")
    val durasiHari: Int = 1,
    val bobotPersen: Double = 10.0, // Total target weight %
    val totalProgres: Double = 0.0, // Accumulated progress % achieved
    val isCompleted: Boolean = false,
    val riwayatProgres: String = "", // Progress history log
    val timestamp: Long = System.currentTimeMillis()
) {
    val sisaKekuranganProgres: Double
        get() {
            if (isCompleted) return 0.0
            val diff = bobotPersen - totalProgres
            return if (diff <= 0.00001) 0.0 else diff
        }
}
