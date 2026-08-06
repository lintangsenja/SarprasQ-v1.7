package com.lintang.sarprasq.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.lintang.sarprasq.data.model.DamageReport
import com.lintang.sarprasq.data.model.Ruang
import androidx.compose.material3.Divider
import com.lintang.sarprasq.ui.components.AddRoomIncidentDialog
import com.lintang.sarprasq.ui.components.DatePickerField
import com.lintang.sarprasq.ui.theme.*
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel

@Composable
fun RoomDamageScreen(
    viewModel: SarprasViewModel,
    modifier: Modifier = Modifier
) {
    val ruangList by viewModel.allRuang.collectAsState()
    val damageList by viewModel.allDamageReports.collectAsState()
    val statusMasterList by viewModel.allStatusPenanganan.collectAsState()
    val kategoriMasterList by viewModel.allKategori.collectAsState()

    var selectedRoomId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var roomCategoryFilter by remember { mutableStateOf("Semua") }
    var statusPriorityFilter by remember { mutableStateOf("Semua Status") } // "Semua Status", "Segera ditangani", "Dalam Perbaikan", "Pending", "Selesai diperbaiki", "Tidak bisa diperbaiki"
    var mainViewMode by remember { mutableStateOf("multi_card") } // "multi_card" or "overview"

    var showAddDialog by remember { mutableStateOf(false) }
    var showMultiAddDialog by remember { mutableStateOf(false) }
    var preselectedRoomForAdd by remember { mutableStateOf<Ruang?>(null) }
    var editingReport by remember { mutableStateOf<DamageReport?>(null) }
    var deletingReport by remember { mutableStateOf<DamageReport?>(null) }

    val statusOptions = if (statusMasterList.isNotEmpty()) {
        statusMasterList.map { it.namaStatus }
    } else {
        listOf("Segera ditangani", "Dalam Perbaikan", "Pending", "Selesai diperbaiki", "Tidak bisa diperbaiki")
    }

    val selectedRoom = ruangList.find { it.id == selectedRoomId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PastelBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Banner
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedRoom != null) {
                        // Room Detail Top Row Navigation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { selectedRoomId = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke Daftar Ruang", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedRoom.namaRuang,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Kode: ${selectedRoom.kodeRuang} • ${selectedRoom.kategori} • PJ: ${selectedRoom.penanggungJawab}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        // Main Overview Top Row
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Kerusakan Per Ruangan",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Manajemen item kerusakan berbasis ruang & prioritas admin",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari ruang, kode, atau item kerusakan...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Filter Chips by Category
                        val categories = listOf("Semua", "Laboratorium", "Ruang Kelas", "Ruang Kerja", "Fasilitas Umum", "Ada Urgent / Pending")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                val isSelected = roomCategoryFilter == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) PastelSkyBlueDark else PastelSkyBlueContainer
                                        )
                                        .clickable { roomCategoryFilter = cat }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // View Mode Switcher Row (Kartu Multi-Masalah vs Overview)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(PastelBackground)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (mainViewMode == "multi_card") PastelSkyBlueDark else Color.Transparent)
                                    .clickable { mainViewMode = "multi_card" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Kartu Multi-Masalah (Eksekusi Parsial)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mainViewMode == "multi_card") Color.White else TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (mainViewMode == "overview") PastelSkyBlueDark else Color.Transparent)
                                    .clickable { mainViewMode = "overview" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Master Ruangan (Ringkasan)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mainViewMode == "overview") Color.White else TextSecondary
                                )
                            }
                        }

                        if (mainViewMode == "multi_card") {
                            Spacer(modifier = Modifier.height(10.dp))
                            // Status Priority Filter Tabs for Multi-Item Cards
                            val statusTabs = listOf("Semua Status", "Segera ditangani", "Dalam Perbaikan", "Pending", "Selesai diperbaiki", "Tidak bisa diperbaiki")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(statusTabs) { tab ->
                                    val isSelected = statusPriorityFilter == tab
                                    val (badgeBg, badgeText) = when (tab) {
                                        "Segera ditangani" -> Pair(PastelPeach.copy(alpha = 0.5f), PastelPeachDark)
                                        "Dalam Perbaikan" -> Pair(PastelButterYellow, TextPrimary)
                                        "Pending" -> Pair(PastelLavender, TextPrimary)
                                        "Selesai diperbaiki" -> Pair(PastelMint, PastelMintDark)
                                        "Tidak bisa diperbaiki" -> Pair(Color(0xFFFFD8D8), Color(0xFFC62828))
                                        else -> Pair(PastelSkyBlueContainer, PastelSkyBlueDark)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) badgeText else badgeBg)
                                            .clickable { statusPriorityFilter = tab }
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = tab,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else badgeText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            if (selectedRoom != null) {
                // VIEW 2: Detail Items in Selected Room
                RoomDetailDamageView(
                    room = selectedRoom,
                    damageList = damageList.filter { it.roomId == selectedRoom.id },
                    statusPriorityFilter = statusPriorityFilter,
                    onStatusPriorityFilterChange = { statusPriorityFilter = it },
                    statusOptions = statusOptions,
                    onUpdateStatus = { report -> editingReport = report },
                    onDeleteReport = { report -> deletingReport = report },
                    onQuickStatusChange = { report, newStatus ->
                        viewModel.updateDamageReportStatus(
                            report = report,
                            newStatus = newStatus,
                            newDitanganiOleh = report.ditanganiOleh,
                            newTanggalSelesai = if (newStatus.contains("Selesai", ignoreCase = true)) "01/08/2026" else report.tanggalSelesai,
                            newKeterangan = report.keterangan
                        )
                    }
                )
            } else if (mainViewMode == "multi_card") {
                // VIEW 1: Multi-Item Room Incident Cards with Partial Execution
                val matchingReports = damageList.filter { report ->
                    val matchesQuery = searchQuery.isEmpty() ||
                            report.namaRuang.contains(searchQuery, ignoreCase = true) ||
                            report.namaItemKerusakan.contains(searchQuery, ignoreCase = true) ||
                            report.namaPelapor.contains(searchQuery, ignoreCase = true)

                    val matchesCategory = when (roomCategoryFilter) {
                        "Semua" -> true
                        "Ada Urgent / Pending" -> report.statusPenanganan.equals("Segera ditangani", ignoreCase = true) || report.statusPenanganan.equals("Pending", ignoreCase = true)
                        else -> {
                            val r = ruangList.find { it.id == report.roomId }
                            r?.kategori?.equals(roomCategoryFilter, ignoreCase = true) == true
                        }
                    }

                    val matchesStatus = if (statusPriorityFilter == "Semua Status") true else report.statusPenanganan.equals(statusPriorityFilter, ignoreCase = true)

                    matchesQuery && matchesCategory && matchesStatus
                }

                val groupedByRoom = matchingReports.groupBy { it.roomId }

                if (groupedByRoom.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(56.dp), tint = TextSecondary.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tidak ada kartu laporan kerusakan ditemukan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Cobalah mengubah filter status prioritas atau kata kunci pencarian.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(groupedByRoom.keys.toList()) { roomId ->
                            val roomReports = groupedByRoom[roomId] ?: emptyList()
                            val roomObj = ruangList.find { it.id == roomId }
                            val roomName = roomObj?.namaRuang ?: (roomReports.firstOrNull()?.namaRuang ?: "Ruangan $roomId")
                            val roomCode = roomObj?.kodeRuang ?: ""
                            val penanggungJawab = roomObj?.penanggungJawab ?: "PJ Ruangan"

                            RoomIncidentMultiCard(
                                roomName = roomName,
                                roomCode = roomCode,
                                penanggungJawab = penanggungJawab,
                                reports = roomReports,
                                onUpdateStatus = { report -> editingReport = report },
                                onDeleteReport = { report -> deletingReport = report },
                                onQuickStatusChange = { report, newStatus ->
                                    viewModel.updateDamageReportStatus(
                                        report = report,
                                        newStatus = newStatus,
                                        newDitanganiOleh = report.ditanganiOleh,
                                        newTanggalSelesai = if (newStatus.contains("Selesai", ignoreCase = true)) "01/08/2026" else report.tanggalSelesai,
                                        newKeterangan = report.keterangan
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                // VIEW 3: Overview List of Rooms with Damage Summary
                val filteredRooms = ruangList.filter { r ->
                    val matchesQuery = searchQuery.isEmpty() ||
                            r.namaRuang.contains(searchQuery, ignoreCase = true) ||
                            r.kodeRuang.contains(searchQuery, ignoreCase = true) ||
                            damageList.any { d -> d.roomId == r.id && d.namaItemKerusakan.contains(searchQuery, ignoreCase = true) }

                    val roomDamages = damageList.filter { it.roomId == r.id }
                    val matchesCategory = when (roomCategoryFilter) {
                        "Semua" -> true
                        "Ada Urgent / Pending" -> roomDamages.any { d ->
                            d.statusPenanganan.equals("Segera ditangani", ignoreCase = true) ||
                                    d.statusPenanganan.equals("Pending", ignoreCase = true)
                        }
                        else -> r.kategori.equals(roomCategoryFilter, ignoreCase = true)
                    }

                    matchesQuery && matchesCategory
                }

                if (filteredRooms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(56.dp), tint = TextSecondary.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tidak ada ruangan yang ditemukan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Coba atur kata kunci pencarian atau filter kategori di atas.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredRooms) { room ->
                            val roomDamages = damageList.filter { it.roomId == room.id }
                            RoomOverviewCard(
                                room = room,
                                damageReports = roomDamages,
                                onClick = { selectedRoomId = room.id }
                            )
                        }
                    }
                }
            }
        }

        // FAB for quick room incident addition
        FloatingActionButton(
            onClick = {
                preselectedRoomForAdd = selectedRoom
                showMultiAddDialog = true
            },
            containerColor = PastelSkyBlueDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Lapor Kerusakan Ruangan", modifier = Modifier.size(24.dp))
        }
    }

    // --- DIALOGS ---
    if (showMultiAddDialog) {
        AddRoomIncidentDialog(
            ruangMasterList = ruangList,
            kategoriMasterList = kategoriMasterList.map { it.namaKategori },
            preSelectedRuangId = preselectedRoomForAdd?.id,
            onAddKategoriToMaster = { viewModel.addKategori(it) },
            onDismiss = { showMultiAddDialog = false },
            onSubmit = { roomId, namaRuang, pelapor, tgl, items ->
                val convertedItems = items.map {
                    com.lintang.sarprasq.ui.viewmodel.RoomIncidentItemInput(
                        namaItemKerusakan = it.namaItem,
                        kategori = it.kategori,
                        urgensi = it.urgensi,
                        keterangan = it.keterangan
                    )
                }
                viewModel.addMultiDamageReport(
                    roomId = roomId,
                    namaRuang = namaRuang,
                    namaPelapor = pelapor,
                    tanggalLapor = tgl,
                    items = convertedItems
                )
                showMultiAddDialog = false
            }
        )
    }

    if (showAddDialog) {
        AddDamageReportDialog(
            ruangList = ruangList,
            preselectedRoom = preselectedRoomForAdd,
            statusOptions = statusOptions,
            onDismiss = { showAddDialog = false },
            onSubmit = { roomId, namaRuang, pelapor, item, tgl, status, ditangani, ket, fotoUrl ->
                viewModel.addDamageReport(
                    roomId = roomId,
                    namaRuang = namaRuang,
                    namaPelapor = pelapor,
                    namaItemKerusakan = item,
                    tanggalLapor = tgl,
                    statusPenanganan = status,
                    ditanganiOleh = ditangani,
                    keterangan = ket,
                    fotoUrl = fotoUrl
                )
                showAddDialog = false
            }
        )
    }

    editingReport?.let { report ->
        UpdateDamageStatusDialog(
            report = report,
            statusOptions = statusOptions,
            onDismiss = { editingReport = null },
            onSubmit = { newStatus, newDitangani, newTglSelesai, newKet ->
                viewModel.updateDamageReportStatus(
                    report = report,
                    newStatus = newStatus,
                    newDitanganiOleh = newDitangani,
                    newTanggalSelesai = newTglSelesai,
                    newKeterangan = newKet
                )
                editingReport = null
            }
        )
    }

    deletingReport?.let { report ->
        AlertDialog(
            onDismissRequest = { deletingReport = null },
            title = { Text("Hapus Item Kerusakan", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus catatan kerusakan '${report.namaItemKerusakan}' di ${report.namaRuang}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDamageReport(report)
                        deletingReport = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingReport = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun RoomOverviewCard(
    room: Ruang,
    damageReports: List<DamageReport>,
    onClick: () -> Unit
) {
    val totalItems = damageReports.size
    val urgentCount = damageReports.count { d ->
        d.statusPenanganan.equals("Segera ditangani", ignoreCase = true) ||
                d.statusPenanganan.equals("Pending", ignoreCase = true)
    }
    val inProgressCount = damageReports.count { d -> d.statusPenanganan.equals("Dalam Perbaikan", ignoreCase = true) }
    val doneCount = damageReports.count { d -> d.statusPenanganan.equals("Selesai diperbaiki", ignoreCase = true) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelSkyBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = PastelSkyBlueDark)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = room.namaRuang,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PastelLavender.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(room.kodeRuang, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Text(
                            text = "Kategori: ${room.kategori} • Penanggung Jawab: ${room.penanggungJawab}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Damage Stat Summary Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Total Items Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelBackground)
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Total Item", fontSize = 10.sp, color = TextSecondary)
                        Text("$totalItems item", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                // Urgent/Pending Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (urgentCount > 0) PastelPeach.copy(alpha = 0.4f) else PastelBackground)
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Urgent / Pending", fontSize = 10.sp, color = if (urgentCount > 0) PastelPeachDark else TextSecondary)
                        Text("$urgentCount item", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (urgentCount > 0) PastelPeachDark else TextPrimary)
                    }
                }

                // In Progress Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (inProgressCount > 0) PastelButterYellow else PastelBackground)
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Diperbaiki", fontSize = 10.sp, color = TextSecondary)
                        Text("$inProgressCount item", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                // Done Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (doneCount > 0) PastelMint.copy(alpha = 0.5f) else PastelBackground)
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Selesai", fontSize = 10.sp, color = if (doneCount > 0) PastelMintDark else TextSecondary)
                        Text("$doneCount item", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (doneCount > 0) PastelMintDark else TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (urgentCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = PastelPeachDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Membutuhkan tindak lanjut admin", fontSize = 11.sp, color = PastelPeachDark, fontWeight = FontWeight.Medium)
                    }
                } else if (totalItems == 0) {
                    Text("Belum ada laporan kerusakan", fontSize = 11.sp, color = TextSecondary)
                } else {
                    Text("Kondisi fasilitas terpantau baik", fontSize = 11.sp, color = PastelMintDark, fontWeight = FontWeight.Medium)
                }

                Text(
                    text = "Buka Detail >",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelSkyBlueDark
                )
            }
        }
    }
}

@Composable
fun RoomDetailDamageView(
    room: Ruang,
    damageList: List<DamageReport>,
    statusPriorityFilter: String,
    onStatusPriorityFilterChange: (String) -> Unit,
    statusOptions: List<String>,
    onUpdateStatus: (DamageReport) -> Unit,
    onDeleteReport: (DamageReport) -> Unit,
    onQuickStatusChange: ((DamageReport, String) -> Unit)? = null
) {
    val totalItems = damageList.size
    val pendingCount = damageList.count { it.statusPenanganan.equals("Pending", ignoreCase = true) }
    val urgentCount = damageList.count { it.statusPenanganan.equals("Segera ditangani", ignoreCase = true) }
    val inProgressCount = damageList.count { it.statusPenanganan.equals("Dalam Perbaikan", ignoreCase = true) }
    val doneCount = damageList.count { it.statusPenanganan.equals("Selesai diperbaiki", ignoreCase = true) }
    val cannotFixCount = damageList.count { it.statusPenanganan.equals("Tidak bisa diperbaiki", ignoreCase = true) }

    val filterOptions = listOf("Semua Status") + statusOptions

    val filteredList = damageList.filter { d ->
        if (statusPriorityFilter == "Semua Status") true
        else d.statusPenanganan.equals(statusPriorityFilter, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Ringkasan Status Ruangan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DamageStatPill("Total Item", "$totalItems", PastelBackground, TextPrimary)
                    DamageStatPill("Urgent", "$urgentCount", PastelPeach.copy(alpha = 0.5f), PastelPeachDark)
                    DamageStatPill("Pending", "$pendingCount", PastelLavender, TextPrimary)
                    DamageStatPill("Diperbaiki", "$inProgressCount", PastelButterYellow, TextPrimary)
                    DamageStatPill("Selesai", "$doneCount", PastelMint.copy(alpha = 0.5f), PastelMintDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Admin Priority Filter Tabs
        Text(
            text = "FILTER PRIORITAS PENANGANAN ADMIN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            items(filterOptions) { filter ->
                val isSelected = statusPriorityFilter == filter
                val count = if (filter == "Semua Status") totalItems else damageList.count { it.statusPenanganan.equals(filter, ignoreCase = true) }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) PastelSkyBlueDark else Color.White
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PastelSkyBlueDark else TextSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onStatusPriorityFilterChange(filter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$filter ($count)",
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of Multi-Item Damage Reports in Table/Card Format
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = PastelMintDark)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Tidak ada item kerusakan dengan status ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Seluruh item fasilitas ruangan dalam kondisi terkelola dengan baik.", fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(filteredList) { report ->
                    DamageItemCard(
                        report = report,
                        onUpdateStatus = { onUpdateStatus(report) },
                        onDeleteReport = { onDeleteReport(report) },
                        onQuickStatusChange = { newStatus -> onQuickStatusChange?.invoke(report, newStatus) }
                    )
                }
            }
        }
    }
}

@Composable
fun DamageStatPill(
    label: String,
    value: String,
    bg: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = TextSecondary)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun DamageItemCard(
    report: DamageReport,
    onUpdateStatus: () -> Unit,
    onDeleteReport: () -> Unit,
    onQuickStatusChange: ((newStatus: String) -> Unit)? = null
) {
    val statusColor = when (report.statusPenanganan.lowercase()) {
        "segera ditangani", "segera" -> PastelPeach.copy(alpha = 0.5f)
        "dalam perbaikan", "proses" -> PastelButterYellow
        "pending" -> PastelLavender
        "selesai diperbaiki", "selesai" -> PastelMint
        "tidak bisa diperbaiki", "ditolak" -> Color(0xFFFFD8D8)
        else -> PastelSkyBlueContainer
    }

    val statusTextColor = when (report.statusPenanganan.lowercase()) {
        "segera ditangani", "segera" -> PastelPeachDark
        "dalam perbaikan", "proses" -> TextPrimary
        "pending" -> TextPrimary
        "selesai diperbaiki", "selesai" -> PastelMintDark
        "tidak bisa diperbaiki", "ditolak" -> Color(0xFFC62828)
        else -> PastelSkyBlueDark
    }

    val (urgensiBg, urgensiText) = when (report.urgensi.lowercase()) {
        "darurat", "darurat / kritis" -> Pair(PastelPeach.copy(alpha = 0.6f), PastelPeachDark)
        "penting", "tinggi" -> Pair(PastelButterYellow, TextPrimary)
        "sedang" -> Pair(PastelSkyBlueContainer, PastelSkyBlueDark)
        else -> Pair(PastelMint.copy(alpha = 0.5f), PastelMintDark)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PastelCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Row 1: Item Name & Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PastelSkyBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = PastelSkyBlueDark, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = report.namaItemKerusakan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (report.kategori.isNotBlank()) {
                            Text(
                                text = "Kategori: ${report.kategori}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Urgensi Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(urgensiBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = report.urgensi,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = urgensiText
                        )
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = report.statusPenanganan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Details
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Pelapor: ${report.namaPelapor} • Tanggal: ${report.tanggalLapor}", fontSize = 11.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Text("Ditangani: ", fontSize = 11.sp, color = TextSecondary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (report.ditanganiOleh == "Teknisi Luar") PastelLavender else PastelSkyBlueContainer)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(report.ditanganiOleh, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        if (report.tanggalSelesai != "-") {
                            Text(" • Selesai: ${report.tanggalSelesai}", fontSize = 11.sp, color = PastelMintDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (report.keterangan.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PastelBackground)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Ket: ${report.keterangan}",
                        fontSize = 11.sp,
                        color = TextPrimary.copy(alpha = 0.85f)
                    )
                }
            }

            if (!report.fotoUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                ) {
                    AsyncImage(
                        model = report.fotoUrl,
                        contentDescription = "Foto Bukti Kerusakan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Quick Execution Buttons & Full Status Edit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDeleteReport,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = PastelPeachDark, modifier = Modifier.size(18.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick "Proses" execution button
                    if (!report.statusPenanganan.equals("Dalam Perbaikan", ignoreCase = true) && !report.statusPenanganan.equals("Selesai diperbaiki", ignoreCase = true)) {
                        OutlinedButton(
                            onClick = { onQuickStatusChange?.invoke("Dalam Perbaikan") },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PastelButterYellow),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Proses", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quick "Selesai" execution button
                    if (!report.statusPenanganan.equals("Selesai diperbaiki", ignoreCase = true)) {
                        OutlinedButton(
                            onClick = { onQuickStatusChange?.invoke("Selesai diperbaiki") },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PastelMintDark),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelMintDark),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Selesai", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Full Edit Status
                    Button(
                        onClick = onUpdateStatus,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update Status", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDamageReportDialog(
    ruangList: List<Ruang>,
    preselectedRoom: Ruang?,
    statusOptions: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (
        roomId: Int,
        namaRuang: String,
        pelapor: String,
        namaItem: String,
        tanggal: String,
        status: String,
        ditanganiOleh: String,
        keterangan: String,
        fotoUrl: String?
    ) -> Unit
) {
    var selectedRoom by remember { mutableStateOf(preselectedRoom ?: ruangList.firstOrNull()) }
    var namaPelapor by remember { mutableStateOf("") }
    var namaItemKerusakan by remember { mutableStateOf("") }
    var tanggalLapor by remember { mutableStateOf("01/08/2026") }
    var statusPenanganan by remember { mutableStateOf("Segera ditangani") }
    var ditanganiOleh by remember { mutableStateOf("Internal") } // "Internal" / "Teknisi Luar"
    var keterangan by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { fotoUrl = it.toString() }
    }

    val samplePhotos = listOf(
        "Layar Retak" to "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400",
        "AC Bocor" to "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=400"
    )

    var expandedRoom by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val quickItems = listOf("AC Ceiling", "Remot Proyektor", "Kipas Angin", "Saklar / Stopkontak", "Lampu Neon", "Pintu / Engsel", "Kran Air Sink", "Sound System")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Input Laporan Kerusakan Item",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Laporan fleksibel (mode ketik bebas & lampiran foto opsional).",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select Room
                ExposedDropdownMenuBox(
                    expanded = expandedRoom,
                    onExpandedChange = { expandedRoom = !expandedRoom }
                ) {
                    OutlinedTextField(
                        value = selectedRoom?.let { "${it.namaRuang} (${it.kodeRuang})" } ?: "Pilih Ruangan",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Ruangan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoom) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRoom,
                        onDismissRequest = { expandedRoom = false }
                    ) {
                        ruangList.forEach { r ->
                            DropdownMenuItem(
                                text = { Text("${r.namaRuang} (${r.kodeRuang})") },
                                onClick = {
                                    selectedRoom = r
                                    expandedRoom = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Item Name - Free Text
                OutlinedTextField(
                    value = namaItemKerusakan,
                    onValueChange = { namaItemKerusakan = it },
                    label = { Text("Nama Item / Jenis Barang (Ketik Bebas) *") },
                    placeholder = { Text("misal: AC Unit 1, Remot Proyektor, Kran Sink") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Item Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickItems) { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelSkyBlueContainer)
                                .clickable { namaItemKerusakan = item }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+ $item", fontSize = 10.sp, color = PastelSkyBlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = namaPelapor,
                        onValueChange = { namaPelapor = it },
                        label = { Text("Nama Pelapor *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    DatePickerField(
                        value = tanggalLapor,
                        onDateSelected = { tanggalLapor = it },
                        label = "Tanggal Lapor",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status & Ditangani Oleh
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = statusPenanganan,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Penanganan Awal") },
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
                        statusOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    statusPenanganan = opt
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ditangani Oleh Segment
                Text("Ditangani Oleh:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (ditanganiOleh == "Internal") PastelSkyBlueDark else PastelBackground)
                            .clickable { ditanganiOleh = "Internal" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tim Internal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (ditanganiOleh == "Internal") Color.White else TextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (ditanganiOleh == "Teknisi Luar") PastelLavender else PastelBackground)
                            .clickable { ditanganiOleh = "Teknisi Luar" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Teknisi Luar (Vendor)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan Detail Kerusakan") },
                    placeholder = { Text("misal: Komponen mati total, suara bising, dll.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Photo Attachment (Opsional)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PastelSkyBlueContainer.copy(alpha = 0.4f)),
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
                                    text = "Lampiran Foto Bukti (Opsional)",
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
                                    Text("Hapus", fontSize = 10.sp)
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
                                    contentDescription = "Foto Kerusakan",
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

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            selectedRoom?.let { r ->
                                if (namaItemKerusakan.isNotBlank() && namaPelapor.isNotBlank()) {
                                    onSubmit(
                                        r.id,
                                        r.namaRuang,
                                        namaPelapor,
                                        namaItemKerusakan,
                                        tanggalLapor,
                                        statusPenanganan,
                                        ditanganiOleh,
                                        keterangan,
                                        fotoUrl.ifBlank { null }
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedRoom != null && namaItemKerusakan.isNotBlank() && namaPelapor.isNotBlank()
                    ) {
                        Text("Simpan Laporan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDamageStatusDialog(
    report: DamageReport,
    statusOptions: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (
        newStatus: String,
        newDitanganiOleh: String,
        newTanggalSelesai: String,
        newKeterangan: String
    ) -> Unit
) {
    var statusPenanganan by remember { mutableStateOf(report.statusPenanganan) }
    var ditanganiOleh by remember { mutableStateOf(report.ditanganiOleh) }
    var tanggalSelesai by remember { mutableStateOf(if (report.tanggalSelesai == "-") "01/08/2026" else report.tanggalSelesai) }
    var keterangan by remember { mutableStateOf(report.keterangan) }

    var expandedStatus by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Update Penanganan Admin",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${report.namaItemKerusakan} @ ${report.namaRuang}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PastelSkyBlueDark,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = statusPenanganan,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Penanganan Baru") },
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
                        statusOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    statusPenanganan = opt
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ditangani Oleh
                Text("Eksekusi Oleh:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (ditanganiOleh == "Internal") PastelSkyBlueDark else PastelBackground)
                            .clickable { ditanganiOleh = "Internal" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tim Internal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (ditanganiOleh == "Internal") Color.White else TextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (ditanganiOleh == "Teknisi Luar") PastelLavender else PastelBackground)
                            .clickable { ditanganiOleh = "Teknisi Luar" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Teknisi Luar (Vendor)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                DatePickerField(
                    value = tanggalSelesai,
                    onDateSelected = { tanggalSelesai = it },
                    label = "Tanggal Selesai Penanganan",
                    placeholder = "DD/MM/YYYY"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan / Catatan Admin") },
                    placeholder = { Text("misal: Selesai diisi freon, Menunggu konfirmasi anggaran") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onSubmit(
                                statusPenanganan,
                                ditanganiOleh,
                                if (statusPenanganan.contains("Selesai", ignoreCase = true) && tanggalSelesai.isBlank()) "01/08/2026" else tanggalSelesai,
                                keterangan
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Status", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomIncidentMultiCard(
    roomName: String,
    roomCode: String,
    penanggungJawab: String,
    reports: List<DamageReport>,
    onUpdateStatus: (DamageReport) -> Unit,
    onDeleteReport: (DamageReport) -> Unit,
    onQuickStatusChange: (DamageReport, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PastelCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card Room Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelSkyBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = PastelSkyBlueDark)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = roomName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (roomCode.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PastelLavender.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(roomCode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                        Text(
                            text = "PJ: ${if (penanggungJawab.isNotBlank()) penanggungJawab else "PJ Ruangan"} • Total ${reports.size} Item Masalah",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PastelPeach.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${reports.size} Item",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelPeachDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = PastelCardBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(12.dp))

            // Multi-Item List inside Room Incident Card
            Text(
                text = "DAFTAR ITEM KERUSAKAN DI RUANGAN INI:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            reports.forEachIndexed { index, report ->
                RoomIncidentItemRow(
                    index = index + 1,
                    report = report,
                    onUpdateStatus = { onUpdateStatus(report) },
                    onDeleteReport = { onDeleteReport(report) },
                    onQuickStatusChange = { newStatus -> onQuickStatusChange(report, newStatus) }
                )
                if (index < reports.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun RoomIncidentItemRow(
    index: Int,
    report: DamageReport,
    onUpdateStatus: () -> Unit,
    onDeleteReport: () -> Unit,
    onQuickStatusChange: (newStatus: String) -> Unit
) {
    val statusColor = when (report.statusPenanganan.lowercase()) {
        "segera ditangani", "segera" -> PastelPeach.copy(alpha = 0.5f)
        "dalam perbaikan", "proses" -> PastelButterYellow
        "pending" -> PastelLavender
        "selesai diperbaiki", "selesai" -> PastelMint
        "tidak bisa diperbaiki", "ditolak" -> Color(0xFFFFD8D8)
        else -> PastelSkyBlueContainer
    }

    val statusTextColor = when (report.statusPenanganan.lowercase()) {
        "segera ditangani", "segera" -> PastelPeachDark
        "dalam perbaikan", "proses" -> TextPrimary
        "pending" -> TextPrimary
        "selesai diperbaiki", "selesai" -> PastelMintDark
        "tidak bisa diperbaiki", "ditolak" -> Color(0xFFC62828)
        else -> PastelSkyBlueDark
    }

    val (urgensiBg, urgensiText) = when (report.urgensi.lowercase()) {
        "darurat", "darurat / kritis" -> Pair(PastelPeach.copy(alpha = 0.6f), PastelPeachDark)
        "penting", "tinggi" -> Pair(PastelButterYellow, TextPrimary)
        "sedang" -> Pair(PastelSkyBlueContainer, PastelSkyBlueDark)
        else -> Pair(PastelMint.copy(alpha = 0.5f), PastelMintDark)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            // Row 1: Index + Item Name + Urgensi & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PastelSkyBlueDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("#$index", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = report.namaItemKerusakan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (report.kategori.isNotBlank()) {
                            Text("Kategori: ${report.kategori}", fontSize = 10.5.sp, color = TextSecondary)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Urgensi Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(urgensiBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(report.urgensi, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = urgensiText)
                    }
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(report.statusPenanganan, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = statusTextColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Pelapor: ${report.namaPelapor} (${report.tanggalLapor}) • PJ/Petugas: ${report.ditanganiOleh}",
                fontSize = 11.sp,
                color = TextSecondary
            )

            if (report.keterangan.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Text("Ket: ${report.keterangan}", fontSize = 10.5.sp, color = TextPrimary.copy(alpha = 0.9f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons for Partial Execution on THIS SPECIFIC ITEM
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDeleteReport,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = PastelPeachDark, modifier = Modifier.size(16.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!report.statusPenanganan.equals("Dalam Perbaikan", ignoreCase = true) && !report.statusPenanganan.equals("Selesai diperbaiki", ignoreCase = true)) {
                        Button(
                            onClick = { onQuickStatusChange("Dalam Perbaikan") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelButterYellowDark, contentColor = TextPrimary),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("⚡ Proses", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (!report.statusPenanganan.equals("Selesai diperbaiki", ignoreCase = true)) {
                        Button(
                            onClick = { onQuickStatusChange("Selesai diperbaiki") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark, contentColor = Color.White),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("✓ Selesai", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onUpdateStatus,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PastelSkyBlueDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelSkyBlueDark),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("✏️ Status", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
