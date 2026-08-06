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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.ProjectTask
import com.lintang.sarprasq.ui.components.ActionPlanDetailDialog
import com.lintang.sarprasq.ui.components.AddActionPlanDialog
import com.lintang.sarprasq.ui.components.StartActionPlanDialog
import com.lintang.sarprasq.ui.components.AddHelpdeskDialog
import com.lintang.sarprasq.ui.components.HelpdeskDetailDialog
import com.lintang.sarprasq.ui.components.PastelStatCard
import com.lintang.sarprasq.ui.components.ProgresBadge
import com.lintang.sarprasq.ui.components.JenisRencanaBadge
import com.lintang.sarprasq.ui.components.StatusBadge
import com.lintang.sarprasq.ui.components.UpdateStatusDialog
import com.lintang.sarprasq.ui.components.UrgencyBadge
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellow
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
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
fun DashboardScreen(
    viewModel: SarprasViewModel,
    onNavigateTab: (Int) -> Unit,
    onOpenAddHelpdesk: () -> Unit,
    onOpenAddActionPlan: () -> Unit,
    onOpenAddSurat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val reports by viewModel.allReports.collectAsState()
    val plans by viewModel.actionPlans.collectAsState()
    val projectTasks by viewModel.allProjectTasks.collectAsState()
    val masterRuangList by viewModel.allRuang.collectAsState()
    val masterStatusList by viewModel.allStatusPenanganan.collectAsState()
    val masterUrgensiList by viewModel.allUrgensi.collectAsState()
    val masterKategoriList by viewModel.allKategori.collectAsState()
    val masterSubKategoriList by viewModel.allSubKategori.collectAsState()
    val masterSatuanList by viewModel.allSatuan.collectAsState()
    val ruangOptions = remember(masterRuangList) { masterRuangList.map { it.namaRuang } }
    val statusOptions = remember(masterStatusList) { masterStatusList.map { it.namaStatus } }
    val urgensiOptions = remember(masterUrgensiList) { masterUrgensiList.map { it.namaUrgensi } }
    val kategoriOptions = remember(masterKategoriList) { masterKategoriList.map { it.namaKategori } }

    // State Dialog for Helpdesk
    var selectedHelpdeskForDetail by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedHelpdeskForEdit by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedHelpdeskForUpdateStatus by remember { mutableStateOf<HelpdeskReport?>(null) }
    var selectedHelpdeskForDelete by remember { mutableStateOf<HelpdeskReport?>(null) }

    // State Dialog for Action Plan
    var selectedActionPlanForDetail by remember { mutableStateOf<ActionPlan?>(null) }
    var selectedActionPlanForEdit by remember { mutableStateOf<ActionPlan?>(null) }
    var selectedActionPlanForDelete by remember { mutableStateOf<ActionPlan?>(null) }
    var selectedActionPlanForStart by remember { mutableStateOf<ActionPlan?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        // --- 1. Ringkasan Operasional Makro ---
        item {
            Column {
                Text(
                    text = "Ringkasan Operasional Makro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PastelStatCard(
                        title = "Laporan Masuk",
                        value = "${stats.totalLaporan}",
                        icon = Icons.Default.Build,
                        backgroundColor = PastelSkyBlueContainer,
                        accentColor = PastelSkyBlueDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(1) }
                    )

                    PastelStatCard(
                        title = "Laporan Selesai",
                        value = "${stats.laporanSelesai}",
                        icon = Icons.Default.AssignmentTurnedIn,
                        backgroundColor = PastelMintLight,
                        accentColor = PastelMintDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(1) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PastelStatCard(
                        title = "Masalah Pending",
                        value = "${stats.laporanPending}",
                        icon = Icons.Default.HourglassTop,
                        backgroundColor = PastelPeach,
                        accentColor = PastelPeachDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(2) }
                    )

                    PastelStatCard(
                        title = "Agenda Proaktif",
                        value = "${stats.agendaProgres}",
                        icon = Icons.Default.NotificationsActive,
                        backgroundColor = PastelButterYellow,
                        accentColor = PastelButterYellowDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(3) }
                    )
                }
            }
        }

        // --- 1B. WIDGET RINGKASAN PROYEK FISIK AKTIF ---
        item {
            ActiveProjectSummaryWidget(
                projectTasks = projectTasks,
                onNavigateToProjects = { onNavigateTab(10) }
            )
        }

        // --- 2. Quick Action Buttons ---
        item {
            Column {
                Text(
                    text = "Aksi Cepat Operasional",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = onOpenAddHelpdesk,
                            colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Laporan Baru", fontSize = 13.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = onOpenAddActionPlan,
                            colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agenda Proaktif", fontSize = 13.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = { onNavigateTab(3) },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Perencana Kalender", fontSize = 13.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = { onNavigateTab(10) },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelButterYellowDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("To-Do & Proyek", fontSize = 13.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = onOpenAddSurat,
                            colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Arsip Surat", fontSize = 13.sp)
                        }
                    }
                    item {
                        Button(
                            onClick = { onNavigateTab(11) },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak & Ekspor", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- 3. GRAFIK ANALITIK HASIL PENANGANAN & REALISASI ---
        item {
            AnalyticsHelpdeskStatusChart(reports = reports)
        }

        item {
            AnalyticsActionPlanRealizationChart(plans = plans)
        }

        // --- 3. Laporan Masuk Terbaru ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Laporan Masuk Terbaru",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Klik kartu untuk detail lengkap & aksi manajemen",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                OutlinedButton(
                    onClick = { onNavigateTab(1) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lihat Semua", fontSize = 12.sp)
                }
            }
        }

        if (reports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada laporan masuk.", fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            items(reports.take(4), key = { "helpdesk_${it.id}" }) { report ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(width = 1.dp, color = PastelCardBorder, shape = RoundedCornerShape(16.dp))
                        .clickable { selectedHelpdeskForDetail = report }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = report.lokasi,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                UrgencyBadge(urgensi = report.urgensi)
                                StatusBadge(status = report.status)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = report.deskripsi,
                            fontSize = 13.sp,
                            color = TextPrimary.copy(alpha = 0.85f),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pelapor: ${report.pelapor} • ${report.tanggal}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = { selectedHelpdeskForEdit = report },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Form",
                                        tint = PastelSkyBlueDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { selectedHelpdeskForUpdateStatus = report },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "Update Status",
                                        tint = PastelMintDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { selectedHelpdeskForDelete = report },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Hapus",
                                        tint = PastelPeachDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Agenda Kerja Proaktif Preview ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Agenda Rencana Kerja",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Klik kartu untuk detail lengkap & aksi manajemen",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                OutlinedButton(
                    onClick = { onNavigateTab(3) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lihat Agenda", fontSize = 12.sp)
                }
            }
        }

        if (plans.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada agenda rencana kerja.", fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            items(plans.take(4), key = { "action_${it.id}" }) { plan ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PastelLavender.copy(alpha = 0.2f))
                        .border(width = 1.dp, color = PastelLavenderDark.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                        .clickable { selectedActionPlanForDetail = plan }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                JenisRencanaBadge(jenis = plan.jenisRencana)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = plan.agenda,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${plan.kategori} • Target: ${plan.targetWaktu}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            ProgresBadge(progres = plan.statusProgres)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (!plan.statusProgres.equals("Selesai", ignoreCase = true)) {
                                    TextButton(
                                        onClick = {
                                            if (plan.statusProgres.equals("Belum Mulai", ignoreCase = true)) {
                                                selectedActionPlanForStart = plan
                                            } else {
                                                viewModel.updateActionPlanStatus(plan, "Selesai", "03/08/2026")
                                            }
                                        },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            if (plan.statusProgres.equals("Belum Mulai", ignoreCase = true)) "Mulai" else "Selesai",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PastelSkyBlueDark
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { selectedActionPlanForEdit = plan },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Agenda",
                                        tint = PastelSkyBlueDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { selectedActionPlanForDelete = plan },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Hapus Agenda",
                                        tint = PastelPeachDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // --- POP-UP DIALOGS (DETAIL, EDIT, CRUD) ---
    // ==========================================

    // 1. Detail Pop-Up Dialog for Helpdesk
    selectedHelpdeskForDetail?.let { report ->
        HelpdeskDetailDialog(
            report = report,
            onDismiss = { selectedHelpdeskForDetail = null },
            onEdit = {
                selectedHelpdeskForEdit = report
                selectedHelpdeskForDetail = null
            },
            onUpdateStatus = {
                selectedHelpdeskForUpdateStatus = report
                selectedHelpdeskForDetail = null
            },
            onDelete = {
                selectedHelpdeskForDelete = report
                selectedHelpdeskForDetail = null
            }
        )
    }

    // 2. Edit Form Dialog for Helpdesk
    selectedHelpdeskForEdit?.let { report ->
        AddHelpdeskDialog(
            initialReport = report,
            ruangList = ruangOptions,
            urgensiList = urgensiOptions,
            statusList = statusOptions,
            kategoriList = kategoriOptions,
            onAddKategoriToMaster = { viewModel.addKategori(it) },
            onDismiss = { selectedHelpdeskForEdit = null },
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
                selectedHelpdeskForEdit = null
            }
        )
    }

    // 3. Update Status Dialog for Helpdesk
    selectedHelpdeskForUpdateStatus?.let { report ->
        UpdateStatusDialog(
            report = report,
            customStatusOptions = statusOptions,
            onDismiss = { selectedHelpdeskForUpdateStatus = null },
            onSubmit = { newStatus, newTindakan, newAlasan, newEstimasi ->
                viewModel.updateHelpdeskReportStatus(
                    report = report,
                    newStatus = newStatus,
                    newTindakan = newTindakan,
                    newAlasanPending = newAlasan,
                    newEstimasi = newEstimasi
                )
                selectedHelpdeskForUpdateStatus = null
            }
        )
    }

    // 4. Delete Confirmation Dialog for Helpdesk
    selectedHelpdeskForDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { selectedHelpdeskForDelete = null },
            title = { Text("Hapus Laporan Helpdesk?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus laporan '${report.deskripsi}' di ${report.lokasi}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHelpdeskReport(report)
                        selectedHelpdeskForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedHelpdeskForDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // 5. Detail Pop-Up Dialog for Action Plan
    selectedActionPlanForDetail?.let { plan ->
        ActionPlanDetailDialog(
            plan = plan,
            onDismiss = { selectedActionPlanForDetail = null },
            onEdit = {
                selectedActionPlanForEdit = plan
                selectedActionPlanForDetail = null
            },
            onStart = {
                selectedActionPlanForStart = plan
                selectedActionPlanForDetail = null
            },
            onQuickProgress = { newStatus ->
                viewModel.updateActionPlanStatus(plan, newStatus, if (newStatus == "Selesai") "03/08/2026" else "-")
                selectedActionPlanForDetail = null
            },
            onDelete = {
                selectedActionPlanForDelete = plan
                selectedActionPlanForDetail = null
            }
        )
    }

    // Start Action Plan Dialog
    selectedActionPlanForStart?.let { plan ->
        StartActionPlanDialog(
            plan = plan,
            onDismiss = { selectedActionPlanForStart = null },
            onSubmit = { tanggalMulai, pelaksana, namaTukang, nomorWhatsapp ->
                viewModel.startActionPlan(
                    plan = plan,
                    tanggalMulai = tanggalMulai,
                    pelaksana = pelaksana,
                    namaTukang = namaTukang,
                    nomorWhatsapp = nomorWhatsapp
                )
                selectedActionPlanForStart = null
            }
        )
    }

    // 6. Edit Form Dialog for Action Plan
    selectedActionPlanForEdit?.let { plan ->
        AddActionPlanDialog(
            initialPlan = plan,
            masterSubKategoriList = masterSubKategoriList,
            masterRuangList = masterRuangList,
            masterSatuanList = masterSatuanList,
            onDismiss = { selectedActionPlanForEdit = null },
            onSubmit = { agenda, jenisRencana, kategori, targetWaktu, statusProgres, tanggalMulai, tanggalRealisasi, pelaksana, namaTukang, nomorWhatsapp, detailAset, lokasiRuang, tingkatKerusakan, spesifikasi, jumlahSatuan, estimasiAnggaran, fotoUrl ->
                val updatedPlan = plan.copy(
                    agenda = agenda,
                    jenisRencana = jenisRencana,
                    kategori = kategori,
                    targetWaktu = targetWaktu,
                    statusProgres = statusProgres,
                    tanggalMulai = tanggalMulai,
                    tanggalRealisasi = tanggalRealisasi,
                    pelaksana = pelaksana,
                    namaTukang = namaTukang,
                    nomorWhatsapp = nomorWhatsapp,
                    detailAset = detailAset,
                    lokasiRuang = lokasiRuang,
                    tingkatKerusakan = tingkatKerusakan,
                    spesifikasi = spesifikasi,
                    jumlahSatuan = jumlahSatuan,
                    estimasiAnggaran = estimasiAnggaran,
                    fotoUrl = fotoUrl
                )
                viewModel.updateActionPlan(updatedPlan)
                selectedActionPlanForEdit = null
            }
        )
    }

    // 7. Delete Confirmation Dialog for Action Plan
    selectedActionPlanForDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { selectedActionPlanForDelete = null },
            title = { Text("Hapus Agenda Rencana Kerja?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus agenda '${plan.agenda}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteActionPlan(plan)
                        selectedActionPlanForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedActionPlanForDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun AnalyticsHelpdeskStatusChart(reports: List<HelpdeskReport>) {
    val total = reports.size
    val pendingCount = reports.count { it.status.equals("Pending", ignoreCase = true) }
    val prosesCount = reports.count { it.status.contains("Proses", ignoreCase = true) }
    val selesaiCount = reports.count { it.status.equals("Selesai", ignoreCase = true) }

    val pendingPct = if (total > 0) (pendingCount * 100f / total).toInt() else 0
    val prosesPct = if (total > 0) (prosesCount * 100f / total).toInt() else 0
    val selesaiPct = if (total > 0) (selesaiCount * 100f / total).toInt() else 0

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = PastelSkyBlueDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analitik Status Penanganan Helpdesk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "$total Laporan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment progress bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PastelBackground)
            ) {
                if (total > 0) {
                    if (selesaiCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(selesaiCount.toFloat())
                                .fillMaxSize()
                                .background(PastelMintDark)
                        )
                    }
                    if (prosesCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(prosesCount.toFloat())
                                .fillMaxSize()
                                .background(PastelSkyBlueDark)
                        )
                    }
                    if (pendingCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(pendingCount.toFloat())
                                .fillMaxSize()
                                .background(PastelButterYellowDark)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stat Legend Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Selesai
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelMintLight)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Selesai", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$selesaiCount ($selesaiPct%)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PastelMintDark)
                    }
                }

                // Diproses
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelSkyBlueContainer)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Diproses", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$prosesCount ($prosesPct%)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PastelSkyBlueDark)
                    }
                }

                // Menunggu / Pending
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelButterYellow)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Menunggu", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$pendingCount ($pendingPct%)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PastelButterYellowDark)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsActionPlanRealizationChart(plans: List<ActionPlan>) {
    val categories = listOf(
        "Rencana Perbaikan / Perawatan",
        "Rencana Pengadaan",
        "Rencana Belanja Barang / Jasa"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = PastelLavenderDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grafik Realisasi Jenis Rencana Kerja",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "${plans.size} Total Agenda",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categories.forEach { cat ->
                    val catPlans = plans.filter { it.jenisRencana.equals(cat, ignoreCase = true) }
                    val totalCat = catPlans.size
                    val doneCat = catPlans.count { it.statusProgres.equals("Selesai", ignoreCase = true) }
                    val pct = if (totalCat > 0) (doneCat * 100f / totalCat).toInt() else 0

                    val displayLabel = when (cat) {
                        "Rencana Perbaikan / Perawatan" -> "Perbaikan / Perawatan"
                        "Rencana Pengadaan" -> "Pengadaan Sarpras"
                        "Rencana Belanja Barang / Jasa" -> "Belanja Barang & Jasa"
                        else -> cat
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelBackground)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$doneCat / $totalCat Selesai ($pct%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelLavenderDark
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { if (totalCat > 0) doneCat / totalCat.toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PastelLavenderDark,
                            trackColor = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveProjectSummaryWidget(
    projectTasks: List<ProjectTask>,
    onNavigateToProjects: () -> Unit
) {
    val activeTasks = remember(projectTasks) { projectTasks.filter { !it.isCompleted } }
    val totalCount = projectTasks.size

    val avgProgressPercent = remember(projectTasks) {
        if (projectTasks.isEmpty()) 0.0
        else {
            val totalAchieved = projectTasks.sumOf { it.totalProgres }
            val totalTarget = projectTasks.sumOf { it.bobotPersen }.let { if (it <= 0.0) 100.0 else it }
            (totalAchieved / totalTarget * 100.0).coerceIn(0.0, 100.0)
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PastelButterYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = PastelButterYellowDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Status Proyek Fisik & Revitalisasi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${activeTasks.size} Proyek Aktif • ${avgProgressPercent.toInt()}% Total Progress",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                TextButton(
                    onClick = onNavigateToProjects,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Kelola", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelSkyBlueDark)
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = PastelSkyBlueDark)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Overall Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { (avgProgressPercent / 100.0).toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = PastelButterYellowDark,
                    trackColor = PastelBackground
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${avgProgressPercent.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PastelButterYellowDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (projectTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelBackground)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada agenda proyek fisik aktif. Klik 'Kelola' untuk menambah proyek fisik/revitalisasi sekolah.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    projectTasks.take(3).forEach { task ->
                        val taskPct = if (task.bobotPersen > 0) (task.totalProgres / task.bobotPersen * 100).coerceIn(0.0, 100.0) else 0.0
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelBackground)
                                .clickable { onNavigateToProjects() }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (task.isCompleted) PastelMintLight else PastelSkyBlueContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (task.isCompleted) "Selesai 100%" else "${task.totalProgres.toInt()}% / ${task.bobotPersen.toInt()}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) PastelMintDark else PastelSkyBlueDark
                                        )
                                    }
                                }

                                if (task.subKategori.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${task.subKategori} • Target: ${task.startDate} s/d ${task.endDate}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (taskPct / 100.0).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (task.isCompleted) PastelMintDark else PastelSkyBlueDark,
                                    trackColor = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
