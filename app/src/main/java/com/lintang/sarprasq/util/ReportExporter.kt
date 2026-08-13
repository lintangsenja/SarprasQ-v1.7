package com.lintang.sarprasq.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.PeminjamanMakro
import com.lintang.sarprasq.data.model.ProjectTask
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.data.model.SuratArsip
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class ReportCategory(val title: String) {
    HELPDESK("Laporan Operasional & Helpdesk"),
    ACTION_PLAN("Laporan Proyek & Revitalisasi Fisik"),
    INVENTARIS_RUANG("Laporan Manajemen Inventaris & Master Ruang"),
    ADMINISTRASI("Laporan Administrasi & Persuratan")
}

data class ReportFilter(
    val category: ReportCategory,
    val dateRangeType: String = "Semua", // Semua, Hari Ini, Bulan Ini, Tahun Ini
    val selectedRuang: String = "Semua Ruang",
    val startDate: String = "",
    val endDate: String = ""
) {
    val dateDisplayString: String
        get() {
            return if (startDate.isNotBlank() || endDate.isNotBlank()) {
                val start = if (startDate.isNotBlank()) startDate else "Awal"
                val end = if (endDate.isNotBlank()) endDate else "Kini"
                "$start s/d $end"
            } else {
                dateRangeType
            }
        }
}

data class ExportedFileResult(
    val file: File,
    val uri: Uri,
    val mimeType: String,
    val fileName: String
)

object ReportExporter {

