package com.lintang.sarprasq.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.ui.components.StatusBadge
import com.lintang.sarprasq.ui.components.UpdateProgressDialog
import com.lintang.sarprasq.ui.components.UpdateStatusDialog
import com.lintang.sarprasq.ui.components.UrgencyBadge
import com.lintang.sarprasq.ui.components.parseProgressLogs
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelMintLight
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel

@Composable
fun PendingControlScreen(
    viewModel: SarprasViewModel,
    modifier: Modifier = Modifier
) {
    val allReports by viewModel.allReports.collectAsState()

    val masterStatusList by viewModel.allStatusPenanganan.collectAsState()
    val statusOptions = remember(masterStatusList) { masterStatusList.map { it.namaStatus } }

    var selectedStatusFilter by remember { mutableStateOf("Semua") }
    var selectedReportForProgress by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedReportForStatusUpdate by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedReportForDelete by remember { mutableStateOf<HelpdeskReport?>(null) }

    val statusSubMenus = listOf("Semua", "Darurat", "Pending", "Diproses", "Segera", "Selesai", "Ditolak")

    fun isReportMatchingSubMenu(report: HelpdeskReport, subMenu: String): Boolean {
        if (report.status.equals("Catat", ignoreCase = true)) return false

        return when (subMenu) {
            "Semua" -> true
            "Darurat" -> report.urgensi.equals("Darurat", ignoreCase = true) || report.status.equals("Darurat", ignoreCase = true)
            "Pending" -> report.status.equals("Pending", ignoreCase = true)
            "Diproses" -> report.status.equals("Proses", ignoreCase = true) || report.status.equals("Diproses", ignoreCase = true) || report.status.contains("Proses", ignoreCase = true)
            "Segera" -> report.status.equals("Segera", ignoreCase = true)
            "Selesai" -> report.status.equals("Selesai", ignoreCase = true)
            "Ditolak" -> report.status.equals("Ditolak", ignoreCase = true) || report.status.contains("Batal", ignoreCase = true)
            else -> report.status.equals(subMenu, ignoreCase = true)
        }
    }

    val filteredReports = remember(allReports, selectedStatusFilter) {
        allReports.filter { report -> isReportMatchingSubMenu(report, selectedStatusFilter) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = PastelSkyBlueDark,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Progres & Riwayat Pengerjaan",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tracking persentase harian & timeline log berkala",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status Sub-Menu Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(statusSubMenus) { subMenu ->
                val isSelected = selectedStatusFilter == subMenu
                val count = remember(allReports, subMenu) {
                    allReports.count { report -> isReportMatchingSubMenu(report, subMenu) }
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedStatusFilter = subMenu },
                    label = { Text("$subMenu ($count)", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PastelSkyBlue,
                        selectedLabelColor = PastelSkyBlueDark,
                        containerColor = PastelSurface,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredReports.isEmpty()) {
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
                        modifier = Modifier.size(56.dp),
                        tint = PastelMintDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak Ada Laporan di Menu '$selectedStatusFilter'",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Semua pengerjaan tercatat dan terpantau dengan baik.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredReports, key = { it.id }) { report ->
                    ProgressReportCard(
                        report = report,
                        onUpdateProgress = { selectedReportForProgress = report },
                        onUpdateStatus = { selectedReportForStatusUpdate = report },
                        onDelete = { selectedReportForDelete = report }
                    )
                }
            }
        }
    }

    // Dialog Update Progress Harian
    selectedReportForProgress?.let { report ->
        UpdateProgressDialog(
            report = report,
            onDismiss = { selectedReportForProgress = null },
            onSubmitProgress = { newPercentage, note, date ->
                viewModel.updateHelpdeskProgress(report, newPercentage, note, date)
                selectedReportForProgress = null
            }
        )
    }

    // Dialog Update Status Laporan
    selectedReportForStatusUpdate?.let { report ->
        UpdateStatusDialog(
            report = report,
            customStatusOptions = statusOptions,
            onDismiss = { selectedReportForStatusUpdate = null },
            onSubmit = { newStatus, newTindakan, newAlasan, newEstimasi ->
                viewModel.updateHelpdeskReportStatus(
                    report = report,
                    newStatus = newStatus,
                    newTindakan = newTindakan,
                    newAlasanPending = newAlasan,
                    newEstimasi = newEstimasi
                )
                selectedReportForStatusUpdate = null
            }
        )
    }

    // Dialog Konfirmasi Hapus Laporan (Diperketat / Double Confirmation)
    selectedReportForDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { selectedReportForDelete = null },
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
                    text = "Hapus Laporan Ini?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus laporan lokasi '${report.lokasi}' secara permanen?",
                        fontSize = 13.5.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Deskripsi: ${report.deskripsi}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Peringatan: Data yang terhapus tidak dapat dikembalikan lagi.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHelpdeskReport(report)
                        selectedReportForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ya, Hapus Permanen", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { selectedReportForDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun ProgressReportCard(
    report: HelpdeskReport,
    onUpdateProgress: () -> Unit,
    onUpdateStatus: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedTimeline by remember { mutableStateOf(false) }
    val logs = remember(report.riwayatProgres) { parseProgressLogs(report.riwayatProgres) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UrgencyBadge(urgensi = report.urgensi)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = report.status)
                    Spacer(modifier = Modifier.width(4.dp))
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Pelapor
            Text(
                text = report.lokasi,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Pelapor: ${report.pelapor} • ${report.tanggal}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = report.deskripsi,
                fontSize = 13.sp,
                color = TextPrimary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- PROGRESS BAR & PERCENTAGE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PastelBackground)
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = PastelSkyBlueDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Progres Pengerjaan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "${report.progresPersen}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (report.progresPersen >= 100) PastelMintDark else PastelSkyBlueDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = report.progresPersen / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (report.progresPersen >= 100) PastelMintDark else PastelSkyBlueDark,
                        trackColor = Color.White
                    )
                }
            }

            // Timeline logs expandable preview
            if (logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expandedTimeline = !expandedTimeline }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = PastelSkyBlueDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Riwayat Log (${logs.size} Update)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSkyBlueDark
                        )
                    }

                    Text(
                        text = if (expandedTimeline) "Sembunyikan ▲" else "Lihat Detail ▼",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (expandedTimeline) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        logs.reversed().forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PastelBackground)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${log.tanggal} (${log.persen}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = log.catatan,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUpdateProgress,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Progres", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onUpdateStatus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ubah Status", fontSize = 12.sp)
                }
            }
        }
    }
}
