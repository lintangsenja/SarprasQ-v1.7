package com.lintang.sarprasq.util

import android.content.Context
import android.net.Uri
import android.util.Xml
import com.lintang.sarprasq.data.model.SuratArsip
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ParseResult(
    val success: Boolean,
    val items: List<SuratArsip> = emptyList(),
    val message: String = "",
    val totalParsedCount: Int = 0
)

object SuratExcelHelper {

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        return sdf.format(Date())
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
        return sdf.format(Date())
    }

    private fun getFileTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun formatExcelDate(rawVal: String): String {
        val clean = rawVal.trim().replace(',', '.')
        if (clean.isBlank()) return ""

        // 1. Check if rawVal is a numeric Excel Serial Date (e.g. "46216", "46216.0")
        val doubleVal = clean.toDoubleOrNull()
        if (doubleVal != null && doubleVal in 1000.0..100000.0) {
            try {
                // 25569.0 is the number of days between 1900-01-01 and 1970-01-01 in Excel's 1900 date system
                val utcMillis = Math.round((doubleVal - 25569.0) * 86400.0 * 1000.0)
                val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = utcMillis
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                return sdf.format(calendar.time)
            } catch (e: Exception) {
                // Fallback if conversion fails
            }
        }

        // 2. Handle string date formatting if it includes time component or ISO format
        val dateOnly = if (clean.contains(" ")) clean.substringBefore(" ") else clean

        // If string matches YYYY-MM-DD or YYYY/MM/DD
        if (dateOnly.matches(Regex("""^\d{4}[-/]\d{1,2}[-/]\d{1,2}$"""))) {
            val parts = dateOnly.split(Regex("[-/]"))
            if (parts.size == 3) {
                val year = parts[0]
                val month = parts[1].padStart(2, '0')
                val day = parts[2].padStart(2, '0')
                return "$year-$month-$day"
            }
        }

        // If string matches DD/MM/YYYY or DD-MM-YYYY
        if (dateOnly.matches(Regex("""^\d{1,2}[-/]\d{1,2}[-/]\d{4}$"""))) {
            val parts = dateOnly.split(Regex("[-/]"))
            if (parts.size == 3) {
                val day = parts[0].padStart(2, '0')
                val month = parts[1].padStart(2, '0')
                val year = parts[2]
                return "$year-$month-$day"
            }
        }

        return dateOnly
    }

    // =========================================================================
    // 1. UNDUH TEMPLATE EXCEL (.xlsx)
    // =========================================================================
    fun generateTemplateXlsx(context: Context): ExportedFileResult? {
        val fileName = "Template_Arsip_Surat_SarprasQ.xlsx"

        return ReportExporter.saveFile(context, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") { fos ->
            val zipOut = ZipOutputStream(fos)

            // 1. [Content_Types].xml
            zipOut.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
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
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            zipOut.write(wbRels.toByteArray())
            zipOut.closeEntry()

            // 4. xl/styles.xml
            zipOut.putNextEntry(ZipEntry("xl/styles.xml"))
            val stylesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <numFmts count="1">
    <numFmt numFmtId="49" formatCode="@"/>
  </numFmts>
  <fonts count="1">
    <font><sz val="11"/><name val="Calibri"/></font>
  </fonts>
  <fills count="1">
    <fill><patternFill patternType="none"/></fill>
  </fills>
  <borders count="1">
    <border><left/><right/><top/><bottom/></border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="49" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
  </cellXfs>
</styleSheet>"""
            zipOut.write(stylesXml.toByteArray())
            zipOut.closeEntry()

            // 5. xl/workbook.xml
            zipOut.putNextEntry(ZipEntry("xl/workbook.xml"))
            val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Template Arsip Surat" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
            zipOut.write(workbook.toByteArray())
            zipOut.closeEntry()

            // 6. xl/worksheets/sheet1.xml
            val sheetData = StringBuilder()
            sheetData.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>
""")

            fun addRow(rowIdx: Int, cells: List<String>) {
                sheetData.append("    <row r=\"$rowIdx\">\n")
                cells.forEachIndexed { colIdx, cellValue ->
                    val colLetter = ('A' + colIdx).toString()
                    val cellRef = "$colLetter$rowIdx"
                    sheetData.append("      <c r=\"$cellRef\" s=\"1\" t=\"inlineStr\"><is><t>${escapeXml(cellValue)}</t></is></c>\n")
                }
                sheetData.append("    </row>\n")
            }

            var r = 1
            addRow(r++, listOf("TEMPLATE MASTER IMPOR DATA ARSIP SURAT - SARPRASQ"))
            addRow(r++, listOf("Petunjuk: Isi data surat di bawah header kolom. Kolom Tanggal Surat diisi dengan format tanggal (contoh: 2026-08-13 atau 13/08/2026). Jangan mengubah nama header kolom utama."))
            addRow(r++, listOf("")) // Baris kosong

            // Header Tabel Utama
            addRow(r++, listOf(
                "Nomor Surat",
                "Tanggal Surat",
                "Perihal / Instansi",
                "Deskripsi Surat",
                "Jenis Surat",
                "Status Kelengkapan Arsip"
            ))

            // Sample Data 1
            addRow(r++, listOf(
                "015/SRV/VIII/2026",
                "2026-08-10",
                "Permohonan Pemeliharaan Jaringan Internet Perpus",
                "Memohon bantuan perbaikan akses Wi-Fi perpustakaan lantai 2 yang mati total.",
                "Surat Masuk",
                "Arsip Digital"
            ))

            // Sample Data 2
            addRow(r++, listOf(
                "089/SP/SARPRAS/VIII/2026",
                "2026-08-11",
                "Surat Undangan Rapat Koordinasi Evaluasi Sarpras",
                "Undangan rapat evaluasi pemeliharaan fasilitas ruang kelas bersama jajaran tim sarpras.",
                "Surat Keluar",
                "Arsip Fisik & Digital"
            ))

            // Sample Data 3
            addRow(r++, listOf(
                "105/ND-SAR/VIII/2026",
                "2026-08-12",
                "Nota Dinas Perbaikan Proyektor Lab Komputer",
                "Pengajuan pengadaan unit proyektor pengganti untuk Lab Komputer 3.",
                "Nota Dinas",
                "Arsip Digital"
            ))

            sheetData.append("""  </sheetData>
</worksheet>""")

            zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zipOut.write(sheetData.toString().toByteArray())
            zipOut.closeEntry()

            zipOut.finish()
        }
    }

    // =========================================================================
    // 2. EKSPOR DATA ARSIP SURAT AKTIF (.xlsx)
    // =========================================================================
    fun exportSuratArsipToXlsx(
        context: Context,
        suratList: List<SuratArsip>,
        schoolName: String = "SMK Negeri 1 SarprasQ",
        programName: String = "Aplikasi SarprasQ",
        petugasName: String = "Staf Sarpras",
        nipPetugas: String = "-"
    ): ExportedFileResult? {
        val fileName = "Export_Arsip_Surat_${getFileTimestamp()}.xlsx"

        return ReportExporter.saveFile(context, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") { fos ->
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
    <sheet name="Data Arsip Surat" sheetId="1" r:id="rId1"/>
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
            addRow(r++, listOf("$programName - SISTEM MANAGEMENT SARPRAS"))
            addRow(r++, listOf("REKAP DATA ARSIP SURAT-MENYURAT"))
            addRow(r++, listOf("Dicetak: ${getCurrentTimestamp()}", "Total Item: ${suratList.size} Surat"))
            addRow(r++, listOf("Petugas Penanggung Jawab: $petugasName", "NIP: $nipPetugas"))
            addRow(r++, listOf("")) // Spacing

            // Table Header
            addRow(r++, listOf("No", "Nomor Surat", "Tanggal Surat", "Perihal / Instansi", "Deskripsi Surat", "Jenis Surat", "Status Kelengkapan Arsip"))

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

            sheetData.append("""  </sheetData>
</worksheet>""")

            zipOut.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zipOut.write(sheetData.toString().toByteArray())
            zipOut.closeEntry()

            zipOut.finish()
        }
    }

    // =========================================================================
    // 3. IMPOR EXCEL DENGAN SMART PARSER & VALIDASI SKEMA
    // =========================================================================
    fun parseSuratArsipFromXlsx(context: Context, fileUri: Uri): ParseResult {
        return try {
            var sharedStringsXml: String? = null
            var sheetXml: String? = null

            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                val zipIn = ZipInputStream(inputStream)
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    val entryName = entry.name.lowercase(Locale.ROOT)
                    if (entryName.endsWith("sharedstrings.xml")) {
                        sharedStringsXml = zipIn.bufferedReader().readText()
                    } else if (entryName.contains("worksheets/sheet") && entryName.endsWith(".xml") && sheetXml == null) {
                        sheetXml = zipIn.bufferedReader().readText()
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            if (sheetXml.isNullOrBlank()) {
                return ParseResult(
                    success = false,
                    message = "Berkas Excel tidak valid atau tidak memuat lembar kerja (worksheet)."
                )
            }

            val sharedStrings = if (!sharedStringsXml.isNullOrBlank()) {
                parseSharedStrings(sharedStringsXml!!)
            } else {
                emptyList()
            }

            val rowsList = parseSheetRows(sheetXml!!, sharedStrings)
            if (rowsList.isEmpty()) {
                return ParseResult(
                    success = false,
                    message = "File Excel kosong atau tidak memiliki baris data."
                )
            }

            // --- SMART PARSER: DETEKSI BARIS HEADER SECARA DINAMIS ---
            var headerRowIndex = -1
            var colNomor = -1
            var colTanggal = -1
            var colPerihal = -1
            var colDeskripsi = -1
            var colJenis = -1
            var colStatus = -1

            for (i in rowsList.indices) {
                val rowMap = rowsList[i]
                var matchesCount = 0
                var foundNomor = -1
                var foundTanggal = -1
                var foundPerihal = -1
                var foundDeskripsi = -1
                var foundJenis = -1
                var foundStatus = -1

                rowMap.forEach { (colIdx, valStr) ->
                    val clean = valStr.lowercase(Locale.ROOT).trim()
                    if (clean.contains("nomor") || clean.contains("no. surat") || clean.contains("no surat") || clean.contains("no dok")) {
                        foundNomor = colIdx
                        matchesCount++
                    } else if (clean.contains("tanggal") || clean.contains("tgl")) {
                        foundTanggal = colIdx
                        matchesCount++
                    } else if (clean.contains("perihal") || clean.contains("instansi") || clean.contains("judul") || clean.contains("subject")) {
                        foundPerihal = colIdx
                        matchesCount++
                    } else if (clean.contains("deskripsi") || clean.contains("keterangan") || clean.contains("rincian") || clean.contains("catatan") || clean.contains("detail")) {
                        foundDeskripsi = colIdx
                        matchesCount++
                    } else if (clean.contains("jenis") || clean.contains("kategori") || clean.contains("tipe")) {
                        foundJenis = colIdx
                        matchesCount++
                    } else if (clean.contains("status") || clean.contains("kelengkapan") || clean.contains("arsip")) {
                        foundStatus = colIdx
                        matchesCount++
                    }
                }

                // Jika sebuah baris memiliki minimal 2 kata kunci header master arsip, kita jadikan Header Row!
                if (matchesCount >= 2) {
                    headerRowIndex = i
                    colNomor = foundNomor
                    colTanggal = foundTanggal
                    colPerihal = foundPerihal
                    colDeskripsi = foundDeskripsi
                    colJenis = foundJenis
                    colStatus = foundStatus
                    break
                }
            }

            // Fallback jika tidak terdeteksi via kata kunci:
            // Cek apakah kolom standar dapat diasumsikan dari baris 0/1
            if (headerRowIndex == -1) {
                if (rowsList.size >= 2) {
                    headerRowIndex = 0
                    colNomor = 0
                    colTanggal = 1
                    colPerihal = 2
                    colDeskripsi = 3
                    colJenis = 4
                    colStatus = 5
                } else {
                    return ParseResult(
                        success = false,
                        message = "Header kolom Excel tidak sesuai dengan master Arsip Surat (butuh kolom: Nomor Surat, Tanggal, Perihal, Deskripsi, Jenis, Status)."
                    )
                }
            }

            // Validasi: Minimal perihal atau nomor surat harus terpetakan
            if (colPerihal == -1 && colNomor == -1) {
                return ParseResult(
                    success = false,
                    message = "Gagal memetakan kolom 'Nomor Surat' atau 'Perihal'. Pastikan header tabel Excel memuat nama kolom tersebut."
                )
            }

            val parsedSuratList = mutableListOf<SuratArsip>()
            val defaultDate = getCurrentDateString()
            val nowTime = System.currentTimeMillis()

            for (rowIndex in (headerRowIndex + 1) until rowsList.size) {
                val rowMap = rowsList[rowIndex]
                val nomorVal = if (colNomor >= 0) rowMap[colNomor]?.trim() ?: "" else ""
                val rawTanggal = if (colTanggal >= 0) rowMap[colTanggal]?.trim() ?: "" else ""
                val tanggalVal = formatExcelDate(rawTanggal)
                val perihalVal = if (colPerihal >= 0) rowMap[colPerihal]?.trim() ?: "" else ""
                val deskripsiVal = if (colDeskripsi >= 0) rowMap[colDeskripsi]?.trim() ?: "" else ""
                val jenisVal = if (colJenis >= 0) rowMap[colJenis]?.trim() ?: "" else ""
                val statusVal = if (colStatus >= 0) rowMap[colStatus]?.trim() ?: "" else ""

                // Abaikan baris kosong
                if (nomorVal.isBlank() && perihalVal.isBlank()) {
                    continue
                }

                val finalNomor = if (nomorVal.isNotBlank()) nomorVal else "SURAT-IMP-${(nowTime + rowIndex) % 100000}"
                val finalTanggal = if (tanggalVal.isNotBlank()) tanggalVal else defaultDate
                val finalPerihal = if (perihalVal.isNotBlank()) perihalVal else "Arsip Surat Impor Excel"
                val finalJenis = if (jenisVal.isNotBlank()) jenisVal else "Surat Masuk"
                val finalStatus = if (statusVal.isNotBlank()) statusVal else "Arsip Digital"

                parsedSuratList.add(
                    SuratArsip(
                        nomorSurat = finalNomor,
                        tanggalSurat = finalTanggal,
                        perihal = finalPerihal,
                        deskripsiSurat = deskripsiVal,
                        jenisSurat = finalJenis,
                        statusArsip = finalStatus,
                        timestamp = nowTime - (rowIndex * 100)
                    )
                )
            }

            if (parsedSuratList.isEmpty()) {
                return ParseResult(
                    success = false,
                    message = "Tidak ada baris data valid yang ditemukan di bawah baris header Excel."
                )
            }

            ParseResult(
                success = true,
                items = parsedSuratList,
                totalParsedCount = parsedSuratList.size,
                message = "Berhasil membaca ${parsedSuratList.size} data arsip surat dari file Excel."
            )
        } catch (e: Exception) {
            ParseResult(
                success = false,
                message = "Gagal membaca berkas Excel: ${e.localizedMessage ?: "Format file tidak didukung"}"
            )
        }
    }

    private fun parseSharedStrings(xmlStr: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlStr))

            var eventType = parser.eventType
            val currentText = StringBuilder()
            var inT = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inT = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inT) {
                            currentText.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inT = false
                        } else if (parser.name.equals("si", ignoreCase = true)) {
                            list.add(currentText.toString().trim())
                            currentText.clear()
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun parseSheetRows(sheetXml: String, sharedStrings: List<String>): List<Map<Int, String>> {
        val rowsList = mutableListOf<Map<Int, String>>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(sheetXml))

            var eventType = parser.eventType
            var currentRowMap = mutableMapOf<Int, String>()
            var currentCellRef = ""
            var currentCellType = ""
            var currentCellValue = StringBuilder()
            var autoColIdx = 0
            var inV = false
            var inT = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name.lowercase(Locale.ROOT)
                        if (tagName == "row") {
                            currentRowMap = mutableMapOf()
                            autoColIdx = 0
                        } else if (tagName == "c") {
                            currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            currentCellValue.clear()
                        } else if (tagName == "v") {
                            inV = true
                        } else if (tagName == "t") {
                            inT = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inV || inT) {
                            currentCellValue.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name.lowercase(Locale.ROOT)
                        if (tagName == "v" || tagName == "t") {
                            inV = false
                            inT = false
                        } else if (tagName == "c") {
                            val colIdxFromRef = getColIndexFromCellRef(currentCellRef)
                            val colIdx = if (colIdxFromRef >= 0) colIdxFromRef else autoColIdx
                            autoColIdx = colIdx + 1

                            var finalVal = currentCellValue.toString().trim()

                            if (currentCellType == "s") {
                                val idx = finalVal.toIntOrNull()
                                if (idx != null && idx in sharedStrings.indices) {
                                    finalVal = sharedStrings[idx]
                                }
                            }

                            if (colIdx >= 0 && finalVal.isNotBlank()) {
                                currentRowMap[colIdx] = finalVal
                            }
                        } else if (tagName == "row") {
                            if (currentRowMap.isNotEmpty()) {
                                rowsList.add(currentRowMap)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rowsList
    }

    private fun getColIndexFromCellRef(cellRef: String): Int {
        if (cellRef.isBlank()) return -1
        val letters = cellRef.takeWhile { it.isLetter() }.uppercase(Locale.ROOT)
        if (letters.isEmpty()) return -1
        var col = 0
        for (char in letters) {
            col = col * 26 + (char - 'A' + 1)
        }
        return col - 1
    }
}
