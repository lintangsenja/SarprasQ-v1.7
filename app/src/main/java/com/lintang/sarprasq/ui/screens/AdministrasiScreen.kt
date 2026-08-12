package com.lintang.sarprasq.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import com.lintang.sarprasq.util.SuratExcelHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.PeminjamanMakro
import com.lintang.sarprasq.data.model.ProjectTask
import com.lintang.sarprasq.data.model.SuratArsip
import com.lintang.sarprasq.ui.components.AddSuratDialog
import com.lintang.sarprasq.ui.components.SuratDetailDialog
import com.lintang.sarprasq.ui.theme.PastelButterYellow
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelMintLight
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel

sealed class ArchivedWorkItem {
    data class TaskItem(val task: ProjectTask) : ArchivedWorkItem()
    data class HelpdeskItem(val report: HelpdeskReport) : ArchivedWorkItem()
}

@Composable
fun AdministrasiScreen(
    viewModel: SarprasViewModel,
    onOpenAddSurat: () -> Unit,
    onOpenAddPeminjaman: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Pekerjaan Selesai, 1: Arsip Surat, 2: Rekap Peminjaman

    // State collections
    val allTasks by viewModel.allProjectTasks.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val suratList by viewModel.suratArsipList.collectAsState()
    val peminjamanList by viewModel.peminjamanList.collectAsState()

    // Dialog state for deletions (Double Confirmation)
    var taskToDelete by remember { mutableStateOf<ProjectTask?>(null) }
    var reportToDelete by remember { mutableStateOf<HelpdeskReport?>(null) }
    var suratToDelete by remember { mutableStateOf<SuratArsip?>(null) }
    var peminjamanToDelete by remember { mutableStateOf<PeminjamanMakro?>(null) }

    // Dialog state for Surat Detail & Edit
    var selectedSuratDetail by remember { mutableStateOf<SuratArsip?>(null) }
    var suratToEdit by remember { mutableStateOf<SuratArsip?>(null) }

    // Count completed physical tasks
    val completedTasks = remember(allTasks) {
        allTasks.filter {
            it.isCompleted ||
            it.totalProgres >= 100.0 ||
            (it.bobotPersen > 0 && (it.totalProgres >= it.bobotPersen || (it.bobotPersen - it.totalProgres) <= 0.00001)) ||
            it.sisaKekuranganProgres <= 0.00001
        }
    }
    val completedReports = remember(allReports) {
        allReports.filter { it.status.equals("Selesai", ignoreCase = true) }
    }
    val totalCompletedWorkCount = completedTasks.size + completedReports.size

    Scaffold(
        floatingActionButton = {
            if (selectedMainTab == 1) {
                FloatingActionButton(
                    onClick = onOpenAddSurat,
                    containerColor = PastelLavenderDark,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Surat", modifier = Modifier.size(24.dp))
                }
            } else if (selectedMainTab == 2) {
                FloatingActionButton(
                    onClick = onOpenAddPeminjaman,
                    containerColor = PastelSkyBlueDark,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Rekap Peminjaman", modifier = Modifier.size(24.dp))
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = PastelSkyBlueDark,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Arsip Serbaguna Sarpras",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pekerjaan fisik selesai, dokumen surat & rekap peminjaman",
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(width = 1.dp, color = PastelCardBorder, shape = RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                // Tab 0: Pekerjaan Selesai
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMainTab == 0) PastelMintLight else Color.Transparent)
                        .clickable { selectedMainTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = if (selectedMainTab == 0) PastelMintDark else TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fisik Selesai ($totalCompletedWorkCount)",
                            fontSize = 12.sp,
                            fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedMainTab == 0) PastelMintDark else TextSecondary
                        )
                    }
                }

                // Tab 1: Arsip Surat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMainTab == 1) PastelLavender else Color.Transparent)
                        .clickable { selectedMainTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            tint = if (selectedMainTab == 1) PastelLavenderDark else TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Surat (${suratList.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedMainTab == 1) PastelLavenderDark else TextSecondary
                        )
                    }
                }

                // Tab 2: Rekap Peminjaman
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedMainTab == 2) PastelSkyBlue else Color.Transparent)
                        .clickable { selectedMainTab = 2 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = if (selectedMainTab == 2) PastelSkyBlueDark else TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Peminjaman (${peminjamanList.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedMainTab == 2) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedMainTab == 2) PastelSkyBlueDark else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TAB CONTENT DISPLAY
            when (selectedMainTab) {
                0 -> CompletedWorkTabContent(
                    completedTasks = completedTasks,
                    completedReports = completedReports,
                    onDeleteTask = { taskToDelete = it },
                    onDeleteReport = { reportToDelete = it }
                )
                1 -> SuratArchiveTabContent(
                    suratList = suratList,
                    viewModel = viewModel,
                    onDeleteSurat = { suratToDelete = it },
                    onSelectSurat = { selectedSuratDetail = it }
                )
                2 -> PeminjamanArchiveTabContent(
                    peminjamanList = peminjamanList,
                    onDeletePeminjaman = { peminjamanToDelete = it }
                )
            }
        }
    }

    // --- DIALOGS KONFIRMASI HAPUS PERMANEN (DOUBLE CONFIRMATION) ---

    // 1. Delete Task
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Arsip Pekerjaan?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus rekam jejak pekerjaan '${task.title}' secara permanen dari arsip?",
                    fontSize = 13.5.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProjectTask(task)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ya, Hapus Permanen", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { taskToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // 2. Delete Helpdesk Report
    reportToDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Arsip Helpdesk?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus arsip laporan perbaikan lokasi '${report.lokasi}' secara permanen?",
                    fontSize = 13.5.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHelpdeskReport(report)
                        reportToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ya, Hapus Permanen", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { reportToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // 3. Delete Surat Arsip
    suratToDelete?.let { surat ->
        AlertDialog(
            onDismissRequest = { suratToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Arsip Surat?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus surat no '${surat.nomorSurat}' (${surat.perihal}) secara permanen?",
                    fontSize = 13.5.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSuratArsip(surat)
                        suratToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ya, Hapus Permanen", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { suratToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // 4. Delete Peminjaman
    peminjamanToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { peminjamanToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Rekap Peminjaman?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus rekap peminjaman '${item.namaBarang}' (${item.bulanTahun})?",
                    fontSize = 13.5.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePeminjamanMakro(item)
                        peminjamanToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ya, Hapus Permanen", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { peminjamanToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // 5. Detail Surat Dialog
    selectedSuratDetail?.let { surat ->
        SuratDetailDialog(
            surat = surat,
            onDismiss = { selectedSuratDetail = null },
            onEdit = { editedSurat ->
                selectedSuratDetail = null
                suratToEdit = editedSurat
            },
            onDelete = { deletedSurat ->
                selectedSuratDetail = null
                suratToDelete = deletedSurat
            }
        )
    }

    // 6. Edit Surat Dialog
    suratToEdit?.let { targetSurat ->
        AddSuratDialog(
            suratToEdit = targetSurat,
            onDismiss = { suratToEdit = null },
            onSubmit = { nomorSurat, tanggalSurat, perihal, jenisSurat, statusArsip ->
                viewModel.updateSuratArsip(
                    targetSurat.copy(
                        nomorSurat = nomorSurat,
                        tanggalSurat = tanggalSurat,
                        perihal = perihal,
                        jenisSurat = jenisSurat,
                        statusArsip = statusArsip
                    )
                )
                suratToEdit = null
            }
        )
    }
}

// ==========================================
// TAB 0: ARSIP PEKERJAAN FISIK SELESAI
// ==========================================
@Composable
fun CompletedWorkTabContent(
    completedTasks: List<ProjectTask>,
    completedReports: List<HelpdeskReport>,
    onDeleteTask: (ProjectTask) -> Unit,
    onDeleteReport: (HelpdeskReport) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("Semua") } // "Semua", "Proyek Revitalisasi", "Rehab Intern", "To-Do Harian", "Helpdesk Selesai"
    var searchQuery by remember { mutableStateOf("") }

    val categoryFilters = listOf("Semua", "Proyek Revitalisasi", "Rehab Intern", "To-Do Harian", "Helpdesk Selesai")

    // Combine all into unified list
    val allArchivedItems = remember(completedTasks, completedReports) {
        val list = mutableListOf<ArchivedWorkItem>()
        completedTasks.forEach { list.add(ArchivedWorkItem.TaskItem(it)) }
        completedReports.forEach { list.add(ArchivedWorkItem.HelpdeskItem(it)) }
        list
    }

    val filteredItems = remember(allArchivedItems, selectedCategoryFilter, searchQuery) {
        allArchivedItems.filter { item ->
            val matchesCategory = when (selectedCategoryFilter) {
                "Proyek Revitalisasi" -> item is ArchivedWorkItem.TaskItem && item.task.type.equals("Proyek Revitalisasi", ignoreCase = true)
                "Rehab Intern" -> item is ArchivedWorkItem.TaskItem && item.task.type.equals("Rehab Intern", ignoreCase = true)
                "To-Do Harian" -> item is ArchivedWorkItem.TaskItem && item.task.type.equals("Harian", ignoreCase = true)
                "Helpdesk Selesai" -> item is ArchivedWorkItem.HelpdeskItem
                else -> true
            }

            val matchesQuery = searchQuery.isBlank() || when (item) {
                is ArchivedWorkItem.TaskItem -> {
                    item.task.title.contains(searchQuery, ignoreCase = true) ||
                            item.task.subKategori.contains(searchQuery, ignoreCase = true) ||
                            item.task.notes.contains(searchQuery, ignoreCase = true)
                }
                is ArchivedWorkItem.HelpdeskItem -> {
                    item.report.lokasi.contains(searchQuery, ignoreCase = true) ||
                            item.report.pelapor.contains(searchQuery, ignoreCase = true) ||
                            item.report.deskripsi.contains(searchQuery, ignoreCase = true) ||
                            item.report.tindakan.contains(searchQuery, ignoreCase = true)
                }
            }

            matchesCategory && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari pekerjaan selesai, lokasi, atau proyek...", fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categoryFilters) { cat ->
                val isSelected = selectedCategoryFilter == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PastelMintLight,
                        selectedLabelColor = PastelMintDark,
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PastelMintDark.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada riwayat pekerjaan selesai di kategori ini.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredItems, key = {
                    when (it) {
                        is ArchivedWorkItem.TaskItem -> "task_${it.task.id}"
                        is ArchivedWorkItem.HelpdeskItem -> "report_${it.report.id}"
                    }
                }) { item ->
                    when (item) {
                        is ArchivedWorkItem.TaskItem -> ArchivedTaskCard(
                            task = item.task,
                            onDelete = { onDeleteTask(item.task) }
                        )
                        is ArchivedWorkItem.HelpdeskItem -> ArchivedReportCard(
                            report = item.report,
                            onDelete = { onDeleteReport(item.report) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArchivedTaskCard(
    task: ProjectTask,
    onDelete: () -> Unit
) {
    val typeLabel = when (task.type) {
        "Harian" -> "To-Do Harian"
        "Proyek Revitalisasi" -> "Proyek Revitalisasi"
        "Rehab Intern" -> "Rehab Intern"
        else -> task.type
    }

    val typeColorPair = when (task.type) {
        "Proyek Revitalisasi" -> Pair(PastelPeach, PastelPeachDark)
        "Rehab Intern" -> Pair(PastelButterYellow, PastelButterYellowDark)
        else -> Pair(PastelSkyBlue, PastelSkyBlueDark)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeColorPair.first)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColorPair.second
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PastelMintLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PastelMintDark,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "100% Selesai",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelMintDark
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Pekerjaan",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (task.subKategori.isNotBlank()) {
                Text(
                    text = "Sub-Kategori Pekerjaan: ${task.subKategori}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Periode Pelaksanaan: ${task.startDate.ifBlank { "-" }} s/d ${task.endDate.ifBlank { "-" }} (${task.durasiHari} Hari)",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            if (task.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PastelMintLight.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Catatan Rekomendasi/Selesai: ${task.notes}",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ArchivedReportCard(
    report: HelpdeskReport,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PastelLavender)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Laporan Helpdesk",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelLavenderDark
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PastelMintLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PastelMintDark,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Penanganan Selesai",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelMintDark
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Laporan",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Lokasi: ${report.lokasi}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Pelapor: ${report.pelapor} • Tanggal Lapor: ${report.tanggal}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = report.deskripsi,
                fontSize = 12.5.sp,
                color = TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (report.tindakan.isNotBlank() && report.tindakan != "-") {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PastelMintLight.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Tindakan Akhir: ${report.tindakan}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 1: ARSIP SURAT-MENYURAT
// ==========================================
@Composable
fun SuratArchiveTabContent(
    suratList: List<SuratArsip>,
    viewModel: SarprasViewModel,
    onDeleteSurat: (SuratArsip) -> Unit,
    onSelectSurat: (SuratArsip) -> Unit
) {
    val context = LocalContext.current
    var selectedJenisFilter by remember { mutableStateOf("Semua") } // "Semua", "Surat Masuk", "Surat Keluar", "Nota Dinas", "Permohonan Perbaikan"
    var searchQuery by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var parsedImportItems by remember { mutableStateOf<List<SuratArsip>>(emptyList()) }

    // SAF Open Document Launcher untuk File Excel Template (.xlsx)
    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { fileUri ->
            val parseResult = SuratExcelHelper.parseSuratArsipFromXlsx(context, fileUri)
            if (parseResult.success && parseResult.items.isNotEmpty()) {
                parsedImportItems = parseResult.items
                showImportDialog = true
            } else {
                val errMsg = parseResult.message.ifBlank { "Format file Excel tidak sesuai dengan master Arsip Surat." }
                Toast.makeText(context, "Gagal Impor Excel: $errMsg", Toast.LENGTH_LONG).show()
            }
        }
    }

    val jenisFilters = listOf("Semua", "Surat Masuk", "Surat Keluar", "Nota Dinas", "Permohonan Perbaikan")

    val filteredSurat = remember(suratList, selectedJenisFilter, searchQuery) {
        suratList.filter { surat ->
            val matchesJenis = when (selectedJenisFilter) {
                "Semua" -> true
                else -> surat.jenisSurat.equals(selectedJenisFilter, ignoreCase = true) ||
                        surat.jenisSurat.contains(selectedJenisFilter, ignoreCase = true)
            }

            val matchesQuery = searchQuery.isBlank() ||
                    surat.nomorSurat.contains(searchQuery, ignoreCase = true) ||
                    surat.perihal.contains(searchQuery, ignoreCase = true) ||
                    surat.tanggalSurat.contains(searchQuery, ignoreCase = true)

            matchesJenis && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- 3 TOMBOL AKSI HEADER ARSIP SURAT ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Unduh Template
            OutlinedButton(
                onClick = {
                    val result = SuratExcelHelper.generateTemplateXlsx(context)
                    if (result != null) {
                        Toast.makeText(
                            context,
                            "✓ Template Excel berhasil diunduh ke folder Downloads:\n${result.fileName}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = PastelLavender.copy(alpha = 0.25f),
                    contentColor = PastelLavenderDark
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDark.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Unduh Template",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Unduh Template", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // 2. Impor Excel
            Button(
                onClick = {
                    excelPickerLauncher.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel",
                            "application/zip",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PastelLavenderDark,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = "Impor Excel",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Impor Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // 3. Ekspor Data
            OutlinedButton(
                onClick = {
                    if (suratList.isEmpty()) {
                        Toast.makeText(context, "Belum ada data arsip surat untuk diekspor!", Toast.LENGTH_SHORT).show()
                    } else {
                        val result = SuratExcelHelper.exportSuratArsipToXlsx(
                            context = context,
                            suratList = filteredSurat.ifEmpty { suratList }
                        )
                        if (result != null) {
                            Toast.makeText(
                                context,
                                "✓ Data Arsip Surat berhasil diekspor ke folder Downloads:\n${result.fileName}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = PastelMint.copy(alpha = 0.25f),
                    contentColor = PastelMintDark
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, PastelMintDark.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Ekspor Data",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ekspor Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari nomor surat atau perihal...", fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(jenisFilters) { jenis ->
                val isSelected = selectedJenisFilter == jenis
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedJenisFilter = jenis },
                    label = { Text(jenis, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PastelLavender,
                        selectedLabelColor = PastelLavenderDark,
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSurat.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada arsip surat pada kategori ini.", fontSize = 13.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredSurat, key = { it.id }) { surat ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSurat(surat) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = surat.nomorSurat,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tanggal: ${surat.tanggalSurat} • Jenis: ${surat.jenisSurat}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(PastelLavender.copy(alpha = 0.4f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = surat.statusArsip,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PastelLavenderDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { onDeleteSurat(surat) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Surat",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Perihal: ${surat.perihal}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG KONFIRMASI PRATINJAU IMPOR BATCH EXCEL ---
    if (showImportDialog && parsedImportItems.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text(
                    text = "Konfirmasi Impor Data Excel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Smart Parser berhasil membaca ${parsedImportItems.size} data arsip surat dari berkas Excel.",
                        fontSize = 12.5.sp,
                        color = TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelLavender.copy(alpha = 0.35f))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Pratinjau Sampel Data:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelLavenderDark
                            )
                            val sample = parsedImportItems.first()
                            Text("• Nomor: ${sample.nomorSurat}", fontSize = 11.sp, color = TextPrimary)
                            Text("• Tanggal: ${sample.tanggalSurat}", fontSize = 11.sp, color = TextPrimary)
                            Text("• Perihal: ${sample.perihal}", fontSize = 11.sp, color = TextPrimary)
                            Text("• Jenis: ${sample.jenisSurat} (${sample.statusArsip})", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Text(
                        text = "Tekan 'Simpan Batch' untuk menyimpan data ke Room Database & menyinkronkannya secara otomatis ke Firebase.",
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = parsedImportItems.size
                        viewModel.insertSuratArsipList(parsedImportItems) {
                            Toast.makeText(
                                context,
                                "✓ Berhasil mengimpor $count data arsip surat ke database!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simpan Batch (${parsedImportItems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showImportDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal", fontSize = 12.sp, color = TextSecondary)
                }
            }
        )
    }
}

// ==========================================
// TAB 2: REKAP PEMINJAMAN MAKRO
// ==========================================
@Composable
fun PeminjamanArchiveTabContent(
    peminjamanList: List<PeminjamanMakro>,
    onDeletePeminjaman: (PeminjamanMakro) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(peminjamanList, searchQuery) {
        peminjamanList.filter { item ->
            searchQuery.isBlank() ||
                    item.namaBarang.contains(searchQuery, ignoreCase = true) ||
                    item.bulanTahun.contains(searchQuery, ignoreCase = true) ||
                    item.kondisi.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari barang atau periode peminjaman...", fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada data rekap peminjaman.", fontSize = 13.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.namaBarang,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Jadwal / Periode: ${item.bulanTahun}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PastelMintLight)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Kondisi: ${item.kondisi}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PastelMintDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { onDeletePeminjaman(item) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Rekap",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PastelSkyBlue.copy(alpha = 0.25f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Jumlah Unit Dipinjam:",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${item.jumlahPeminjaman} Unit",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PastelSkyBlueDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
