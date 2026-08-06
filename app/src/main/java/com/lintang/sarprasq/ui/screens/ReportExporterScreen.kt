package com.lintang.sarprasq.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.PeminjamanMakro
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.data.model.SuratArsip
import com.lintang.sarprasq.ui.components.DatePickerField
import com.lintang.sarprasq.ui.components.FilterDialog
import com.lintang.sarprasq.ui.components.FilterOptionGroup
import com.lintang.sarprasq.ui.components.FilterTriggerButton
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellow
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel
import com.lintang.sarprasq.util.ReportCategory
import com.lintang.sarprasq.util.ReportExporter
import com.lintang.sarprasq.util.ReportFilter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportExporterScreen(viewModel: SarprasViewModel) {
    val context = LocalContext.current

    val schoolName by viewModel.namaSekolah.collectAsState()
    val programName by viewModel.namaProgram.collectAsState()
    val petugasName by viewModel.namaPetugas.collectAsState()
    val nipPetugas by viewModel.nipPetugas.collectAsState()

    val allHelpdesk by viewModel.allReports.collectAsState()
    val allActionPlans by viewModel.actionPlans.collectAsState()
    val allRuang by viewModel.allRuang.collectAsState()
    val allPeminjaman by viewModel.peminjamanList.collectAsState()
    val allSurat by viewModel.suratArsipList.collectAsState()

    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var selectedDateRange by remember { mutableStateOf("Semua") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedRuang by remember { mutableStateOf("Semua Ruang") }
    var showFilterDialog by remember { mutableStateOf(false) }

    val categories = ReportCategory.values()
    val currentCategory = categories[selectedCategoryIndex]
    val categoryTitleList = remember { categories.map { it.title } }

    val dateRanges = listOf("Semua", "Hari Ini", "Bulan Ini", "Tahun Ini")
    val ruangOptions = remember(allRuang) { listOf("Semua Ruang") + allRuang.map { it.namaRuang } }

    var tempCategoryName by remember { mutableStateOf(currentCategory.title) }
    var tempDateRange by remember { mutableStateOf(selectedDateRange) }
    var tempRuang by remember { mutableStateOf(selectedRuang) }

    val activeFilterCount = (if (selectedDateRange != "Semua" || startDate.isNotBlank() || endDate.isNotBlank()) 1 else 0) + (if (selectedRuang != "Semua Ruang") 1 else 0)

    val dateSubtitle = if (startDate.isNotBlank() || endDate.isNotBlank()) {
        "${startDate.ifBlank { "Awal" }} s/d ${endDate.ifBlank { "Kini" }}"
    } else {
        selectedDateRange
    }

    // --- FILTER DATA LOGIC ---
    val filteredHelpdesk = remember(allHelpdesk, selectedRuang, selectedDateRange, startDate, endDate) {
        allHelpdesk.filter { item ->
            val matchRuang = if (selectedRuang == "Semua Ruang") true
            else item.lokasi.contains(selectedRuang, ignoreCase = true)
            val matchDate = isDateInRange(item.tanggal, startDate, endDate, selectedDateRange)
            matchRuang && matchDate
        }
    }

    val filteredActionPlans = remember(allActionPlans, selectedRuang, selectedDateRange, startDate, endDate) {
        allActionPlans.filter { item ->
            val matchRuang = if (selectedRuang == "Semua Ruang") true
            else (item.lokasiRuang ?: "").contains(selectedRuang, ignoreCase = true)
            val dateStr = item.tanggalMulai.ifBlank { item.targetWaktu }
            val matchDate = isDateInRange(dateStr, startDate, endDate, selectedDateRange)
            matchRuang && matchDate
        }
    }

    val filteredRuang = remember(allRuang, selectedRuang) {
        if (selectedRuang == "Semua Ruang") allRuang
        else allRuang.filter { it.namaRuang.contains(selectedRuang, ignoreCase = true) }
    }

    val filteredSurat = remember(allSurat, selectedDateRange, startDate, endDate) {
        allSurat.filter { item ->
            isDateInRange(item.tanggalSurat, startDate, endDate, selectedDateRange)
        }
    }

    val filterObj = ReportFilter(
        category = currentCategory,
        dateRangeType = selectedDateRange,
        selectedRuang = selectedRuang,
        startDate = startDate,
        endDate = endDate
    )

    fun triggerExportPdf() {
        ReportExporter.exportToPdf(
            context = context,
            filter = filterObj,
            schoolName = schoolName,
            programName = programName,
            petugasName = petugasName,
            nipPetugas = nipPetugas,
            helpdeskList = filteredHelpdesk,
            actionPlanList = filteredActionPlans,
            ruangList = filteredRuang,
            peminjamanList = allPeminjaman,
            suratList = filteredSurat
        )
    }

    fun triggerExportExcel() {
        ReportExporter.exportToExcel(
            context = context,
            filter = filterObj,
            schoolName = schoolName,
            programName = programName,
            petugasName = petugasName,
            nipPetugas = nipPetugas,
            helpdeskList = filteredHelpdesk,
            actionPlanList = filteredActionPlans,
            ruangList = filteredRuang,
            peminjamanList = allPeminjaman,
            suratList = filteredSurat
        )
    }

    fun triggerExportWord() {
        ReportExporter.exportToWord(
            context = context,
            filter = filterObj,
            schoolName = schoolName,
            programName = programName,
            petugasName = petugasName,
            nipPetugas = nipPetugas,
            helpdeskList = filteredHelpdesk,
            actionPlanList = filteredActionPlans,
            ruangList = filteredRuang,
            peminjamanList = allPeminjaman,
            suratList = filteredSurat
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelBackground)
    ) {
        // --- 1. HEADER BANNER WITH INLINE FILTER ICON BUTTON ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PastelSkyBlueContainer)
                .border(1.dp, PastelCardBorder, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PastelSkyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = PastelSkyBlueDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Laporan & Ekspor Dokumen",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentCategory.title} • Periode: $dateSubtitle • $selectedRuang",
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Inline 3-line Filter Icon Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeFilterCount > 0) PastelSkyBlue else Color.White)
                        .border(1.dp, if (activeFilterCount > 0) PastelSkyBlueDark else PastelCardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            tempCategoryName = currentCategory.title
                            tempDateRange = selectedDateRange
                            tempRuang = selectedRuang
                            showFilterDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (activeFilterCount > 0) PastelSkyBlueDark else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    if (activeFilterCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(5.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PastelSkyBlueDark)
                        )
                    }
                }
            }
        }

        // --- 2. CATEGORY SELECTION TABS ---
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = Color.Transparent,
            contentColor = PastelSkyBlueDark,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                    height = 3.dp,
                    color = PastelSkyBlueDark
                )
            }
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = {
                        Text(
                            text = when(cat) {
                                ReportCategory.HELPDESK -> "1. Helpdesk"
                                ReportCategory.ACTION_PLAN -> "2. Proyek"
                                ReportCategory.INVENTARIS_RUANG -> "3. Inventaris"
                                ReportCategory.ADMINISTRASI -> "4. Persuratan"
                            },
                            fontSize = 13.sp,
                            fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedCategoryIndex == index) PastelSkyBlueDark else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // --- 3. FILTER RENTANG TANGGAL (LANGSUNG DI HALAMAN UTAMA) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PastelCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PastelSkyBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = PastelSkyBlueDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Filter Rentang Tanggal",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (startDate.isNotBlank() || endDate.isNotBlank())
                                            "Periode Kustom: ${startDate.ifBlank { "..." }} s/d ${endDate.ifBlank { "..." }}"
                                        else if (selectedDateRange != "Semua") "Periode Preset: $selectedDateRange"
                                        else "Menampilkan semua tanggal data",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (startDate.isNotBlank() || endDate.isNotBlank() || selectedDateRange != "Semua") {
                                Surface(
                                    onClick = {
                                        startDate = ""
                                        endDate = ""
                                        selectedDateRange = "Semua"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = PastelPeach,
                                    contentColor = PastelPeachDark
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Reset", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Preset Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Semua", "Hari Ini", "Bulan Ini", "Tahun Ini").forEach { preset ->
                                val isSelected = selectedDateRange == preset && startDate.isBlank() && endDate.isBlank()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDateRange = preset
                                        startDate = ""
                                        endDate = ""
                                    },
                                    label = { Text(preset, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PastelSkyBlue,
                                        selectedLabelColor = PastelSkyBlueDark,
                                        containerColor = PastelSurface,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = PastelCardBorder,
                                        selectedBorderColor = PastelSkyBlueDark,
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.dp
                                    ),
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Form Pilihan Filter Tanggal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DatePickerField(
                                value = startDate,
                                onDateSelected = { newDate ->
                                    startDate = newDate
                                    selectedDateRange = "Kustom"
                                },
                                label = "Dari Tanggal",
                                placeholder = "DD/MM/YYYY",
                                modifier = Modifier.weight(1f)
                            )

                            DatePickerField(
                                value = endDate,
                                onDateSelected = { newDate ->
                                    endDate = newDate
                                    selectedDateRange = "Kustom"
                                },
                                label = "Sampai Tanggal",
                                placeholder = "DD/MM/YYYY",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- 4. ACTION BUTTONS FOR EXPORT ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PastelCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = PastelSkyBlueDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Aksi Ekspor Dokumen:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text(
                                text = "Pilih Format",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // PDF
                            Button(
                                onClick = { triggerExportPdf() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Excel
                            Button(
                                onClick = { triggerExportExcel() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Excel", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Word
                            Button(
                                onClick = { triggerExportWord() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Word", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // --- 5. LIVE METRICS SUMMARY & PREVIEW TABLE ---
            item {
                Text(
                    text = "RINGKASAN METRIK ${currentCategory.title.uppercase(Locale.getDefault())}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                when (currentCategory) {
                    ReportCategory.HELPDESK -> HelpdeskSummaryMetrics(filteredHelpdesk)
                    ReportCategory.ACTION_PLAN -> ActionPlanSummaryMetrics(filteredActionPlans)
                    ReportCategory.INVENTARIS_RUANG -> InventarisSummaryMetrics(filteredRuang, allPeminjaman)
                    ReportCategory.ADMINISTRASI -> AdministrasiSummaryMetrics(filteredSurat)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PRATINJAU DATA SIAP CETAK (${
                        when(currentCategory) {
                            ReportCategory.HELPDESK -> "${filteredHelpdesk.size} Item"
                            ReportCategory.ACTION_PLAN -> "${filteredActionPlans.size} Item"
                            ReportCategory.INVENTARIS_RUANG -> "${filteredRuang.size} Ruang"
                            ReportCategory.ADMINISTRASI -> "${filteredSurat.size} Dokumen"
                        }
                    })",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // --- PREVIEW DATA LIST ---
            when (currentCategory) {
                ReportCategory.HELPDESK -> {
                    items(filteredHelpdesk) { item ->
                        HelpdeskPreviewCard(item)
                    }
                }
                ReportCategory.ACTION_PLAN -> {
                    items(filteredActionPlans) { item ->
                        ActionPlanPreviewCard(item)
                    }
                }
                ReportCategory.INVENTARIS_RUANG -> {
                    items(filteredRuang) { item ->
                        RuangPreviewCard(item)
                    }
                }
                ReportCategory.ADMINISTRASI -> {
                    items(filteredSurat) { item ->
                        SuratPreviewCard(item)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                title = "Filter Laporan Dinamis",
                filterGroups = listOf(
                    FilterOptionGroup(
                        title = "Kategori Laporan",
                        selectedValue = tempCategoryName,
                        options = categoryTitleList,
                        onValueChange = { tempCategoryName = it }
                    ),
                    FilterOptionGroup(
                        title = "Periode Waktu",
                        selectedValue = tempDateRange,
                        options = dateRanges,
                        onValueChange = { tempDateRange = it }
                    ),
                    FilterOptionGroup(
                        title = "Lokasi / Ruangan",
                        selectedValue = tempRuang,
                        options = ruangOptions,
                        onValueChange = { tempRuang = it }
                    )
                ),
                onDismiss = { showFilterDialog = false },
                onApply = {
                    val catIndex = categories.indexOfFirst { it.title == tempCategoryName }
                    if (catIndex >= 0) {
                        selectedCategoryIndex = catIndex
                    }
                    selectedDateRange = tempDateRange
                    selectedRuang = tempRuang
                    showFilterDialog = false
                }
            )
        }
    }
}

// =============================================================================
// SUB-COMPONENTS FOR METRICS & PREVIEW CARDS
// =============================================================================

@Composable
private fun HelpdeskSummaryMetrics(list: List<HelpdeskReport>) {
    val total = list.size
    val belum = list.count { it.status.contains("Belum", ignoreCase = true) || it.status.contains("Pending", ignoreCase = true) }
    val proses = list.count { it.status.contains("Proses", ignoreCase = true) || it.status.contains("Perbaikan", ignoreCase = true) }
    val selesai = list.count { it.status.contains("Selesai", ignoreCase = true) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricBox("Total Laporan", "$total", PastelSkyBlueContainer, PastelSkyBlueDark, Modifier.weight(1f))
        MetricBox("Belum / Pending", "$belum", PastelPeach, PastelPeachDark, Modifier.weight(1f))
        MetricBox("Proses", "$proses", PastelButterYellow, PastelButterYellowDark, Modifier.weight(1f))
        MetricBox("Selesai", "$selesai", PastelMint, PastelMintDark, Modifier.weight(1f))
    }
}

@Composable
private fun ActionPlanSummaryMetrics(list: List<ActionPlan>) {
    val total = list.size
    val totalEst = list.sumOf {
        it.estimasiAnggaran?.replace(Regex("[^0-9]"), "")?.toDoubleOrNull() ?: 0.0
    }
    val proses = list.count { !it.statusProgres.contains("Selesai", ignoreCase = true) }
    val selesai = list.count { it.statusProgres.contains("Selesai", ignoreCase = true) }

    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalEst).replace(",00", "")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricBox("Total Action Plan", "$total Proyek", PastelSkyBlueContainer, PastelSkyBlueDark, Modifier.weight(1f))
            MetricBox("Progres / Berjalan", "$proses Proyek", PastelButterYellow, PastelButterYellowDark, Modifier.weight(1f))
            MetricBox("Proyek Selesai", "$selesai Proyek", PastelMint, PastelMintDark, Modifier.weight(1f))
        }
        MetricBox("Total Estimasi Anggaran Revitalisasi", formatRupiah, PastelSkyBlueContainer, PastelSkyBlueDark, Modifier.fillMaxWidth())
    }
}

@Composable
private fun InventarisSummaryMetrics(ruangList: List<Ruang>, peminjamanList: List<PeminjamanMakro>) {
    val totalRuang = ruangList.size
    val totalPinjam = peminjamanList.size

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricBox("Total Ruangan", "$totalRuang Ruang", PastelSkyBlueContainer, PastelSkyBlueDark, Modifier.weight(1f))
        MetricBox("Peminjaman Makro", "$totalPinjam Transaksi", PastelMint, PastelMintDark, Modifier.weight(1f))
    }
}

@Composable
private fun AdministrasiSummaryMetrics(suratList: List<SuratArsip>) {
    val total = suratList.size
    val masuk = suratList.count { it.jenisSurat.contains("Masuk", ignoreCase = true) }
    val keluar = suratList.count { it.jenisSurat.contains("Keluar", ignoreCase = true) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricBox("Total Dokumen", "$total", PastelSkyBlueContainer, PastelSkyBlueDark, Modifier.weight(1f))
        MetricBox("Surat Masuk", "$masuk", PastelMint, PastelMintDark, Modifier.weight(1f))
        MetricBox("Surat Keluar", "$keluar", PastelButterYellow, PastelButterYellowDark, Modifier.weight(1f))
    }
}

@Composable
private fun MetricBox(label: String, value: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, PastelCardBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textColor, maxLines = 1)
        }
    }
}

// PREVIEW ITEM CARDS
@Composable
private fun HelpdeskPreviewCard(item: HelpdeskReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(item.lokasi, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(item.tanggal, fontSize = 11.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.deskripsi, fontSize = 12.sp, color = TextSecondary, maxLines = 2)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusTag(item.urgensi, PastelPeach, PastelPeachDark)
                StatusTag(item.status, PastelSkyBlueContainer, PastelSkyBlueDark)
            }
        }
    }
}

@Composable
private fun ActionPlanPreviewCard(item: ActionPlan) {
    val doubleAmount = item.estimasiAnggaran?.replace(Regex("[^0-9]"), "")?.toDoubleOrNull() ?: 0.0
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(doubleAmount).replace(",00", "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(item.agenda, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(formatRupiah, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelMintDark)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Lokasi: ${item.lokasiRuang ?: "-"} | Target: ${item.targetWaktu}", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            StatusTag("Progres: ${item.statusProgres}", PastelButterYellow, PastelButterYellowDark)
        }
    }
}

@Composable
private fun RuangPreviewCard(item: Ruang) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(item.namaRuang, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                StatusTag(item.kodeRuang, PastelSkyBlueContainer, PastelSkyBlueDark)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Kategori: ${item.kategori}", fontSize = 11.sp, color = TextSecondary)
            Text("PJ: ${item.penanggungJawab}", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun SuratPreviewCard(item: SuratArsip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(item.nomorSurat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(item.tanggalSurat, fontSize = 11.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.perihal, fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusTag(item.jenisSurat, PastelSkyBlueContainer, PastelSkyBlueDark)
                StatusTag(item.statusArsip, PastelMint, PastelMintDark)
            }
        }
    }
}

@Composable
private fun StatusTag(label: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

private fun isDateInRange(
    dateStr: String,
    startDateStr: String,
    endDateStr: String,
    rangeType: String
): Boolean {
    if (rangeType == "Semua" && startDateStr.isBlank() && endDateStr.isBlank()) {
        return true
    }

    val date = parseAnyDate(dateStr) ?: return true

    val cal = java.util.Calendar.getInstance().apply { time = date }
    val now = java.util.Calendar.getInstance()

    if (startDateStr.isBlank() && endDateStr.isBlank()) {
        return when (rangeType) {
            "Hari Ini" -> {
                cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                        cal.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
            }
            "Bulan Ini" -> {
                cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                        cal.get(java.util.Calendar.MONTH) == now.get(java.util.Calendar.MONTH)
            }
            "Tahun Ini" -> {
                cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
            }
            else -> true
        }
    }

    var inRange = true
    if (startDateStr.isNotBlank()) {
        val startDate = parseAnyDate(startDateStr)
        if (startDate != null) {
            val startCal = java.util.Calendar.getInstance().apply {
                time = startDate
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            inRange = inRange && !cal.time.before(startCal.time)
        }
    }
    if (endDateStr.isNotBlank()) {
        val endDate = parseAnyDate(endDateStr)
        if (endDate != null) {
            val endCal = java.util.Calendar.getInstance().apply {
                time = endDate
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }
            inRange = inRange && !cal.time.after(endCal.time)
        }
    }
    return inRange
}

private fun parseAnyDate(dateStr: String): java.util.Date? {
    if (dateStr.isBlank()) return null
    val formats = listOf<SimpleDateFormat>(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("d/M/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    )
    for (fmt in formats) {
        try {
            return fmt.parse(dateStr)
        } catch (_: Exception) {}
    }
    return null
}
