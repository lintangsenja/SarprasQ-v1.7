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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.ui.components.FilterDialog
import com.lintang.sarprasq.ui.components.FilterOptionGroup
import com.lintang.sarprasq.ui.components.FilterTriggerButton
import com.lintang.sarprasq.ui.components.ActionPlanDetailDialog
import com.lintang.sarprasq.ui.components.AddActionPlanDialog
import com.lintang.sarprasq.ui.components.JenisRencanaBadge
import com.lintang.sarprasq.ui.components.ProgresBadge
import com.lintang.sarprasq.ui.components.StartActionPlanDialog
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPlanScreen(
    viewModel: SarprasViewModel,
    onOpenAddActionPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionPlans by viewModel.actionPlans.collectAsStateWithLifecycle()
    val subKategoriList by viewModel.allSubKategori.collectAsStateWithLifecycle()
    val ruangList by viewModel.allRuang.collectAsStateWithLifecycle()
    val satuanList by viewModel.allSatuan.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Semua") }
    var selectedRuangFilter by remember { mutableStateOf("Semua Ruang") }
    var selectedJenisFilter by remember { mutableStateOf("Semua Jenis") }
    var showFilterDialog by remember { mutableStateOf(false) }

    val statusOptions = listOf("Semua", "Belum Mulai", "Dalam Proses", "Selesai")
    val ruangOptions = remember(ruangList) { listOf("Semua Ruang") + ruangList.map { it.namaRuang } }
    val jenisOptions = listOf("Semua Jenis", "Pemeliharaan", "Pengadaan", "Perbaikan", "Revitalisasi", "Kebersihan")

    var tempStatus by remember { mutableStateOf(selectedStatusFilter) }
    var tempRuang by remember { mutableStateOf(selectedRuangFilter) }
    var tempJenis by remember { mutableStateOf(selectedJenisFilter) }

    val activeFilterCount = (if (selectedStatusFilter != "Semua") 1 else 0) +
            (if (selectedRuangFilter != "Semua Ruang") 1 else 0) +
            (if (selectedJenisFilter != "Semua Jenis") 1 else 0)

    var selectedPlanForDetail by remember { mutableStateOf<ActionPlan?>(null) }
    var selectedPlanForEdit by remember { mutableStateOf<ActionPlan?>(null) }
    var selectedPlanForStart by remember { mutableStateOf<ActionPlan?>(null) }
    var selectedPlanForDelete by remember { mutableStateOf<ActionPlan?>(null) }

    val filteredPlans = remember(actionPlans, searchQuery, selectedStatusFilter, selectedRuangFilter, selectedJenisFilter) {
        actionPlans.filter { plan ->
            val matchesSearch = searchQuery.isBlank() ||
                    plan.agenda.contains(searchQuery, ignoreCase = true) ||
                    plan.kategori.contains(searchQuery, ignoreCase = true) ||
                    plan.jenisRencana.contains(searchQuery, ignoreCase = true) ||
                    (plan.lokasiRuang?.contains(searchQuery, ignoreCase = true) == true)

            val matchesStatus = when (selectedStatusFilter) {
                "Belum Mulai" -> plan.statusProgres.equals("Belum Mulai", ignoreCase = true)
                "Dalam Proses" -> plan.statusProgres.equals("Dalam Proses", ignoreCase = true)
                "Selesai" -> plan.statusProgres.equals("Selesai", ignoreCase = true)
                else -> true
            }

            val matchesRuang = selectedRuangFilter == "Semua Ruang" ||
                    (plan.lokasiRuang?.contains(selectedRuangFilter, ignoreCase = true) == true)

            val matchesJenis = selectedJenisFilter == "Semua Jenis" ||
                    plan.jenisRencana.equals(selectedJenisFilter, ignoreCase = true)

            matchesSearch && matchesStatus && matchesRuang && matchesJenis
        }
    }

    val totalCount = actionPlans.size
    val belumMulaiCount = actionPlans.count { it.statusProgres.equals("Belum Mulai", ignoreCase = true) }
    val dalamProsesCount = actionPlans.count { it.statusProgres.equals("Dalam Proses", ignoreCase = true) }
    val selesaiCount = actionPlans.count { it.statusProgres.equals("Selesai", ignoreCase = true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PastelBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddActionPlan,
                containerColor = PastelSkyBlueDark,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Rencana Aksi")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rencana Aksi Sarpras",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Kelola agenda perbaikan, pengadaan, & perawatan",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Button(
                    onClick = onOpenAddActionPlan,
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    title = "Total",
                    count = totalCount.toString(),
                    bgColor = PastelSkyBlue.copy(alpha = 0.2f),
                    textColor = PastelSkyBlueDark,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Belum",
                    count = belumMulaiCount.toString(),
                    bgColor = PastelLavender.copy(alpha = 0.3f),
                    textColor = PastelLavenderDark,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Proses",
                    count = dalamProsesCount.toString(),
                    bgColor = Color(0xFFFFF3CD),
                    textColor = PastelButterYellowDark,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Selesai",
                    count = selesaiCount.toString(),
                    bgColor = PastelMint.copy(alpha = 0.3f),
                    textColor = PastelMintDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari agenda, kategori, lokasi...", fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PastelSurface,
                        unfocusedContainerColor = PastelSurface,
                        focusedBorderColor = PastelSkyBlueDark,
                        unfocusedBorderColor = PastelCardBorder
                    )
                )

                FilterTriggerButton(
                    activeFilterCount = activeFilterCount,
                    onClick = {
                        tempStatus = selectedStatusFilter
                        tempRuang = selectedRuangFilter
                        tempJenis = selectedJenisFilter
                        showFilterDialog = true
                    },
                    modifier = Modifier.height(50.dp),
                    label = "Filter"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Semua", "Belum Mulai", "Dalam Proses", "Selesai").forEach { status ->
                    val isSelected = selectedStatusFilter == status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PastelSkyBlueDark else PastelSurface)
                            .border(1.dp, if (isSelected) PastelSkyBlueDark else PastelCardBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedStatusFilter = status }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = status,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Action Plans
            if (filteredPlans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PastelSurface)
                        .border(1.dp, PastelCardBorder, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Tidak ada rencana aksi yang cocok dengan pencarian." else "Belum ada rencana aksi terdaftar.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredPlans, key = { it.id }) { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlanForDetail = plan },
                            colors = CardDefaults.cardColors(containerColor = PastelSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
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
                                        if (!plan.lokasiRuang.isNull_or_blank_safe()) {
                                            Text(
                                                text = "Lokasi: ${plan.lokasiRuang}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    ProgresBadge(progres = plan.statusProgres)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!plan.statusProgres.equals("Selesai", ignoreCase = true)) {
                                        TextButton(
                                            onClick = {
                                                if (plan.statusProgres.equals("Belum Mulai", ignoreCase = true)) {
                                                    selectedPlanForStart = plan
                                                } else {
                                                    val nowStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                                                    viewModel.updateActionPlanStatus(plan, "Selesai", nowStr)
                                                }
                                            },
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp)
                                        ) {
                                            Text(
                                                text = if (plan.statusProgres.equals("Belum Mulai", ignoreCase = true)) "Mulai Kerjakan" else "Tandai Selesai",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PastelSkyBlueDark
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { selectedPlanForEdit = plan },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Agenda",
                                            tint = PastelSkyBlueDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { selectedPlanForDelete = plan },
                                        modifier = Modifier.size(30.dp)
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

        // Dialogs
        selectedPlanForDetail?.let { plan ->
            ActionPlanDetailDialog(
                plan = plan,
                onDismiss = { selectedPlanForDetail = null },
                onEdit = {
                    selectedPlanForEdit = plan
                    selectedPlanForDetail = null
                },
                onStart = {
                    selectedPlanForStart = plan
                    selectedPlanForDetail = null
                },
                onQuickProgress = { newStatus ->
                    val nowStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                    viewModel.updateActionPlanStatus(plan, newStatus, if (newStatus == "Selesai") nowStr else "-")
                    selectedPlanForDetail = null
                },
                onDelete = {
                    selectedPlanForDelete = plan
                    selectedPlanForDetail = null
                }
            )
        }

        selectedPlanForStart?.let { plan ->
            StartActionPlanDialog(
                plan = plan,
                onDismiss = { selectedPlanForStart = null },
                onSubmit = { startDate, pelaksana, tukang, wa ->
                    viewModel.startActionPlan(
                        plan = plan,
                        tanggalMulai = startDate,
                        pelaksana = pelaksana,
                        namaTukang = tukang,
                        nomorWhatsapp = wa
                    )
                    selectedPlanForStart = null
                }
            )
        }

        selectedPlanForEdit?.let { plan ->
            AddActionPlanDialog(
                initialPlan = plan,
                masterSubKategoriList = subKategoriList,
                masterRuangList = ruangList,
                masterSatuanList = satuanList,
                onDismiss = { selectedPlanForEdit = null },
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
                    selectedPlanForEdit = null
                }
            )
        }

        selectedPlanForDelete?.let { plan ->
            AlertDialog(
                onDismissRequest = { selectedPlanForDelete = null },
                title = { Text("Hapus Rencana Aksi", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = { Text("Apakah Anda yakin ingin menghapus agenda '${plan.agenda}'?", fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteActionPlan(plan)
                            selectedPlanForDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark)
                    ) {
                        Text("Hapus", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedPlanForDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }

        if (showFilterDialog) {
            FilterDialog(
                title = "Filter Rencana Aksi",
                filterGroups = listOf(
                    FilterOptionGroup(
                        title = "Status Progres",
                        selectedValue = tempStatus,
                        options = statusOptions,
                        onValueChange = { tempStatus = it }
                    ),
                    FilterOptionGroup(
                        title = "Lokasi / Ruangan",
                        selectedValue = tempRuang,
                        options = ruangOptions,
                        onValueChange = { tempRuang = it }
                    ),
                    FilterOptionGroup(
                        title = "Jenis Rencana",
                        selectedValue = tempJenis,
                        options = jenisOptions,
                        onValueChange = { tempJenis = it }
                    )
                ),
                onDismiss = { showFilterDialog = false },
                onApply = {
                    selectedStatusFilter = tempStatus
                    selectedRuangFilter = tempRuang
                    selectedJenisFilter = tempJenis
                    showFilterDialog = false
                }
            )
        }
    }
}

private fun String?.isNull_or_blank_safe(): Boolean {
    return this == null || this.trim().isEmpty()
}

@Composable
private fun StatPill(
    title: String,
    count: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(text = title, fontSize = 10.sp, color = textColor)
        }
    }
}
