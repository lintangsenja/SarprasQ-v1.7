package com.lintang.sarprasq.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.data.model.SatuanMaster
import com.lintang.sarprasq.data.model.SubKategoriMaster
import com.lintang.sarprasq.ui.theme.PastelButterYellow
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelMintLight
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActionPlanDialog(
    initialPlan: ActionPlan? = null,
    masterSubKategoriList: List<SubKategoriMaster> = emptyList(),
    masterRuangList: List<Ruang> = emptyList(),
    masterSatuanList: List<SatuanMaster> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (
        agenda: String,
        jenisRencana: String,
        kategori: String,
        targetWaktu: String,
        statusProgres: String,
        tanggalMulai: String,
        tanggalRealisasi: String,
        pelaksana: String,
        namaTukang: String?,
        nomorWhatsapp: String?,
        detailAset: String?,
        lokasiRuang: String?,
        tingkatKerusakan: String?,
        spesifikasi: String?,
        jumlahSatuan: String?,
        estimasiAnggaran: String?,
        fotoUrl: String?
    ) -> Unit
) {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    val today = sdf.format(java.util.Date())

    val kategoriOptions = remember(masterSubKategoriList) {
        masterSubKategoriList.map { it.namaSubKategori }
    }

    // Wizard current step: 1 = Inti Rencana, 2 = Eksekusi & Waktu, 3 = Catatan & Anggaran
    var currentStep by remember { mutableIntStateOf(1) }
    var showErrorValidation by remember { mutableStateOf(false) }

    // State Tahap 1: Inti Rencana
    var agenda by remember { mutableStateOf(initialPlan?.agenda ?: "") }
    var jenisRencana by remember { mutableStateOf(initialPlan?.jenisRencana ?: "Rencana Perbaikan / Perawatan") }
    var kategori by remember { mutableStateOf(initialPlan?.kategori ?: (kategoriOptions.firstOrNull() ?: "")) }

    // State Tahap 2: Eksekusi & Waktu
    var tanggalMulai by remember { mutableStateOf(if (initialPlan?.tanggalMulai.isNullOrBlank() || initialPlan?.tanggalMulai == "-") today else initialPlan!!.tanggalMulai) }
    var targetWaktu by remember { mutableStateOf(if (initialPlan?.targetWaktu.isNullOrBlank()) today else initialPlan!!.targetWaktu) }
    var pelaksana by remember { mutableStateOf(initialPlan?.pelaksana ?: "Internal") }
    var namaTukang by remember { mutableStateOf(if (!initialPlan?.namaTukang.isNullOrBlank()) initialPlan!!.namaTukang else if (pelaksana == "Internal") "Kevin Ricky Utama, S.Kom." else "") }
    var nomorWhatsapp by remember { mutableStateOf(initialPlan?.nomorWhatsapp ?: "") }
    var statusProgres by remember { mutableStateOf(initialPlan?.statusProgres ?: "Belum Mulai") }
    var tanggalRealisasi by remember { mutableStateOf(initialPlan?.tanggalRealisasi ?: "-") }

    // State Tahap 3: Catatan & Anggaran Fleksibel (Opsional)
    fun formatInitialRupiah(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val digitsOnly = input.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return input
        val parsed = digitsOnly.toLongOrNull() ?: return input
        return "Rp. ${java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(parsed)}"
    }

    var spesifikasi by remember { mutableStateOf(initialPlan?.spesifikasi ?: initialPlan?.detailAset ?: "") }
    var estimasiAnggaran by remember { mutableStateOf(formatInitialRupiah(initialPlan?.estimasiAnggaran)) }
    var fotoUrl by remember { mutableStateOf(initialPlan?.fotoUrl ?: "") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { fotoUrl = it.toString() }
    }

    val samplePhotos = listOf(
        "Proyektor" to "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400",
        "AC Split" to "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=400",
        "Komputer" to "https://images.unsplash.com/photo-1587831990711-23ca6441447b?w=400"
    )

    var expandedJenisRencana by remember { mutableStateOf(false) }
    var expandedKategori by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val jenisRencanaOptions = listOf(
        "Rencana Perbaikan / Perawatan",
        "Rencana Pengadaan",
        "Rencana Belanja Barang / Jasa"
    )
    val statusOptions = listOf("Belum Mulai", "Dalam Proses", "Selesai")

    val doSubmit = {
        if (agenda.isNotBlank()) {
            onSubmit(
                agenda.trim(),
                jenisRencana,
                kategori,
                targetWaktu,
                statusProgres,
                tanggalMulai,
                tanggalRealisasi,
                pelaksana,
                namaTukang.ifBlank { null },
                nomorWhatsapp.ifBlank { null },
                spesifikasi.ifBlank { null }, // detailAset
                null, // lokasiRuang
                null, // tingkatKerusakan
                spesifikasi.ifBlank { null }, // spesifikasi
                null, // jumlahSatuan
                estimasiAnggaran.ifBlank { null },
                fotoUrl.ifBlank { null }
            )
        } else {
            currentStep = 1
            showErrorValidation = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialPlan != null) "Edit Rencana Kerja" else "Tambah Rencana Kerja Proaktif",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Form 3 Tahap Logis & Ringkas",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stepper Indicator Row (3 Steps)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val stepTitles = listOf("1. Inti", "2. Eksekusi", "3. Catatan")
                    stepTitles.forEachIndexed { index, title ->
                        val stepNum = index + 1
                        val isActive = currentStep == stepNum
                        val isPassed = currentStep > stepNum

                        val bg = when {
                            isActive -> PastelSkyBlueDark
                            isPassed -> PastelMintLight
                            else -> Color(0xFFF1F5F9)
                        }
                        val contentColor = when {
                            isActive -> Color.White
                            isPassed -> PastelMintDark
                            else -> TextSecondary
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable {
                                    if (stepNum == 1 || agenda.isNotBlank()) {
                                        currentStep = stepNum
                                    } else {
                                        showErrorValidation = true
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPassed) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Validation Warning
                if (showErrorValidation && agenda.isBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PastelPeach.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PastelPeachDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Harap isi Nama Kegiatan / Proyek Rencana terlebih dahulu.",
                                fontSize = 11.sp,
                                color = PastelPeachDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // STEP CONTENT SWITCHING
                when (currentStep) {
                    1 -> {
                        // TAHAP 1: INTI RENCANA (Judul & Kategori)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PastelSkyBlueContainer.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = PastelSkyBlueDark, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tahap 1: Inti Rencana (Judul & Kategori)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelSkyBlueDark
                                    )
                                }

                                OutlinedTextField(
                                    value = agenda,
                                    onValueChange = {
                                        agenda = it
                                        if (it.isNotBlank()) showErrorValidation = false
                                    },
                                    label = { Text("Nama Kegiatan / Proyek Rencana *") },
                                    placeholder = { Text("Contoh: Perbaikan AC Lab Komputer / Kuras Tandon") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    isError = showErrorValidation && agenda.isBlank()
                                )

                                ExposedDropdownMenuBox(
                                    expanded = expandedJenisRencana,
                                    onExpandedChange = { expandedJenisRencana = !expandedJenisRencana }
                                ) {
                                    OutlinedTextField(
                                        value = jenisRencana,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Jenis Rencana") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJenisRencana) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedJenisRencana,
                                        onDismissRequest = { expandedJenisRencana = false }
                                    ) {
                                        jenisRencanaOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option, fontWeight = FontWeight.Medium) },
                                                onClick = {
                                                    jenisRencana = option
                                                    expandedJenisRencana = false
                                                }
                                            )
                                        }
                                    }
                                }

                                ExposedDropdownMenuBox(
                                    expanded = expandedKategori,
                                    onExpandedChange = { expandedKategori = !expandedKategori }
                                ) {
                                    OutlinedTextField(
                                        value = kategori,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Sub-Kategori / Sifat Pekerjaan") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategori) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedKategori,
                                        onDismissRequest = { expandedKategori = false }
                                    ) {
                                        kategoriOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    kategori = option
                                                    expandedKategori = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAHAP 2: EKSEKUSI & WAKTU (Action & Schedule)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PastelLavender.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PastelLavenderDark, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tahap 2: Eksekusi & Waktu (Action & Schedule)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelLavenderDark
                                    )
                                }

                                DatePickerField(
                                    value = tanggalMulai,
                                    onDateSelected = { tanggalMulai = it },
                                    label = "Tanggal Rencana Mulai"
                                )

                                DatePickerField(
                                    value = targetWaktu,
                                    onDateSelected = { targetWaktu = it },
                                    label = "Target Selesai / Deadline Pekerjaan *"
                                )

                                // Pilihan Pelaksana
                                Text("Pelaksana Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val isInternal = pelaksana == "Internal"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isInternal) PastelSkyBlueDark else Color(0xFFF1F5F9))
                                            .clickable { pelaksana = "Internal" }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isInternal) Color.White else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Internal (Sarpras)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isInternal) Color.White else TextSecondary
                                            )
                                        }
                                    }

                                    val isEksternal = pelaksana == "Eksternal"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isEksternal) PastelLavenderDark else Color(0xFFF1F5F9))
                                            .clickable { pelaksana = "Eksternal" }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Engineering,
                                                contentDescription = null,
                                                tint = if (isEksternal) Color.White else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Eksternal (Tukang/Vendor)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isEksternal) Color.White else TextSecondary
                                            )
                                        }
                                    }
                                }

                                if (pelaksana == "Eksternal") {
                                    OutlinedTextField(
                                        value = namaTukang,
                                        onValueChange = { namaTukang = it },
                                        label = { Text("Nama Tukang / Vendor (Opsional)") },
                                        placeholder = { Text("Contoh: Pak Budi (Teknisi AC)") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = nomorWhatsapp,
                                        onValueChange = { nomorWhatsapp = it },
                                        label = { Text("Nomor WhatsApp Vendor (Opsional)") },
                                        placeholder = { Text("Contoh: 08123456789") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                ExposedDropdownMenuBox(
                                    expanded = expandedStatus,
                                    onExpandedChange = { expandedStatus = !expandedStatus }
                                ) {
                                    OutlinedTextField(
                                        value = statusProgres,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Status Progres Rencana") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedStatus,
                                        onDismissRequest = { expandedStatus = false }
                                    ) {
                                        statusOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    statusProgres = option
                                                    expandedStatus = false
                                                }
                                            )
                                        }
                                    }
                                }

                                if (statusProgres == "Selesai") {
                                    DatePickerField(
                                        value = if (tanggalRealisasi == "-") today else tanggalRealisasi,
                                        onDateSelected = { tanggalRealisasi = it },
                                        label = "Tanggal Realisasi Selesai"
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAHAP 3: CATATAN & ANGGARAN FLEKSIBEL (OPSIONAL)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PastelButterYellow.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = PastelButterYellowDark, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tahap 3: Catatan & Anggaran Fleksibel (Opsional)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelButterYellowDark
                                    )
                                }

                                OutlinedTextField(
                                    value = spesifikasi,
                                    onValueChange = { spesifikasi = it },
                                    label = { Text("Keterangan / Spesifikasi Singkat (Opsional)") },
                                    placeholder = { Text("Misal: Spesifikasi barang 2 PK garansi resmi, lokasi Gedung Utama LT 2...") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = estimasiAnggaran,
                                    onValueChange = { input ->
                                        val digitsOnly = input.filter { it.isDigit() }
                                        estimasiAnggaran = if (digitsOnly.isNotEmpty()) {
                                            val parsed = digitsOnly.toLongOrNull()
                                            if (parsed != null) {
                                                "Rp. ${java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(parsed)}"
                                            } else input
                                        } else ""
                                    },
                                    label = { Text("Estimasi Biaya / Anggaran (Opsional)") },
                                    placeholder = { Text("Contoh: 15.000") },
                                    leadingIcon = { Text("Rp.", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Lampiran Foto Dokumentasi (Opsional)
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.AddPhotoAlternate,
                                                    contentDescription = null,
                                                    tint = PastelSkyBlueDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Lampiran Foto Dokumentasi (Opsional)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            }

                                            if (fotoUrl.isNotBlank()) {
                                                OutlinedButton(
                                                    onClick = { fotoUrl = "" },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelPeachDark),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("Hapus Foto", fontSize = 10.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (fotoUrl.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                            ) {
                                                AsyncImage(
                                                    model = fotoUrl,
                                                    contentDescription = "Foto Dokumentasi",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { imagePickerLauncher.launch("image/*") },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Pilih Foto", fontSize = 11.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Atau contoh foto cepat:", fontSize = 10.sp, color = TextSecondary)
                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                samplePhotos.forEach { (label, url) ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(PastelSkyBlueContainer)
                                                            .clickable { fotoUrl = url }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = PastelSkyBlueDark)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Buttons (Kembali / Lanjut / Simpan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep -= 1 },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kembali", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal", fontSize = 12.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (currentStep < 3) {
                            Button(
                                onClick = {
                                    if (agenda.isBlank()) {
                                        showErrorValidation = true
                                    } else {
                                        showErrorValidation = false
                                        currentStep += 1
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Lanjut", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        } else {
                            Button(
                                onClick = doSubmit,
                                colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (initialPlan != null) "Simpan Perubahan" else "Simpan Agenda", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
