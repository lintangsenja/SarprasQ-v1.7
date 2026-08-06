package com.lintang.sarprasq.ui.components

import com.lintang.sarprasq.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.lintang.sarprasq.util.ImageCompressor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.lintang.sarprasq.data.model.DynamicDamageItem
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.parseDamageItemsFromReport
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHelpdeskDialog(
    initialReport: HelpdeskReport? = null,
    ruangList: List<String> = emptyList(),
    urgensiList: List<String> = emptyList(),
    statusList: List<String> = emptyList(),
    kategoriList: List<String> = emptyList(),
    onAddKategoriToMaster: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        pelapor: String,
        lokasi: String,
        deskripsi: String,
        urgensi: String,
        status: String,
        tindakan: String,
        alasanPending: String?,
        estimasiEksekusi: String?,
        tanggal: String,
        kategori: String,
        fotoUrl: String?,
        catatan: String?,
        petugas: String?
    ) -> Unit
) {
    val defaultRuang = listOf("Lab Komputer 1", "Lab Komputer 2", "Lab IPA", "Ruang Guru", "Ruang Kepala Sekolah", "Kelas X-A", "Kelas XI-IPA 1", "Perpustakaan", "Aula Utama", "Gudang Sarpras")
    val defaultUrgensi = listOf("Darurat", "Penting", "Sedang", "Rendah")
    val defaultStatus = listOf("Catat", "Laporan Masuk", "Segera", "Pending", "Proses", "Selesai", "Ditolak")
    val defaultKategori = listOf(
        "Elektronik & Audio Visual",
        "IT & Komputer",
        "Mebel / Furnitur",
        "Perlengkapan Kebersihan & Rumah Tangga",
        "Alat Laboratorium / Praktikum",
        "Alat Olahraga",
        "Tools & Perkakas",
        "ATK & Perlengkapan Kantor"
    )

    val effectiveRuangList = if (ruangList.isNotEmpty()) ruangList else defaultRuang
    val effectiveUrgensiList = if (urgensiList.isNotEmpty()) urgensiList else defaultUrgensi
    val effectiveStatusList = remember(statusList) {
        val base = if (statusList.isNotEmpty()) statusList else defaultStatus
        val list = base.toMutableList()
        if (!list.contains("Catat")) {
            list.add(0, "Catat")
        }
        list.distinct()
    }
    val effectiveKategoriList = kategoriList

    var pelapor by remember { mutableStateOf(if (!initialReport?.pelapor.isNullOrBlank()) initialReport!!.pelapor else "Kevin Ricky Utama, S.Kom.") }
    var lokasi by remember { mutableStateOf(initialReport?.lokasi ?: "") }
    var status by remember { mutableStateOf(initialReport?.status ?: effectiveStatusList.firstOrNull { it.contains("Proses", ignoreCase = true) } ?: effectiveStatusList.firstOrNull() ?: "Proses") }
    var tindakan by remember { mutableStateOf(initialReport?.tindakan ?: "") }
    var alasanPending by remember { mutableStateOf(initialReport?.alasanPending ?: "") }
    var estimasiEksekusi by remember { mutableStateOf(initialReport?.estimasiEksekusi ?: "") }
    var fotoUrl by remember { mutableStateOf(initialReport?.fotoUrl ?: "") }
    var catatan by remember { mutableStateOf(initialReport?.catatan ?: "") }
    var petugas by remember { mutableStateOf(if (!initialReport?.petugas.isNullOrBlank()) initialReport!!.petugas else "Kevin Ricky Utama, S.Kom.") }

    var damageItems by remember {
        mutableStateOf(
            parseDamageItemsFromReport(
                initialReport,
                effectiveKategoriList.firstOrNull() ?: "Elektronik & Audio Visual",
                effectiveUrgensiList.firstOrNull() ?: "Sedang"
            )
        )
    }

    var tanggal by remember {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        mutableStateOf(initialReport?.tanggal ?: sdf.format(java.util.Date()))
    }

    var expandedRuang by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var uploadStatusMsg by remember { mutableStateOf<String?>(null) }
    var compressedDetails by remember { mutableStateOf<String?>(null) }

    // Gallery Picker Launcher with automatic compression (<400 KB) & Firebase Storage upload
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploadingPhoto = true
                val (urlResult, compressedInfo) = ImageCompressor.processAndUploadPhoto(
                    context = context,
                    imageUri = selectedUri,
                    folder = "bukti_kerusakan",
                    onStatusUpdate = { status -> uploadStatusMsg = status }
                )
                fotoUrl = urlResult
                if (compressedInfo != null) {
                    val kb = String.format(java.util.Locale.US, "%.1f", compressedInfo.sizeInKb)
                    compressedDetails = "Ukuran: ${kb} KB (Mentok <400 KB) • Rasio: ${compressedInfo.finalWidth}x${compressedInfo.finalHeight} px"
                }
                isUploadingPhoto = false
            }
        }
    }

    // Sample photos for quick preview selection
    val samplePhotos = listOf(
        Pair("Proyektor Rusak", "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500&q=80"),
        Pair("AC Leak / Bocor", "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=500&q=80"),
        Pair("Sound Amplifier", "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=500&q=80"),
        Pair("Kran Patah", "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=500&q=80")
    )

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
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialReport != null) "Edit Laporan Helpdesk" else "Laporan Inspeksi Ruangan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pilih lokasi ruangan & input multi-kerusakan",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Pelapor
                OutlinedTextField(
                    value = pelapor,
                    onValueChange = { pelapor = it },
                    label = { Text("Pelapor (Guru / Staf / Inspektor)") },
                    placeholder = { Text("Contoh: Bu Siti (Guru IPA)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Lokasi Ruangan Dropdown (Acuan Utama)
                ExposedDropdownMenuBox(
                    expanded = expandedRuang,
                    onExpandedChange = { expandedRuang = !expandedRuang }
                ) {
                    OutlinedTextField(
                        value = lokasi,
                        onValueChange = { lokasi = it },
                        label = { Text("Lokasi Ruangan (Acuan Utama)") },
                        placeholder = { Text("Pilih atau ketik lokasi ruangan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRuang) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRuang,
                        onDismissRequest = { expandedRuang = false }
                    ) {
                        effectiveRuangList.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    lokasi = option
                                    expandedRuang = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- 3. DAFTAR KERUSAKAN DINAMIS (MULTI-ISSUE PER ROOM) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PastelBackground)
                        .border(1.dp, PastelCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daftar Kerusakan / Temuan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Inspeksi multi-kerusakan dalam ruangan ini",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PastelSkyBlueContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${damageItems.size} Item",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelSkyBlueDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        damageItems.forEachIndexed { index, item ->
                            var expandedKategoriItem by remember(item.id) { mutableStateOf(false) }
                            var expandedUrgensiItem by remember(item.id) { mutableStateOf(false) }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(PastelSkyBlueDark),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Item Kerusakan #${index + 1}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }

                                        if (damageItems.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    damageItems = damageItems.filterIndexed { i, _ -> i != index }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus Baris",
                                                    tint = PastelPeachDark
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Kategori Dropdown Item
                                    ExposedDropdownMenuBox(
                                        expanded = expandedKategoriItem,
                                        onExpandedChange = { expandedKategoriItem = !expandedKategoriItem }
                                    ) {
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        val isCustom = item.kategori.isNotBlank() && !effectiveKategoriList.any { it.equals(item.kategori.trim(), ignoreCase = true) }
                                        OutlinedTextField(
                                            value = item.kategori,
                                            onValueChange = { newKat ->
                                                damageItems = damageItems.mapIndexed { i, d ->
                                                    if (i == index) d.copy(kategori = newKat) else d
                                                }
                                                expandedKategoriItem = true
                                            },
                                            readOnly = false,
                                            label = { Text("Kategori / Jenis Kerusakan", fontSize = 11.sp) },
                                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = PastelSkyBlueDark, modifier = Modifier.size(18.dp)) },
                                            trailingIcon = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (isCustom && onAddKategoriToMaster != null) {
                                                        IconButton(
                                                            onClick = {
                                                                onAddKategoriToMaster(item.kategori.trim())
                                                                android.widget.Toast.makeText(context, "Kategori '${item.kategori.trim()}' disimpan ke Master", android.widget.Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.AddCircle, contentDescription = "Simpan ke Master", tint = PastelMintDark, modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategoriItem)
                                                }
                                            },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expandedKategoriItem,
                                            onDismissRequest = { expandedKategoriItem = false }
                                        ) {
                                            if (isCustom && onAddKategoriToMaster != null) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = PastelMintDark, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("Tambah \"${item.kategori.trim()}\" ke Master", color = PastelMintDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                        }
                                                    },
                                                    onClick = {
                                                        onAddKategoriToMaster(item.kategori.trim())
                                                        expandedKategoriItem = false
                                                        android.widget.Toast.makeText(context, "Kategori '${item.kategori.trim()}' disimpan ke Master", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                                HorizontalDivider()
                                            }
                                            effectiveKategoriList.filter { it.contains(item.kategori, ignoreCase = true) }.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option, fontSize = 12.sp) },
                                                    onClick = {
                                                        damageItems = damageItems.mapIndexed { i, d ->
                                                            if (i == index) d.copy(kategori = option) else d
                                                        }
                                                        expandedKategoriItem = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Deskripsi Spesifik Item
                                    OutlinedTextField(
                                        value = item.deskripsi,
                                        onValueChange = { newDesc ->
                                            damageItems = damageItems.mapIndexed { i, d ->
                                                if (i == index) d.copy(deskripsi = newDesc) else d
                                            }
                                        },
                                        label = { Text("Deskripsi Kerusakan Spesifik", fontSize = 11.sp) },
                                        placeholder = { Text("Misal: Proyektor utama mati total", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 1,
                                        maxLines = 3,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Urgensi Dropdown Item
                                    ExposedDropdownMenuBox(
                                        expanded = expandedUrgensiItem,
                                        onExpandedChange = { expandedUrgensiItem = !expandedUrgensiItem }
                                    ) {
                                        OutlinedTextField(
                                            value = item.urgensi,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Tingkat Urgensi Item", fontSize = 11.sp) },
                                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = PastelPeachDark, modifier = Modifier.size(18.dp)) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUrgensiItem) },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expandedUrgensiItem,
                                            onDismissRequest = { expandedUrgensiItem = false }
                                        ) {
                                            effectiveUrgensiList.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option, fontSize = 12.sp) },
                                                    onClick = {
                                                        damageItems = damageItems.mapIndexed { i, d ->
                                                            if (i == index) d.copy(urgensi = option) else d
                                                        }
                                                        expandedUrgensiItem = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TOMBOL TAMBAH BARIS (+)
                        OutlinedButton(
                            onClick = {
                                damageItems = damageItems + DynamicDamageItem(
                                    kategori = effectiveKategoriList.firstOrNull() ?: "Elektronik & Audio Visual",
                                    urgensi = "Sedang"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PastelSkyBlueDark),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelSkyBlueDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Tambah Kerusakan Lain", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Catatan / Keterangan Tambahan
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan / Kronologi Tambahan (Opsional)") },
                    placeholder = { Text("Contoh: Terjadi saat jam pelajaran ke-3, asap tipis keluar...") },
                    leadingIcon = { Icon(Icons.Default.Note, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Nama / Kontak Petugas
                OutlinedTextField(
                    value = petugas,
                    onValueChange = { petugas = it },
                    label = { Text("Nama / Kontak Petugas Penanggung Jawab (Opsional)") },
                    placeholder = { Text("Contoh: Pak Bambang (Teknisi) / 0812-3456-7890") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PastelSkyBlueDark) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Unggah / Lampiran Foto Kerusakan
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PastelBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Lampiran Foto Kerusakan (Opsional)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            if (fotoUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { fotoUrl = "" },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelPeachDark),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Hapus Foto", fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isUploadingPhoto) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PastelSkyBlueContainer, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PastelSkyBlueDark,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = uploadStatusMsg ?: "Memproses kompresi & mengunggah foto...",
                                    fontSize = 11.sp,
                                    color = PastelSkyBlueDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else if (fotoUrl.isNotBlank()) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = fotoUrl,
                                        contentDescription = "Foto Kerusakan",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                if (uploadStatusMsg != null || compressedDetails != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(PastelMintLight, RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        uploadStatusMsg?.let { msg ->
                                            Text(
                                                text = msg,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PastelMintDark
                                            )
                                        }
                                        compressedDetails?.let { det ->
                                            Text(
                                                text = det,
                                                fontSize = 9.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
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
                                    Text("Pilih dari Galeri", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Atau pilih contoh foto cepat:", fontSize = 10.sp, color = TextSecondary)
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

                Spacer(modifier = Modifier.height(14.dp))

                // Status Penanganan Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Penanganan") },
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
                        effectiveStatusList.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tindakan Penanganan
                OutlinedTextField(
                    value = tindakan,
                    onValueChange = { tindakan = it },
                    label = { Text("Tindakan Penanganan Awal") },
                    placeholder = { Text("Tindakan yang telah atau akan dilakukan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (status.equals("Pending", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = alasanPending,
                        onValueChange = { alasanPending = it },
                        label = { Text("Alasan Tertunda (Pending Reason)") },
                        placeholder = { Text("Contoh: Menunggu teknisi luar / suku cadang") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DatePickerField(
                        value = estimasiEksekusi,
                        onDateSelected = { estimasiEksekusi = it },
                        label = "Estimasi Tanggal Eksekusi",
                        placeholder = "DD/MM/YYYY"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tanggal Laporan
                DatePickerField(
                    value = tanggal,
                    onDateSelected = { tanggal = it },
                    label = "Tanggal Laporan"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Simpan Sekaligus / Batch Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val validItems = damageItems.filter { it.deskripsi.isNotBlank() }
                            val finalItems = if (validItems.isNotEmpty()) validItems else listOf(
                                DynamicDamageItem(
                                    kategori = effectiveKategoriList.firstOrNull() ?: "Elektronik & Audio Visual",
                                    deskripsi = "Laporan inspeksi kerusakan ruangan",
                                    urgensi = "Sedang"
                                )
                            )

                            if (pelapor.isNotBlank() && lokasi.isNotBlank()) {
                                val combinedDeskripsi = if (finalItems.size == 1) {
                                    val single = finalItems.first()
                                    "[${single.kategori}] ${single.deskripsi}"
                                } else {
                                    finalItems.joinToString("\n") { item ->
                                        "• [${item.kategori}] ${item.deskripsi} (Urgensi: ${item.urgensi})"
                                    }
                                }

                                val hasDarurat = finalItems.any { it.urgensi.equals("Darurat", ignoreCase = true) }
                                val hasPenting = finalItems.any { it.urgensi.equals("Penting", ignoreCase = true) }
                                val calculatedUrgensi = when {
                                    hasDarurat -> "Darurat"
                                    hasPenting -> "Penting"
                                    else -> finalItems.firstOrNull()?.urgensi ?: "Sedang"
                                }

                                val distinctCategories = finalItems.map { it.kategori }.distinct()
                                val calculatedKategori = if (distinctCategories.size == 1) {
                                    distinctCategories.first()
                                } else {
                                    "Multi-Kerusakan (${distinctCategories.size} Jenis)"
                                }

                                onSubmit(
                                    pelapor,
                                    lokasi,
                                    combinedDeskripsi,
                                    calculatedUrgensi,
                                    status,
                                    tindakan,
                                    if (status.equals("Pending", ignoreCase = true)) alasanPending else null,
                                    if (status.equals("Pending", ignoreCase = true)) estimasiEksekusi else null,
                                    tanggal,
                                    calculatedKategori,
                                    fotoUrl.ifBlank { null },
                                    catatan.ifBlank { null },
                                    petugas.ifBlank { null }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (initialReport != null) "Simpan Perubahan" else "Simpan Laporan")
                    }
                }
            }
        }
    }
}

