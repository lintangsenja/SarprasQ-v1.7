package com.lintang.sarprasq.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lintang.sarprasq.data.model.DynamicDamageItem
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.parseDamageItemsFromReport
import com.lintang.sarprasq.ui.components.AddHelpdeskDialog
import com.lintang.sarprasq.ui.components.FilterDialog
import com.lintang.sarprasq.ui.components.FilterOptionGroup
import com.lintang.sarprasq.ui.components.FilterTriggerButton
import com.lintang.sarprasq.ui.components.HelpdeskDetailDialog
import com.lintang.sarprasq.ui.components.StatusBadge
import com.lintang.sarprasq.ui.components.UpdateStatusDialog
import com.lintang.sarprasq.ui.components.UrgencyBadge
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelMintLight
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel

@Composable
fun HelpdeskScreen(
    viewModel: SarprasViewModel,
    onOpenAddHelpdesk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.filteredReports.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedStatusFilter.collectAsState()
    val masterRuangList by viewModel.allRuang.collectAsState()
    val masterStatusList by viewModel.allStatusPenanganan.collectAsState()
    val masterUrgensiList by viewModel.allUrgensi.collectAsState()
    val masterKategoriList by viewModel.allKategori.collectAsState()

    val ruangOptions = remember(masterRuangList) { masterRuangList.map { it.namaRuang } }
    val statusOptions = remember(masterStatusList) {
        val list = masterStatusList.map { it.namaStatus }.toMutableList()
        if (!list.contains("Catat")) {
            list.add(0, "Catat")
        }
        list.distinct()
    }
    val urgensiOptions = remember(masterUrgensiList) { masterUrgensiList.map { it.namaUrgensi } }
    val kategoriOptions = remember(masterKategoriList) { masterKategoriList.map { it.namaKategori } }

    var selectedKategoriFilter by remember { mutableStateOf("Semua Kategori") }
    var selectedUrgensiFilter by remember { mutableStateOf("Semua Urgensi") }
    var selectedRuangFilter by remember { mutableStateOf("Semua Ruang") }
    var showFilterDialog by remember { mutableStateOf(false) }

    var selectedReportForDetail by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedReportForEdit by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedReportForUpdateStatus by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedReportForDelete by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedReportForPartialExecution by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedIndicesForPartial by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val statusFilterOptions = remember(statusOptions) {
        listOf("Semua Status", "Catat", "Darurat") + statusOptions.filter { it != "Catat" }
    }
    val urgensiFilterOptions = listOf("Semua Urgensi", "Darurat", "Penting", "Sedang", "Rendah")
    val kategoriFilterOptions = listOf("Semua Kategori") + kategoriOptions
    val ruangFilterOptions = remember(ruangOptions) { listOf("Semua Ruang") + ruangOptions }

    var tempStatus by remember { mutableStateOf(if (selectedFilter.isBlank()) "Semua Status" else selectedFilter) }
    var tempUrgensi by remember { mutableStateOf(selectedUrgensiFilter) }
    var tempKategori by remember { mutableStateOf(selectedKategoriFilter) }
    var tempRuang by remember { mutableStateOf(selectedRuangFilter) }

    val displayReports = remember(reports, selectedKategoriFilter, selectedUrgensiFilter, selectedRuangFilter) {
        reports.filter { report ->
            val matchesKategori = selectedKategoriFilter == "Semua Kategori" || report.kategori.equals(selectedKategoriFilter, ignoreCase = true)
            val matchesUrgensi = selectedUrgensiFilter == "Semua Urgensi" || report.urgensi.equals(selectedUrgensiFilter, ignoreCase = true)
            val matchesRuang = selectedRuangFilter == "Semua Ruang" || report.lokasi.contains(selectedRuangFilter, ignoreCase = true)
            matchesKategori && matchesUrgensi && matchesRuang
        }
    }

    val isStatusFiltered = selectedFilter.isNotBlank() && selectedFilter != "Semua" && selectedFilter != "Semua Status"
    val isUrgensiFiltered = selectedUrgensiFilter != "Semua Urgensi"
    val isKategoriFiltered = selectedKategoriFilter != "Semua Kategori"
    val isRuangFiltered = selectedRuangFilter != "Semua Ruang"
    val activeFilterCount = (if (isStatusFiltered) 1 else 0) + (if (isUrgensiFiltered) 1 else 0) + (if (isKategoriFiltered) 1 else 0) + (if (isRuangFiltered) 1 else 0)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddHelpdesk,
                containerColor = PastelSkyBlueDark,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Laporan", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Filter Trigger Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Cari lokasi, pelapor, deskripsi...", fontSize = 12.5.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                FilterTriggerButton(
                    activeFilterCount = activeFilterCount,
                    onClick = {
                        tempStatus = if (selectedFilter.isBlank()) "Semua Status" else selectedFilter
                        tempUrgensi = selectedUrgensiFilter
                        tempKategori = selectedKategoriFilter
                        tempRuang = selectedRuangFilter
                        showFilterDialog = true
                    },
                    modifier = Modifier.height(54.dp),
                    label = "Filter"
                )
            }

            // Active Filters Summary Bar
            if (activeFilterCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isStatusFiltered) {
                        item {
                            FilterActiveTag(
                                label = "Status: $selectedFilter",
                                onClear = { viewModel.selectedStatusFilter.value = "Semua" },
                                bg = PastelSkyBlue,
                                textColor = PastelSkyBlueDark
                            )
                        }
                    }
                    if (isUrgensiFiltered) {
                        item {
                            FilterActiveTag(
                                label = "Urgensi: $selectedUrgensiFilter",
                                onClear = { selectedUrgensiFilter = "Semua Urgensi" },
                                bg = PastelPeach,
                                textColor = PastelPeachDark
                            )
                        }
                    }
                    if (isKategoriFiltered) {
                        item {
                            FilterActiveTag(
                                label = "Kategori: $selectedKategoriFilter",
                                onClear = { selectedKategoriFilter = "Semua Kategori" },
                                bg = PastelMintLight,
                                textColor = PastelMintDark
                            )
                        }
                    }
                    item {
                        Text(
                            text = "Reset All",
                            fontSize = 11.sp,
                            color = PastelPeachDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.selectedStatusFilter.value = "Semua"
                                    selectedUrgensiFilter = "Semua Urgensi"
                                    selectedKategoriFilter = "Semua Kategori"
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Helpdesk List
            if (displayReports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada laporan helpdesk ditemukan.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(displayReports, key = { it.id }) { report ->
                        val damageItems = remember(report.deskripsi) { parseDamageItemsFromReport(report) }
                        var selectedIndices by remember(report.id, report.deskripsi) { mutableStateOf(setOf<Int>()) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(width = 1.dp, color = PastelCardBorder, shape = RoundedCornerShape(20.dp))
                                .clickable { selectedReportForDetail = report }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = report.lokasi,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Pelapor: ${report.pelapor} (${report.tanggal})",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        UrgencyBadge(urgensi = report.urgensi)
                                        StatusBadge(status = report.status)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // RINCIAN DENGAN CHECKBOX SELEKTIF PER ITEM
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Daftar Kerusakan (${damageItems.size} Item):",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )

                                            if (damageItems.size > 1) {
                                                Text(
                                                    text = if (selectedIndices.size == damageItems.size) "Batal Pilih" else "Pilih Semua",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PastelSkyBlueDark,
                                                    modifier = Modifier.clickable {
                                                        selectedIndices = if (selectedIndices.size == damageItems.size) {
                                                            emptySet()
                                                        } else {
                                                            damageItems.indices.toSet()
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        damageItems.forEachIndexed { index, item ->
                                            val isChecked = index in selectedIndices
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isChecked) PastelSkyBlueContainer.copy(alpha = 0.5f) else Color.White)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isChecked) PastelSkyBlueDark else Color(0xFFF1F5F9),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        selectedIndices = if (isChecked) {
                                                            selectedIndices - index
                                                        } else {
                                                            selectedIndices + index
                                                        }
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { checked ->
                                                        selectedIndices = if (checked == true) {
                                                            selectedIndices + index
                                                        } else {
                                                            selectedIndices - index
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = PastelSkyBlueDark,
                                                        uncheckedColor = Color.Gray
                                                    ),
                                                    modifier = Modifier.size(28.dp)
                                                )

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "[${item.kategori}]",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = PastelSkyBlueDark
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = item.deskripsi,
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = TextPrimary
                                                        )
                                                    }
                                                    Text(
                                                        text = "Urgensi: ${item.urgensi}",
                                                        fontSize = 10.5.sp,
                                                        color = if (item.urgensi.equals("Darurat", ignoreCase = true)) PastelPeachDark else TextSecondary
                                                    )
                                                }

                                                // Tombol Eksekusi Langsung per Item Satuan
                                                IconButton(
                                                    onClick = {
                                                        selectedReportForPartialExecution = report
                                                        selectedIndicesForPartial = setOf(index)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Build,
                                                        contentDescription = "Proses Item Ini",
                                                        tint = PastelSkyBlueDark,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // BATCH ACTION BUTTON (JIKA ADA CHECKBOX TERCENTANG)
                                        if (selectedIndices.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    selectedReportForPartialExecution = report
                                                    selectedIndicesForPartial = selectedIndices
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Build,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Proses ${selectedIndices.size} Item Terpilih",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Tindakan: ${report.tindakan}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                if (report.status.equals("Pending", ignoreCase = true)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PastelPeach.copy(alpha = 0.5f))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "⚠️ Alasan Tertunda: ${report.alasanPending ?: "-"}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PastelPeachDark
                                            )
                                            if (!report.estimasiEksekusi.isNullOrBlank()) {
                                                Text(
                                                    text = "📅 Estimasi Eksekusi: ${report.estimasiEksekusi}",
                                                    fontSize = 11.sp,
                                                    color = PastelPeachDark.copy(alpha = 0.9f)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { selectedReportForDelete = report }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Hapus Laporan",
                                            tint = Color.Red.copy(alpha = 0.6f)
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { selectedReportForEdit = report },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Form", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { selectedReportForUpdateStatus = report },
                                            colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Status Paket", fontSize = 12.sp)
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

    // Detail Pop-Up
    selectedReportForDetail?.let { report ->
        HelpdeskDetailDialog(
            report = report,
            onDismiss = { selectedReportForDetail = null },
            onEdit = {
                selectedReportForEdit = report
                selectedReportForDetail = null
            },
            onUpdateStatus = {
                selectedReportForUpdateStatus = report
                selectedReportForDetail = null
            },
            onDelete = {
                selectedReportForDelete = report
                selectedReportForDetail = null
            }
        )
    }

    // Edit Form Dialog
    selectedReportForEdit?.let { report ->
        AddHelpdeskDialog(
            initialReport = report,
            ruangList = ruangOptions,
            urgensiList = urgensiOptions,
            statusList = statusOptions,
            kategoriList = kategoriOptions,
            onAddKategoriToMaster = { viewModel.addKategori(it) },
            onDismiss = { selectedReportForEdit = null },
            onSubmit = { pelapor, lokasi, deskripsi, urgensi, status, tindakan, alasanPending, estimasiEksekusi, tanggal, kategori, fotoUrl, catatan, petugas ->
                val updatedReport = report.copy(
                    pelapor = pelapor,
                    lokasi = lokasi,
                    deskripsi = deskripsi,
                    urgensi = urgensi,
                    status = status,
                    tindakan = tindakan,
                    alasanPending = alasanPending,
                    estimasiEksekusi = estimasiEksekusi,
                    tanggal = tanggal,
                    kategori = kategori,
                    fotoUrl = fotoUrl,
                    catatan = catatan,
                    petugas = petugas
                )
                viewModel.updateHelpdeskReport(updatedReport)
                selectedReportForEdit = null
            }
        )
    }

    // Update Status Dialog
    selectedReportForUpdateStatus?.let { report ->
        UpdateStatusDialog(
            report = report,
            customStatusOptions = statusOptions,
            onDismiss = { selectedReportForUpdateStatus = null },
            onSubmit = { newStatus, newTindakan, newAlasan, newEstimasi ->
                viewModel.updateHelpdeskReportStatus(
                    report = report,
                    newStatus = newStatus,
                    newTindakan = newTindakan,
                    newAlasanPending = newAlasan,
                    newEstimasi = newEstimasi
                )
                selectedReportForUpdateStatus = null
            }
        )
    }

    // Delete Confirmation Dialog
    selectedReportForDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { selectedReportForDelete = null },
            title = { Text("Hapus Laporan?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus laporan '${report.deskripsi}' di ${report.lokasi}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHelpdeskReport(report)
                        selectedReportForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReportForDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Partial Item Status Dialog
    selectedReportForPartialExecution?.let { report ->
        val itemsToProcess = remember(report, selectedIndicesForPartial) {
            val all = parseDamageItemsFromReport(report)
            all.filterIndexed { index, _ -> index in selectedIndicesForPartial }
        }
        PartialStatusDialog(
            report = report,
            selectedItems = itemsToProcess,
            customStatusOptions = statusOptions,
            onDismiss = {
                selectedReportForPartialExecution = null
                selectedIndicesForPartial = emptySet()
            },
            onSubmit = { newStatus, newTindakan, newAlasan, newEstimasi, newPetugas ->
                viewModel.processPartialHelpdeskReportItems(
                    originalReport = report,
                    selectedIndices = selectedIndicesForPartial,
                    newStatus = newStatus,
                    newTindakan = newTindakan,
                    newAlasanPending = newAlasan,
                    newEstimasi = newEstimasi,
                    newPetugas = newPetugas
                )
                selectedReportForPartialExecution = null
                selectedIndicesForPartial = emptySet()
            }
        )
    }

    // 2-Column Grid Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            title = "Filter Data Helpdesk",
            filterGroups = listOf(
                FilterOptionGroup(
                    title = "Status Penanganan",
                    selectedValue = tempStatus,
                    options = statusFilterOptions,
                    onValueChange = { tempStatus = it }
                ),
                FilterOptionGroup(
                    title = "Tingkat Urgensi",
                    selectedValue = tempUrgensi,
                    options = urgensiFilterOptions,
                    onValueChange = { tempUrgensi = it }
                ),
                FilterOptionGroup(
                    title = "Kategori Kerusakan",
                    selectedValue = tempKategori,
                    options = kategoriFilterOptions,
                    onValueChange = { tempKategori = it }
                ),
                FilterOptionGroup(
                    title = "Lokasi / Ruangan",
                    selectedValue = tempRuang,
                    options = ruangFilterOptions,
                    onValueChange = { tempRuang = it }
                )
            ),
            onDismiss = { showFilterDialog = false },
            onApply = {
                viewModel.selectedStatusFilter.value = if (tempStatus == "Semua Status") "Semua" else tempStatus
                selectedUrgensiFilter = tempUrgensi
                selectedKategoriFilter = tempKategori
                selectedRuangFilter = tempRuang
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun FilterActiveTag(
    label: String,
    onClear: () -> Unit,
    bg: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Hapus Filter",
                tint = textColor,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onClear() }
            )
        }
    }
}

@Composable
fun HelpdeskFilterDialog(
    selectedStatus: String,
    statusOptions: List<String>,
    onSelectStatus: (String) -> Unit,
    selectedUrgensi: String,
    urgensiOptions: List<String>,
    onSelectUrgensi: (String) -> Unit,
    selectedKategori: String,
    kategoriOptions: List<String>,
    onSelectKategori: (String) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PastelSkyBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = PastelSkyBlueDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Filter Data Laporan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Opsi Filter Layout 2 Kolom",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2-Column Grid Body Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KOLOM 1: STATUS PENANGANAN (Left Column)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PastelSkyBlueContainer.copy(alpha = 0.35f))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = PastelSkyBlueDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Status",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelSkyBlueDark
                            )
                        }

                        statusOptions.forEach { option ->
                            val isSelected = (selectedStatus == option) ||
                                    (selectedStatus.isBlank() && option == "Semua Status") ||
                                    (selectedStatus == "Semua" && option == "Semua Status")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PastelSkyBlue else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PastelSkyBlueDark else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectStatus(if (option == "Semua Status") "Semua" else option) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PastelSkyBlueDark else TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PastelSkyBlueDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // KOLOM 2: URGENSI & KATEGORI (Right Column)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PastelPeach.copy(alpha = 0.25f))
                            .padding(10.dp)
                    ) {
                        // Section Urgensi
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PastelPeachDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Urgensi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelPeachDark
                            )
                        }

                        urgensiOptions.forEach { option ->
                            val isSelected = selectedUrgensi == option
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PastelPeach else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PastelPeachDark else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectUrgensi(option) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PastelPeachDark else TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PastelPeachDark,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Section Kategori
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = PastelMintDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kategori",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelMintDark
                            )
                        }

                        kategoriOptions.forEach { option ->
                            val isSelected = selectedKategori == option
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PastelMintLight else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PastelMintDark else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectKategori(option) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PastelMintDark else TextPrimary,
                                        maxLines = 1
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PastelMintDark,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onResetFilters,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelPeachDark)
                    ) {
                        Text("Reset Filter", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terapkan", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartialStatusDialog(
    report: HelpdeskReport,
    selectedItems: List<DynamicDamageItem>,
    customStatusOptions: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (newStatus: String, newTindakan: String, newAlasanPending: String?, newEstimasi: String?, newPetugas: String?) -> Unit
) {
    val effectiveStatusList = if (customStatusOptions.isNotEmpty()) customStatusOptions else listOf("Proses", "Pending", "Selesai")
    var selectedStatus by remember { mutableStateOf(effectiveStatusList.firstOrNull { it.contains("Proses", ignoreCase = true) } ?: effectiveStatusList.first()) }
    var tindakan by remember { mutableStateOf("") }
    var alasanPending by remember { mutableStateOf("") }
    var estimasiEksekusi by remember { mutableStateOf("") }
    var petugas by remember { mutableStateOf(if (!report.petugas.isNullOrBlank()) report.petugas else "Kevin Ricky Utama, S.Kom.") }
    var expandedStatus by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Eksekusi Granular (${selectedItems.size} Item)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Lokasi: ${report.lokasi}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PastelSkyBlueDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item List Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelBackground)
                        .border(1.dp, PastelCardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Item yang diproses terpisah:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        selectedItems.forEach { item ->
                            Text(
                                text = "• [${item.kategori}] ${item.deskripsi} (${item.urgensi})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Baru") },
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
                                    selectedStatus = option
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tindakan,
                    onValueChange = { tindakan = it },
                    label = { Text("Tindakan Penanganan") },
                    placeholder = { Text("Misal: Diproses teknisi / sparepart dipesan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (selectedStatus.equals("Pending", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = alasanPending,
                        onValueChange = { alasanPending = it },
                        label = { Text("Alasan Pending") },
                        placeholder = { Text("Misal: Menunggu persetujuan anggaran") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = estimasiEksekusi,
                        onValueChange = { estimasiEksekusi = it },
                        label = { Text("Estimasi Selesai / Eksekusi") },
                        placeholder = { Text("Misal: 05/08/2026") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = petugas,
                    onValueChange = { petugas = it },
                    label = { Text("Petugas Penanggung Jawab") },
                    placeholder = { Text("Misal: Pak Budi (Teknisi)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSubmit(
                                selectedStatus,
                                tindakan,
                                if (selectedStatus.equals("Pending", ignoreCase = true)) alasanPending.ifBlank { null } else null,
                                if (selectedStatus.equals("Pending", ignoreCase = true)) estimasiEksekusi.ifBlank { null } else null,
                                petugas.ifBlank { null }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proses & Pisahkan Status", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