    private fun formatRupiah(value: Any?): String {
        if (value == null) return "Rp 0"
        val doubleVal = when (value) {
            is Number -> value.toDouble()
            is String -> value.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(doubleVal).replace(",00", "")
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
        return sdf.format(Date())
    }

    private fun getFileTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- FILE SAVING & INTENT LAUNCHING HELPER ---
    fun saveFile(
        context: Context,
        fileName: String,
        mimeType: String,
        writeContent: (FileOutputStream) -> Unit
    ): ExportedFileResult? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            val fos = FileOutputStream(file)
            writeContent(fos)
            fos.flush()
            fos.close()

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            Toast.makeText(context, "File berhasil disimpan di Downloads:\n$fileName", Toast.LENGTH_SHORT).show()
            ExportedFileResult(file, uri, mimeType, fileName)
        } catch (e: Exception) {
            Log.e("ReportExporter", "Error saving file: ${e.message}", e)
            Toast.makeText(context, "Gagal mengunduh file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun openFile(context: Context, uri: Uri, mimeType: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ReportExporter", "Error opening file: ${e.message}", e)
            Toast.makeText(context, "Tidak ada aplikasi default untuk membuka file ini. Silakan periksa folder Downloads.", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String, fileName: String = "Laporan") {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Bagikan $fileName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ReportExporter", "Error sharing file: ${e.message}", e)
            Toast.makeText(context, "Gagal membagikan file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // 1. PDF EXPORT GENERATOR (Native Android PdfDocument API)
    // =========================================================================
    fun exportToPdf(
        context: Context,
        filter: ReportFilter,
        schoolName: String,
        programName: String,
        petugasName: String,
        nipPetugas: String,
        helpdeskList: List<HelpdeskReport>,
        actionPlanList: List<ActionPlan>,
        ruangList: List<Ruang>,
        peminjamanList: List<PeminjamanMakro>,
        suratList: List<SuratArsip>,
        projectTaskList: List<ProjectTask> = emptyList()
    ): ExportedFileResult? {
        val fileName = "Laporan_SarprasQ_${filter.category.name}_${getFileTimestamp()}.pdf"

        return saveFile(context, fileName, "application/pdf") { fos ->
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            var currentY = 40f

            // --- 1. KOP SEKOLAH ---
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 10f
            textPaint.color = Color.DKGRAY
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("PEMERINTAH KABUPATEN / PROVINSI - DINAS PENDIDIKAN", 297.5f, currentY, textPaint)
            currentY += 16f

            textPaint.textSize = 14f
            textPaint.color = Color.BLACK
            canvas.drawText(schoolName.uppercase(Locale.getDefault()), 297.5f, currentY, textPaint)
            currentY += 14f

            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("$programName - APLIKASI SISTEM MANAGEMENT SARPRAS", 297.5f, currentY, textPaint)
            currentY += 12f

            // Divider Line Kop
            paint.color = Color.BLACK
            paint.strokeWidth = 2f
            canvas.drawLine(40f, currentY, 555f, currentY, paint)
            currentY += 3f
            paint.strokeWidth = 0.8f
            canvas.drawLine(40f, currentY, 555f, currentY, paint)
            currentY += 20f

            // --- 2. JUDUL LAPORAN & METADATA ---
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 13f
            textPaint.color = Color.rgb(20, 80, 140)
            canvas.drawText(filter.category.title.uppercase(Locale.getDefault()), 297.5f, currentY, textPaint)
            currentY += 16f

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 8.5f
            textPaint.color = Color.GRAY
            canvas.drawText("Periode: ${filter.dateDisplayString}  |  Lokasi/Ruang: ${filter.selectedRuang}  |  Dicetak: ${getCurrentTimestamp()}", 297.5f, currentY, textPaint)
            currentY += 22f

            // --- 3. REKAP RINGKASAN DATA (METRICS CARD) ---
            paint.color = Color.rgb(240, 244, 248)
            val rect = RectF(40f, currentY, 555f, currentY + 45f)
            canvas.drawRoundRect(rect, 6f, 6f, paint)

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 8.5f
            textPaint.color = Color.BLACK
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            when (filter.category) {
                ReportCategory.HELPDESK -> {
                    val total = helpdeskList.size
                    val belum = helpdeskList.count { it.status.contains("Belum", ignoreCase = true) }
                    val proses = helpdeskList.count { it.status.contains("Proses", ignoreCase = true) || it.status.contains("Perbaikan", ignoreCase = true) }
                    val selesai = helpdeskList.count { it.status.contains("Selesai", ignoreCase = true) }
                    val darurat = helpdeskList.count { it.urgensi.contains("Darurat", ignoreCase = true) }

                    canvas.drawText("RINGKASAN METRIK OPERASIONAL HELPDESK:", 50f, currentY + 16f, textPaint)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Total Laporan: $total item  |  Belum Diproses: $belum  |  Dalam Perbaikan: $proses  |  Selesai: $selesai  |  Urgensi Darurat: $darurat", 50f, currentY + 32f, textPaint)
                }
                ReportCategory.ACTION_PLAN -> {
                    val totalAction = actionPlanList.size
                    val totalTask = projectTaskList.size
                    val total = totalAction + totalTask
                    val totalEst = actionPlanList.sumOf {
                        it.estimasiAnggaran?.replace(Regex("[^0-9]"), "")?.toDoubleOrNull() ?: 0.0
                    }
                    val proses = actionPlanList.count { !it.statusProgres.contains("Selesai", ignoreCase = true) } +
                            projectTaskList.count { !it.isCompleted }
                    val selesai = actionPlanList.count { it.statusProgres.contains("Selesai", ignoreCase = true) } +
                            projectTaskList.count { it.isCompleted }

                    val avgProgress = if (total == 0) 0.0 else {
                        val apSum = actionPlanList.sumOf {
                            when {
                                it.statusProgres.contains("Selesai", ignoreCase = true) -> 100.0
                                it.statusProgres.contains("Proses", ignoreCase = true) -> 50.0
                                else -> 0.0
                            }
                        }
                        val ptSum = projectTaskList.sumOf {
                            if (it.isCompleted) 100.0
                            else if (it.bobotPersen > 0) (it.totalProgres / it.bobotPersen * 100.0).coerceIn(0.0, 100.0)
                            else it.totalProgres.coerceIn(0.0, 100.0)
                        }
                        (apSum + ptSum) / total
                    }

                    canvas.drawText("RINGKASAN METRIK PROYEK & TO-DO / RENCANA:", 50f, currentY + 16f, textPaint)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Total Item: $total | Progres/Aktif: $proses | Selesai: $selesai | Rata-Rata Progres: ${String.format(Locale.US, "%.1f", avgProgress)}% | Est. Anggaran: ${formatRupiah(totalEst)}", 50f, currentY + 32f, textPaint)
                }
                ReportCategory.INVENTARIS_RUANG -> {
                    val totalRuang = ruangList.size
                    val totalPinjam = peminjamanList.size

                    canvas.drawText("RINGKASAN METRIK MANAJEMEN INVENTARIS & RUANG:", 50f, currentY + 16f, textPaint)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Total Ruangan: $totalRuang ruang  |  Riwayat Peminjaman Makro: $totalPinjam transaksi", 50f, currentY + 32f, textPaint)
                }
                ReportCategory.ADMINISTRASI -> {
                    val totalSurat = suratList.size
                    val masuk = suratList.count { it.jenisSurat.contains("Masuk", ignoreCase = true) }
                    val keluar = suratList.count { it.jenisSurat.contains("Keluar", ignoreCase = true) }
                    val beritaAcara = suratList.count { it.jenisSurat.contains("Acara", ignoreCase = true) }

                    canvas.drawText("RINGKASAN METRIK ADMINISTRASI & PERSURATAN:", 50f, currentY + 16f, textPaint)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Total Dokumen: $totalSurat  |  Surat Masuk: $masuk  |  Surat Keluar: $keluar  |  Berita Acara: $beritaAcara", 50f, currentY + 32f, textPaint)
                }
            }

            currentY += 60f

            // --- 4. TABEL DATA UTAMA ---
            val headerPaint = Paint().apply {
                color = Color.rgb(30, 60, 110)
                style = Paint.Style.FILL
            }
            val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val borderPaint = Paint().apply {
                color = Color.LTGRAY
                style = Paint.Style.STROKE
                strokeWidth = 0.5f
            }

            fun drawTableHeader(cols: List<Pair<String, Float>>) {
                var currentX = 40f
                canvas.drawRect(40f, currentY, 555f, currentY + 20f, headerPaint)
                cols.forEach { (title, width) ->
                    canvas.drawText(title, currentX + 4f, currentY + 13f, headerTextPaint)
                    currentX += width
                }
                currentY += 20f
            }

            when (filter.category) {
                ReportCategory.HELPDESK -> {
                    val cols = listOf(
                        "No" to 25f,
                        "Tanggal" to 65f,
                        "Pelapor" to 75f,
                        "Lokasi" to 85f,
                        "Deskripsi Kerusakan" to 140f,
                        "Urgensi" to 55f,
                        "Status" to 70f
                    )
                    drawTableHeader(cols)

                    helpdeskList.take(25).forEachIndexed { idx, item ->
                        var currentX = 40f
                        if (idx % 2 == 1) {
                            canvas.drawRect(40f, currentY, 555f, currentY + 18f, Paint().apply { color = Color.rgb(248, 250, 252) })
                        }
                        canvas.drawRect(40f, currentY, 555f, currentY + 18f, borderPaint)

                        val data = listOf(
                            "${idx + 1}",
                            item.tanggal.take(10),
                            item.pelapor.take(12),
                            item.lokasi.take(14),
                            item.deskripsi.take(28),
                            item.urgensi.take(8),
                            item.status.take(10)
                        )
                        cols.forEachIndexed { colIdx, (_, width) ->
                            canvas.drawText(data[colIdx], currentX + 4f, currentY + 12f, cellTextPaint)
                            currentX += width
                        }
                        currentY += 18f
                    }
                }

                ReportCategory.ACTION_PLAN -> {
                    val cols = listOf(
                        "No" to 25f,
                        "Agenda / Tugas Proyek" to 130f,
                        "Sub-Kategori / Lokasi" to 85f,
                        "Status Progres" to 80f,
                        "Target Waktu" to 65f,
                        "Est. Anggaran / Notes" to 130f
                    )
                    drawTableHeader(cols)

                    var rowIdx = 0

                    projectTaskList.take(25).forEach { item ->
                        var currentX = 40f
                        if (rowIdx % 2 == 1) {
                            canvas.drawRect(40f, currentY, 555f, currentY + 18f, Paint().apply { color = Color.rgb(248, 250, 252) })
                        }
                        canvas.drawRect(40f, currentY, 555f, currentY + 18f, borderPaint)

                        val progressPct = if (item.isCompleted) 100.0 else if (item.bobotPersen > 0) (item.totalProgres / item.bobotPersen * 100.0).coerceIn(0.0, 100.0) else item.totalProgres
                        val statusStr = if (item.isCompleted) "Selesai (100%)" else "${String.format(Locale.US, "%.1f", progressPct)}%"

                        val data = listOf(
                            "${rowIdx + 1}",
                            "[${item.type}] ${item.title}".take(22),
                            item.subKategori.ifBlank { "To-Do" }.take(14),
                            statusStr.take(12),
                            item.startDate.take(10),
                            (if (item.notes.isNotBlank()) item.notes else "Bobot ${String.format(Locale.US, "%.1f", item.bobotPersen)}%").take(22)
                        )
                        cols.forEachIndexed { colIdx, (_, width) ->
                            canvas.drawText(data[colIdx], currentX + 4f, currentY + 12f, cellTextPaint)
                            currentX += width
                        }
                        currentY += 18f
                        rowIdx++
                    }

                    actionPlanList.take((25 - rowIdx).coerceAtLeast(0)).forEach { item ->
                        var currentX = 40f
                        if (rowIdx % 2 == 1) {
                            canvas.drawRect(40f, currentY, 555f, currentY + 18f, Paint().apply { color = Color.rgb(248, 250, 252) })
                        }
                        canvas.drawRect(40f, currentY, 555f, currentY + 18f, borderPaint)

                        val data = listOf(
                            "${rowIdx + 1}",
                            item.agenda.take(22),
                            (item.lokasiRuang ?: "-").take(14),
                            item.statusProgres.take(12),
                            item.targetWaktu.take(10),
                            formatRupiah(item.estimasiAnggaran)
                        )
                        cols.forEachIndexed { colIdx, (_, width) ->
                            canvas.drawText(data[colIdx], currentX + 4f, currentY + 12f, cellTextPaint)
                            currentX += width
                        }
                        currentY += 18f
                        rowIdx++
                    }
                }

                ReportCategory.INVENTARIS_RUANG -> {
                    val cols = listOf(
                        "No" to 25f,
                        "Kode Ruang" to 80f,
                        "Nama Ruang" to 150f,
                        "Kategori Ruang" to 110f,
                        "Penanggung Jawab" to 150f
                    )
                    drawTableHeader(cols)

                    ruangList.take(25).forEachIndexed { idx, item ->
                        var currentX = 40f
                        if (idx % 2 == 1) {
                            canvas.drawRect(40f, currentY, 555f, currentY + 18f, Paint().apply { color = Color.rgb(248, 250, 252) })
                        }
                        canvas.drawRect(40f, currentY, 555f, currentY + 18f, borderPaint)

                        val data = listOf(
                            "${idx + 1}",
                            item.kodeRuang.take(12),
                            item.namaRuang.take(24),
                            item.kategori.take(18),
                            item.penanggungJawab.take(24)
                        )
                        cols.forEachIndexed { colIdx, (_, width) ->
                            canvas.drawText(data[colIdx], currentX + 4f, currentY + 12f, cellTextPaint)
                            currentX += width
                        }
                        currentY += 18f
                    }
                }

                ReportCategory.ADMINISTRASI -> {
                    val cols = listOf(
                        "No" to 25f,
                        "Nomor Surat" to 90f,
                        "Tanggal" to 55f,
                        "Perihal Dokumen" to 115f,
                        "Deskripsi Surat" to 120f,
                        "Jenis Surat" to 55f,
                        "Status" to 55f
                    )
                    drawTableHeader(cols)

                    suratList.take(25).forEachIndexed { idx, item ->
                        var currentX = 40f
                        if (idx % 2 == 1) {
                            canvas.drawRect(40f, currentY, 555f, currentY + 18f, Paint().apply { color = Color.rgb(248, 250, 252) })
                        }
                        canvas.drawRect(40f, currentY, 555f, currentY + 18f, borderPaint)

                        val data = listOf(
                            "${idx + 1}",
                            item.nomorSurat.take(15),
                            item.tanggalSurat.take(10),
                            item.perihal.take(20),
                            item.deskripsiSurat.ifBlank { "-" }.take(22),
                            item.jenisSurat.take(10),
                            item.statusArsip.take(10)
                        )
                        cols.forEachIndexed { colIdx, (_, width) ->
                            canvas.drawText(data[colIdx], currentX + 4f, currentY + 12f, cellTextPaint)
                            currentX += width
                        }
                        currentY += 18f
                    }
                }
            }

            // --- 5. TANDA TANGAN (SIGNATURE BLOCK) ---
            currentY += 35f
            if (currentY > 730f) currentY = 730f

            cellTextPaint.textSize = 8.5f
            cellTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            // Left side: Kepala Sekolah
            canvas.drawText("Mengetahui,", 60f, currentY, cellTextPaint)
            canvas.drawText("Kepala $schoolName", 60f, currentY + 12f, cellTextPaint)
            canvas.drawText("(...................................................)", 60f, currentY + 55f, cellTextPaint)
            canvas.drawText("NIP. ............................................", 60f, currentY + 67f, cellTextPaint)

            // Right side: Petugas Sarpras
            canvas.drawText("${schoolName.take(15)}, ${getCurrentTimestamp().take(12)}", 380f, currentY, cellTextPaint)
            canvas.drawText("Petugas / Pengelola Sarpras", 380f, currentY + 12f, cellTextPaint)

            cellTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(petugasName, 380f, currentY + 55f, cellTextPaint)
            cellTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("NIP. $nipPetugas", 380f, currentY + 67f, cellTextPaint)

            // --- 6. FOOTER HALAMAN ---
            textPaint.textSize = 7.5f
            textPaint.color = Color.GRAY
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Dokumen Resmi Laporan $programName | Halaman 1 dari 1", 297.5f, 820f, textPaint)

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
        }
    }

    // =========================================================================
    // 2. EXCEL (.xlsx) EXPORT GENERATOR (Native OpenXML Zip Package)
    // =========================================================================
    fun exportToExcel(
        context: Context,
        filter: ReportFilter,
        schoolName: String,
        programName: String,
        petugasName: String,
        nipPetugas: String,
        helpdeskList: List<HelpdeskReport>,
        actionPlanList: List<ActionPlan>,
        ruangList: List<Ruang>,
        peminjamanList: List<PeminjamanMakro>,
        suratList: List<SuratArsip>,
        projectTaskList: List<ProjectTask> = emptyList()
    ): ExportedFileResult? {
        val fileName = "Laporan_SarprasQ_${filter.category.name}_${getFileTimestamp()}.xlsx"

        return saveFile(context, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") { fos ->
            val zipOut = ZipOutputStream(fos)

            // 1. [Content_Types].xml
            zipOut.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""
            zipOut.write(contentTypes.toByteArray())
            zipOut.closeEntry()

            // 2. _rels/.rels
            zipOut.putNextEntry(ZipEntry("_rels/.rels"))
            val rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
            zipOut.write(rels.toByteArray())
            zipOut.closeEntry()

            // 3. xl/_rels/workbook.xml.rels
            zipOut.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            val wbRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
            zipOut.write(wbRels.toByteArray())
            zipOut.closeEntry()

            // 4. xl/workbook.xml
            zipOut.putNextEntry(ZipEntry("xl/workbook.xml"))
            val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="${filter.category.title.take(31)}" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
            zipOut.write(workbook.toByteArray())
            zipOut.closeEntry()

            // 5. xl/worksheets/sheet1.xml
            val sheetData = StringBuilder()
            sheetData.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>
""")

            fun escapeXml(text: String): String {
                return text.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;")
            }

            fun addRow(rowIdx: Int, cells: List<String>) {
                sheetData.append("    <row r=\"$rowIdx\">\n")
                cells.forEachIndexed { colIdx, cellValue ->
                    val colLetter = ('A' + colIdx).toString()
                    val cellRef = "$colLetter$rowIdx"
                    sheetData.append("      <c r=\"$cellRef\" t=\"inlineStr\"><is><t>${escapeXml(cellValue)}</t></is></c>\n")
                }
                sheetData.append("    </row>\n")
            }

            var r = 1
            addRow(r++, listOf("PEMERINTAH KABUPATEN / PROVINSI - DINAS PENDIDIKAN"))
            addRow(r++, listOf(schoolName.uppercase(Locale.getDefault())))
            addRow(r++, listOf("$programName - APLIKASI SISTEM MANAGEMENT SARPRAS"))
            addRow(r++, listOf(filter.category.title.uppercase(Locale.getDefault())))
            addRow(r++, listOf("Periode: ${filter.dateDisplayString}", "Ruang: ${filter.selectedRuang}", "Dicetak: ${getCurrentTimestamp()}"))
            addRow(r++, listOf("Petugas: $petugasName", "NIP: $nipPetugas"))
            r++ // Empty spacing row

            when (filter.category) {
                ReportCategory.HELPDESK -> {
                    addRow(r++, listOf("No", "Tanggal", "Pelapor", "Lokasi / Ruang", "Deskripsi Kerusakan", "Urgensi", "Status Penanganan", "Tindakan / Petugas"))
                    helpdeskList.forEachIndexed { idx, item ->
                        addRow(r++, listOf(
                            "${idx + 1}",
                            item.tanggal,
                            item.pelapor,
                            item.lokasi,
                            item.deskripsi,
                            item.urgensi,
                            item.status,
                            if (item.tindakan.isNotBlank()) item.tindakan else (item.petugas ?: "-")
                        ))
                    }
                }
                ReportCategory.ACTION_PLAN -> {
                    addRow(r++, listOf("No", "Jenis / Agenda Proyek", "Sub-Kategori / Kategori", "Lokasi Ruang", "Target Waktu", "Status Progres", "Estimasi Anggaran / Notes", "Pelaksana / Detail"))
                    var rowIdx = 1
                    projectTaskList.forEach { item ->
                        val progressPct = if (item.isCompleted) 100.0 else if (item.bobotPersen > 0) (item.totalProgres / item.bobotPersen * 100.0).coerceIn(0.0, 100.0) else item.totalProgres
                        val statusStr = if (item.isCompleted) "Selesai (100%)" else "${String.format(Locale.US, "%.1f", progressPct)}%"

                        addRow(r++, listOf(
                            "${rowIdx++}",
                            "[${item.type}] ${item.title}",
                            item.subKategori.ifBlank { "To-Do" },
                            "-",
                            "${item.startDate} s/d ${item.endDate}",
                            statusStr,
                            if (item.notes.isNotBlank()) item.notes else "Bobot ${String.format(Locale.US, "%.1f", item.bobotPersen)}%",
                            "Tim Internal Sarpras"
                        ))
                    }
                    actionPlanList.forEach { item ->
                        addRow(r++, listOf(
                            "${rowIdx++}",
                            item.agenda,
                            item.kategori,
                            item.lokasiRuang ?: "-",
                            item.targetWaktu,
                            item.statusProgres,
                            formatRupiah(item.estimasiAnggaran),
                            if (item.pelaksana.isNotBlank()) item.pelaksana else (item.namaTukang ?: "-")
                        ))
                    }
                }
                ReportCategory.INVENTARIS_RUANG -> {
                    addRow(r++, listOf("DATAMASTER RUANGAN & SARPRAS"))
                    addRow(r++, listOf("No", "Kode Ruang", "Nama Ruang", "Kategori Ruang", "Penanggung Jawab"))
                    ruangList.forEachIndexed { idx, item ->
                        addRow(r++, listOf(
                            "${idx + 1}",
                            item.kodeRuang,
                            item.namaRuang,
                            item.kategori,
                            item.penanggungJawab
                        ))
                    }
                    r++
                    addRow(r++, listOf("HISTORI PEMINJAMAN MAKRO FASILITAS"))
                    addRow(r++, listOf("No", "Bulan/Tahun", "Nama Barang / Fasilitas", "Jumlah", "Kondisi"))
                    peminjamanList.forEachIndexed { idx, item ->
                        addRow(r++, listOf(
                            "${idx + 1}",
                            item.bulanTahun,
                            item.namaBarang,
                            "${item.jumlahPeminjaman} unit",
                            item.kondisi
                        ))
                    }
                }
                ReportCategory.ADMINISTRASI -> {
                    addRow(r++, listOf("No", "Nomor Surat", "Tanggal Surat", "Perihal Dokumen", "Deskripsi Surat", "Jenis Surat", "Status Arsip"))
                    suratList.forEachIndexed { idx, item ->
                        addRow(r++, listOf(
                            "${idx + 1}",
                            item.nomorSurat,
                            item.tanggalSurat,
                            item.perihal,
                            item.deskripsiSurat,
                            item.jenisSurat,
                            item.statusArsip
                        ))
                    }
                }
            }

            sheetData.append("""  </sheetData>
</worksheet>""")

            zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zipOut.write(sheetData.toString().toByteArray())
            zipOut.closeEntry()

            zipOut.finish()
        }
    }

    // =========================================================================
    // 3. WORD (.docx) EXPORT GENERATOR (Native OpenXML Zip Package)
    // =========================================================================
    fun exportToWord(
        context: Context,
        filter: ReportFilter,
        schoolName: String,
        programName: String,
        petugasName: String,
        nipPetugas: String,
        helpdeskList: List<HelpdeskReport>,
        actionPlanList: List<ActionPlan>,
        ruangList: List<Ruang>,
        peminjamanList: List<PeminjamanMakro>,
        suratList: List<SuratArsip>,
        projectTaskList: List<ProjectTask> = emptyList()
    ): ExportedFileResult? {
        val fileName = "Laporan_SarprasQ_${filter.category.name}_${getFileTimestamp()}.docx"

        return saveFile(context, fileName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document") { fos ->
            val zipOut = ZipOutputStream(fos)

            // 1. [Content_Types].xml
            zipOut.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""
            zipOut.write(contentTypes.toByteArray())
            zipOut.closeEntry()

            // 2. _rels/.rels
            zipOut.putNextEntry(ZipEntry("_rels/.rels"))
            val rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""
            zipOut.write(rels.toByteArray())
            zipOut.closeEntry()

            // 3. word/_rels/document.xml.rels
            zipOut.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
            val docRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>"""
            zipOut.write(docRels.toByteArray())
            zipOut.closeEntry()

            // 4. word/document.xml
            val docBuilder = StringBuilder()
            docBuilder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
""")

            fun escapeXml(text: String): String {
                return text.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;")
            }

            fun addHeading(text: String, sizePt: Int = 14, align: String = "center", bold: Boolean = true) {
                docBuilder.append("""
    <w:p>
      <w:pPr>
        <w:jc w:val="$align"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          ${if (bold) "<w:b/>" else ""}
          <w:sz w:val="${sizePt * 2}"/>
          <w:color w:val="103060"/>
        </w:rPr>
        <w:t>${escapeXml(text)}</w:t>
      </w:r>
    </w:p>
""")
            }

            fun addParagraph(text: String, bold: Boolean = false, align: String = "left") {
                docBuilder.append("""
    <w:p>
      <w:pPr>
        <w:jc w:val="$align"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          ${if (bold) "<w:b/>" else ""}
          <w:sz w:val="21"/>
        </w:rPr>
        <w:t>${escapeXml(text)}</w:t>
      </w:r>
    </w:p>
""")
            }

            fun addTable(headers: List<String>, rows: List<List<String>>) {
                docBuilder.append("""
    <w:tbl>
      <w:tblPr>
        <w:tblW w:w="0" w:type="auto"/>
        <w:tblBorders>
          <w:top w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
          <w:left w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
          <w:bottom w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
          <w:right w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
          <w:insideH w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
          <w:insideV w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
        </w:tblBorders>
      </w:tblPr>
      <w:tr>
""")
                headers.forEach { h ->
                    docBuilder.append("""
        <w:tc>
          <w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="1E3C6E"/></w:tcPr>
          <w:p><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="18"/></w:rPr><w:t>${escapeXml(h)}</w:t></w:r></w:p>
        </w:tc>
""")
                }
                docBuilder.append("      </w:tr>\n")

                rows.forEach { row ->
                    docBuilder.append("      <w:tr>\n")
                    row.forEach { cell ->
                        docBuilder.append("""
        <w:tc>
          <w:p><w:r><w:rPr><w:sz w:val="18"/></w:rPr><w:t>${escapeXml(cell)}</w:t></w:r></w:p>
        </w:tc>
""")
                    }
                    docBuilder.append("      </w:tr>\n")
                }
                docBuilder.append("    </w:tbl>\n")
            }

            // KOP DOKUMEN
            addHeading("PEMERINTAH KABUPATEN / PROVINSI - DINAS PENDIDIKAN", 10, "center", false)
            addHeading(schoolName.uppercase(Locale.getDefault()), 16, "center", true)
            addHeading("$programName - SISTEM MANAGEMENT SARPRAS", 10, "center", false)
            addParagraph("------------------------------------------------------------------------------------------------------------------------", false, "center")

            addHeading(filter.category.title.uppercase(Locale.getDefault()), 14, "center", true)
            addParagraph("Periode Filter: ${filter.dateDisplayString}  |  Filter Ruang: ${filter.selectedRuang}  |  Dicetak: ${getCurrentTimestamp()}", false, "center")
            addParagraph("Petugas Penanggung Jawab: $petugasName (NIP: $nipPetugas)", true, "left")

            when (filter.category) {
                ReportCategory.HELPDESK -> {
                    val headers = listOf("No", "Tanggal", "Pelapor", "Lokasi", "Deskripsi Kerusakan", "Urgensi", "Status")
                    val rows = helpdeskList.mapIndexed { idx, item ->
                        listOf("${idx + 1}", item.tanggal, item.pelapor, item.lokasi, item.deskripsi, item.urgensi, item.status)
                    }
                    addTable(headers, rows)
                }
                ReportCategory.ACTION_PLAN -> {
                    val headers = listOf("No", "Agenda Proyek / To-Do", "Lokasi / Sub-Kat", "Target Waktu", "Status", "Est. Anggaran / Notes")
                    var rowIdx = 1
                    val rows = mutableListOf<List<String>>()

                    projectTaskList.forEach { item ->
                        val progressPct = if (item.isCompleted) 100.0 else if (item.bobotPersen > 0) (item.totalProgres / item.bobotPersen * 100.0).coerceIn(0.0, 100.0) else item.totalProgres
                        val statusStr = if (item.isCompleted) "Selesai (100%)" else "${String.format(Locale.US, "%.1f", progressPct)}%"

                        rows.add(listOf(
                            "${rowIdx++}",
                            "[${item.type}] ${item.title}",
                            item.subKategori.ifBlank { "-" },
                            "${item.startDate} - ${item.endDate}",
                            statusStr,
                            if (item.notes.isNotBlank()) item.notes else "Bobot ${String.format(Locale.US, "%.1f", item.bobotPersen)}%"
                        ))
                    }

                    actionPlanList.forEach { item ->
                        rows.add(listOf(
                            "${rowIdx++}",
                            item.agenda,
                            item.lokasiRuang ?: "-",
                            item.targetWaktu,
                            item.statusProgres,
                            formatRupiah(item.estimasiAnggaran)
                        ))
                    }

                    addTable(headers, rows)
                }
                ReportCategory.INVENTARIS_RUANG -> {
                    addHeading("DATAMASTER RUANGAN & SARPRAS", 12, "left", true)
                    val headersRuang = listOf("No", "Kode Ruang", "Nama Ruang", "Kategori Ruang", "Penanggung Jawab")
                    val rowsRuang = ruangList.mapIndexed { idx, item ->
                        listOf("${idx + 1}", item.kodeRuang, item.namaRuang, item.kategori, item.penanggungJawab)
                    }
                    addTable(headersRuang, rowsRuang)

                    addHeading("HISTORI PEMINJAMAN MAKRO FASILITAS", 12, "left", true)
                    val headersPinjam = listOf("No", "Bulan/Tahun", "Nama Barang", "Jumlah", "Kondisi")
                    val rowsPinjam = peminjamanList.mapIndexed { idx, item ->
                        listOf("${idx + 1}", item.bulanTahun, item.namaBarang, "${item.jumlahPeminjaman} unit", item.kondisi)
                    }
                    addTable(headersPinjam, rowsPinjam)
                }
                ReportCategory.ADMINISTRASI -> {
                    val headers = listOf("No", "Nomor Surat", "Tanggal Surat", "Perihal Dokumen", "Deskripsi Surat", "Jenis Surat", "Status Arsip")
                    val rows = suratList.mapIndexed { idx, item ->
                        listOf("${idx + 1}", item.nomorSurat, item.tanggalSurat, item.perihal, item.deskripsiSurat.ifBlank { "-" }, item.jenisSurat, item.statusArsip)
                    }
                    addTable(headers, rows)
                }
            }

            // Tanda Tangan
            addParagraph("\n\n", false, "left")
            addParagraph("Mengetahui,", false, "left")
            addParagraph("Kepala $schoolName", false, "left")
            addParagraph("\n\n(............................................................)", false, "left")

            docBuilder.append("""
  </w:body>
</w:document>""")

            zipOut.putNextEntry(ZipEntry("word/document.xml"))
            zipOut.write(docBuilder.toString().toByteArray())
            zipOut.closeEntry()

            zipOut.finish()
        }
    }
}
