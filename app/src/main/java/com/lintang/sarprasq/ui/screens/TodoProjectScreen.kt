package com.lintang.sarprasq.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import com.lintang.sarprasq.ui.viewmodel.ProgressLogItem
import com.lintang.sarprasq.ui.viewmodel.parseProgressHistory
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import com.lintang.sarprasq.ui.components.DatePickerField
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import com.lintang.sarprasq.ui.components.FilterTriggerButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lintang.sarprasq.data.model.ProjectTask
import com.lintang.sarprasq.data.model.SubKategoriMaster
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellow
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatProgressPercent(value: Double): String {
    val df = java.text.DecimalFormat("0.0######", java.text.DecimalFormatSymbols(Locale.US))
    return df.format(value)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoProjectScreen(
    viewModel: SarprasViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allProjectTasks.collectAsStateWithLifecycle()
    val subKategoriList by viewModel.allSubKategori.collectAsStateWithLifecycle()

    // --- State Kalender ---
    val calendar = remember { Calendar.getInstance() }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) } // 0-based
    var selectedDay by remember { mutableStateOf<Int?>(calendar.get(Calendar.DAY_OF_MONTH)) }

    // --- State Filter & Search ---
    var selectedTypeFilter by remember { mutableStateOf("Semua") } // "Semua", "Harian", "Proyek Revitalisasi", "Rehab Intern"
    var selectedStatusFilter by remember { mutableStateOf("Semua") } // "Semua", "Selesai (100%)", "Dalam Proses (<100%)"
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    // --- State Dialog ---
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToRecordProgress by remember { mutableStateOf<ProjectTask?>(null) }
    var taskToViewHistory by remember { mutableStateOf<ProjectTask?>(null) }

    // Clean dates formatted strings
    val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val selectedDateString = if (selectedDay != null) {
        String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, currentMonth + 1, currentYear)
    } else null

    // Filter tasks based on selected date & filters
    val filteredTasks = tasks.filter { task ->
        val matchesType = when (selectedTypeFilter) {
            "Harian" -> task.type.equals("Harian", ignoreCase = true)
            "Proyek Revitalisasi" -> task.type.equals("Proyek Revitalisasi", ignoreCase = true)
            "Rehab Intern" -> task.type.equals("Rehab Intern", ignoreCase = true)
            else -> true
        }

        val matchesStatus = when (selectedStatusFilter) {
            "Selesai (100%)" -> task.isCompleted || task.totalProgres >= 100.0 || (task.bobotPersen > 0 && (task.totalProgres >= task.bobotPersen || (task.bobotPersen - task.totalProgres) <= 0.00001)) || task.sisaKekuranganProgres <= 0.00001
            "Dalam Proses (<100%)" -> !task.isCompleted && task.sisaKekuranganProgres > 0.00001 && task.totalProgres < 100.0 && (task.bobotPersen <= 0 || task.totalProgres < task.bobotPersen)
            else -> true
        }

        val matchesSearch = searchQuery.isBlank() ||
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.subKategori.contains(searchQuery, ignoreCase = true) ||
                task.notes.contains(searchQuery, ignoreCase = true)

        val matchesDate = if (selectedDateString == null) true else {
            // Check if task single date matches or selectedDate falls inside range
            isDateInTaskRange(selectedDateString, task.startDate, task.endDate)
        }

        matchesType && matchesStatus && matchesSearch && matchesDate
    }

    Box(modifier = modifier.fillMaxSize().background(PastelBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // --- HEADER TITLE & SUMMARY CARD ---
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PastelSkyBlue.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = PastelSkyBlueDark
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "To-Do & Perencana Proyek",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Kalender interaktif & kontrol fisik Revitalisasi / Rehab Intern",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        val totalCount = tasks.size
                        val harianCount = tasks.count { it.type.equals("Harian", ignoreCase = true) }
                        val proyekCount = tasks.count { it.type.equals("Proyek Revitalisasi", ignoreCase = true) }
                        val rehabCount = tasks.count { it.type.equals("Rehab Intern", ignoreCase = true) }
                        val totalProgresAvg = tasks.filter {
                            it.type.equals("Proyek Revitalisasi", ignoreCase = true) || it.type.equals("Rehab Intern", ignoreCase = true)
                        }.map { it.totalProgres }.let { if (it.isNotEmpty()) it.average() else 0.0 }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatSummaryPill(
                                label = "Total",
                                value = "$totalCount",
                                color = PastelSkyBlueDark,
                                container = PastelSkyBlue.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryPill(
                                label = "Harian",
                                value = "$harianCount",
                                color = PastelMintDark,
                                container = PastelMint.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryPill(
                                label = "Proyek",
                                value = "$proyekCount",
                                color = PastelLavenderDark,
                                container = PastelLavender.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryPill(
                                label = "Rehab",
                                value = "$rehabCount",
                                color = PastelButterYellowDark,
                                container = PastelButterYellow.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryPill(
                                label = "Avg Progres",
                                value = String.format(Locale.getDefault(), "%.1f%%", totalProgresAvg),
                                color = PastelPeachDark,
                                container = PastelPeach.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    }
                }
            }

            // --- KALENDER INTERAKTIF BULANAN ---
            item {
                InteractiveMonthlyCalendar(
                    year = currentYear,
                    month = currentMonth,
                    monthName = monthNames[currentMonth],
                    selectedDay = selectedDay,
                    tasks = tasks,
                    onPrevMonth = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                        selectedDay = null
                    },
                    onNextMonth = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                        selectedDay = null
                    },
                    onSelectToday = {
                        val today = Calendar.getInstance()
                        currentYear = today.get(Calendar.YEAR)
                        currentMonth = today.get(Calendar.MONTH)
                        selectedDay = today.get(Calendar.DAY_OF_MONTH)
                    },
                    onDayClick = { day ->
                        selectedDay = if (selectedDay == day) null else day
                    }
                )
            }

            // --- FILTER & SEARCH BAR (POSISI SEJAJAR) ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isFilterActive = selectedTypeFilter != "Semua" || selectedStatusFilter != "Semua"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search box (Weight 1f)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Cari tugas atau sub-pekerjaan...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Tombol Ikon Filter (Sejajar di sebelah kanan Search Bar)
                        FilterTriggerButton(
                            activeFilterCount = if (isFilterActive) 1 else 0,
                            onClick = { showFilterDialog = true },
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    // Indicator Active Filter Chips
                    if (isFilterActive || selectedDay != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedTypeFilter != "Semua") {
                                    AssistChip(
                                        onClick = { selectedTypeFilter = "Semua" },
                                        label = { Text("Tipe: $selectedTypeFilter", fontSize = 11.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    )
                                }
                                if (selectedStatusFilter != "Semua") {
                                    AssistChip(
                                        onClick = { selectedStatusFilter = "Semua" },
                                        label = { Text("Status: $selectedStatusFilter", fontSize = 11.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    )
                                }
                            }

                            if (selectedDay != null) {
                                TextButton(
                                    onClick = { selectedDay = null }
                                ) {
                                    Text("Reset Tgl", fontSize = 11.sp, color = PastelSkyBlueDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Selected Date Header indicator
                    if (selectedDay != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PastelSkyBlue.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = PastelSkyBlueDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Menampilkan Tugas Tanggal: $selectedDateString",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelSkyBlueDark,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${filteredTasks.size} Tugas",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // --- TASK CARDS LIST ---
            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tidak Ada Agenda / Tugas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedDay != null) "Tidak ada jadwal untuk tanggal $selectedDateString" else "Klik tombol '+' untuk menambahkan tugas baru.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    ProjectTaskItemCard(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onRecordProgress = { taskToRecordProgress = task },
                        onViewHistory = { taskToViewHistory = task },
                        onDelete = { viewModel.deleteProjectTask(task) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // --- FAB ADD TASK (MINIMALIS IKON +) ---
        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = PastelSkyBlueDark,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Task",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // --- DIALOG FILTER POP-UP 2 KOLOM ---
    if (showFilterDialog) {
        FilterTasksDialog(
            currentTypeFilter = selectedTypeFilter,
            currentStatusFilter = selectedStatusFilter,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { typeFilter, statusFilter ->
                selectedTypeFilter = typeFilter
                selectedStatusFilter = statusFilter
                showFilterDialog = false
            },
            onResetFilter = {
                selectedTypeFilter = "Semua"
                selectedStatusFilter = "Semua"
                showFilterDialog = false
            }
        )
    }

    // --- DIALOG ADD TASK (MULTI-ITEM SIMPAN SEKALIGUS) ---
    if (showAddTaskDialog) {
        AddEditProjectTaskDialog(
            subKategoriMasterList = subKategoriList,
            initialDate = selectedDateString ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            onAddSubKategori = { name, kat -> viewModel.addSubKategori(name, kat) },
            onDismiss = { showAddTaskDialog = false },
            onSubmit = { taskList ->
                taskList.forEach { item ->
                    viewModel.addProjectTask(
                        title = item.title,
                        type = item.type,
                        subKategori = item.subKat,
                        notes = item.notes,
                        startDate = item.startDate,
                        endDate = item.endDate,
                        durasiHari = item.durasi,
                        bobotPersen = item.bobot
                    )
                }
                showAddTaskDialog = false
            }
        )
    }

    // --- DIALOG RECORD DAILY PROGRESS ---
    taskToRecordProgress?.let { task ->
        RecordDailyProgressDialog(
            task = task,
            onDismiss = { taskToRecordProgress = null },
            onSubmit = { addedProgress, notes, dateStr ->
                viewModel.recordDailyProgress(
                    task = task,
                    addedProgress = addedProgress,
                    notes = notes,
                    dateStr = dateStr
                )
                taskToRecordProgress = null
            }
        )
    }

    // --- DIALOG VIEW PROGRESS HISTORY ---
    taskToViewHistory?.let { historyTask ->
        val currentTask = tasks.find { it.id == historyTask.id } ?: historyTask
        ViewProgressHistoryDialog(
            task = currentTask,
            onDismiss = { taskToViewHistory = null },
            onEditEntry = { index, date, percent, notes ->
                viewModel.editProgressLogEntry(currentTask, index, date, percent, notes)
            },
            onDeleteEntry = { index ->
                viewModel.deleteProgressLogEntry(currentTask, index)
            }
        )
    }
}

// --- CALENDAR WEEK SPAN DATA CLASS ---
private data class CalendarWeekSpan(
    val task: ProjectTask,
    val startCol: Int,
    val endCol: Int,
    val isTaskStart: Boolean,
    val isTaskEnd: Boolean,
    val startColDayNumber: Int
)

// --- PALETTE HARMONIS UNTUK VARIASI ITEM GANDA DIBAR KALENDER ---
private val calendarTaskPalettes = listOf(
    Color(0xFF5E35B1), // Deep Violet
    Color(0xFF00838F), // Deep Teal / Cyan
    Color(0xFF1565C0), // Royal Blue
    Color(0xFFE65100), // Amber / Orange
    Color(0xFFAD1457), // Magenta / Crimson
    Color(0xFF6A1B9A), // Amethyst Purple
    Color(0xFFD84315), // Deep Coral
    Color(0xFF0288D1), // Cerulean Blue
    Color(0xFFF57F17), // Deep Yellow / Gold
    Color(0xFF8E24AA), // Vibrant Purple
    Color(0xFF00796B), // Sea Teal
    Color(0xFFC2185B)  // Dark Pink
)

private fun getTaskCalendarProgressInfo(task: ProjectTask): Pair<Int, String> {
    val isLunas = task.isCompleted ||
            task.sisaKekuranganProgres <= 0.00001 ||
            (task.bobotPersen > 0 && task.totalProgres >= task.bobotPersen) ||
            task.totalProgres >= 100.0

    if (isLunas) {
        return Pair(100, "100% - LUNAS")
    }
    if (task.type.equals("Harian", ignoreCase = true)) {
        return if (task.isCompleted) Pair(100, "100% - LUNAS") else Pair(0, "0%")
    }
    val relativePct = if (task.bobotPersen > 0) {
        ((task.totalProgres / task.bobotPersen) * 100.0).coerceIn(0.0, 100.0)
    } else {
        task.totalProgres.coerceIn(0.0, 100.0)
    }
    val rounded = relativePct.toInt()
    val labelStr = if (rounded >= 100) "100% - LUNAS" else "$rounded%"
    return Pair(if (rounded >= 100) 100 else rounded, labelStr)
}

// --- INTERACTIVE MONTHLY CALENDAR COMPOSABLE ---
@Composable
fun InteractiveMonthlyCalendar(
    year: Int,
    month: Int,
    monthName: String,
    selectedDay: Int?,
    tasks: List<ProjectTask>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectToday: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    var calendarViewMode by remember { mutableStateOf("Timeline") } // "Timeline" (Rentang Bar) vs "Ringkas" (Dot)

    val cal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Sunday = 1 -> (1 - 1) = 0 ("Min"), Monday = 2 -> (2 - 1) = 1 ("Sen"), ...
    val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1)

    val daysOfWeek = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Month Switcher Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya", tint = TextPrimary)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSelectToday() }
                ) {
                    Text(
                        text = "$monthName $year",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PastelSkyBlue.copy(alpha = 0.3f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Hari Ini", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PastelSkyBlueDark)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mode Toggle Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (calendarViewMode == "Timeline") PastelLavender.copy(alpha = 0.5f) else Color(0xFFF0F0F0)
                            )
                            .border(
                                1.dp,
                                if (calendarViewMode == "Timeline") PastelLavenderDark.copy(alpha = 0.6f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                calendarViewMode = if (calendarViewMode == "Timeline") "Ringkas" else "Timeline"
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Mode Rentang",
                                tint = if (calendarViewMode == "Timeline") PastelLavenderDark else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (calendarViewMode == "Timeline") "Bar Rentang" else "Ringkas",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (calendarViewMode == "Timeline") PastelLavenderDark else TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onNextMonth) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Berikutnya", tint = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-banner info
            if (calendarViewMode == "Timeline") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PastelLavender.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PastelLavenderDark, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mode Rentang Proyek (Timeline): Bar membentang sesuai tanggal & progres %",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = PastelLavenderDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Days of week header row
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dayName == "Min") Color(0xFFE53935) else TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Days grid (up to 42 cells)
            val totalCells = (firstDayOfWeek + daysInMonth + 6) / 7 * 7
            val rows = totalCells / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 0 until rows) {
                    // Compute week spans for row r
                    val weekSpans = mutableListOf<CalendarWeekSpan>()
                    tasks.forEach { task ->
                        val activeCols = mutableListOf<Int>()
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNumber = cellIndex - firstDayOfWeek + 1
                            if (dayNumber in 1..daysInMonth) {
                                val dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayNumber, month + 1, year)
                                if (isDateInTaskRange(dateStr, task.startDate, task.endDate)) {
                                    activeCols.add(c)
                                }
                            }
                        }

                        if (activeCols.isNotEmpty()) {
                            val startCol = activeCols.minOrNull()!!
                            val endCol = activeCols.maxOrNull()!!

                            val startDayNumber = r * 7 + startCol - firstDayOfWeek + 1
                            val startDayStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", startDayNumber, month + 1, year)

                            val endDayNumber = r * 7 + endCol - firstDayOfWeek + 1
                            val endDayStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", endDayNumber, month + 1, year)

                            val taskEnd = task.endDate.ifBlank { task.startDate }

                            val isTaskStart = (task.startDate == startDayStr)
                            val isTaskEnd = (taskEnd == endDayStr)

                            weekSpans.add(
                                CalendarWeekSpan(
                                    task = task,
                                    startCol = startCol,
                                    endCol = endCol,
                                    isTaskStart = isTaskStart,
                                    isTaskEnd = isTaskEnd,
                                    startColDayNumber = startDayNumber
                                )
                            )
                        }
                    }

                    // Sort week spans: longest and Proyek Revitalisasi first
                    weekSpans.sortWith(
                        compareBy<CalendarWeekSpan> { it.startCol }
                            .thenByDescending { it.endCol - it.startCol }
                            .thenBy {
                                when {
                                    it.task.type.equals("Proyek Revitalisasi", ignoreCase = true) -> 0
                                    it.task.type.equals("Rehab Intern", ignoreCase = true) -> 1
                                    else -> 2
                                }
                            }
                    )

                    // Week Row Container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (calendarViewMode == "Timeline" && weekSpans.isNotEmpty()) Color(0xFFF8F9FA) else Color.Transparent)
                            .border(
                                width = if (calendarViewMode == "Timeline" && weekSpans.isNotEmpty()) 0.5.dp else 0.dp,
                                color = Color.LightGray.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        // Day Numbers Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (c in 0 until 7) {
                                val cellIndex = r * 7 + c
                                val dayNumber = cellIndex - firstDayOfWeek + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayNumber, month + 1, year)
                                    val isSelected = selectedDay == dayNumber

                                    val dayTasks = tasks.filter { isDateInTaskRange(dateStr, it.startDate, it.endDate) }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> PastelSkyBlueDark
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 0.dp else 0.5.dp,
                                                color = if (isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onDayClick(dayNumber) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || dayTasks.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> Color.White
                                                    c == 0 -> Color(0xFFD32F2F)
                                                    else -> TextPrimary
                                                }
                                            )

                                            // Badge dots container for Ringkas mode
                                            if (calendarViewMode == "Ringkas" && dayTasks.isNotEmpty()) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    dayTasks.take(3).forEachIndexed { dotIdx, dTask ->
                                                        val (dPct, _) = getTaskCalendarProgressInfo(dTask)
                                                        val isDLunas = dPct >= 100
                                                        val dotColor = if (isDLunas) Color(0xFF2E7D32) else calendarTaskPalettes[kotlin.math.abs(dTask.id * 31 + dotIdx) % calendarTaskPalettes.size]
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else dotColor)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Span Bars for Timeline Mode
                        if (calendarViewMode == "Timeline" && weekSpans.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))

                            val maxVisibleBars = 3
                            val visibleSpans = weekSpans.take(maxVisibleBars)
                            val hiddenCount = weekSpans.size - maxVisibleBars

                            visibleSpans.forEachIndexed { spanIdx, span ->
                                val task = span.task
                                val isProyek = task.type.equals("Proyek Revitalisasi", ignoreCase = true)
                                val isRehab = task.type.equals("Rehab Intern", ignoreCase = true)

                                val (progressPct, progressLabelText) = getTaskCalendarProgressInfo(task)
                                val isLunas = progressPct >= 100 || progressLabelText.contains("LUNAS")

                                val titleStr = task.title.ifBlank {
                                    task.subKategori.ifBlank {
                                        if (isProyek) "Proyek Revitalisasi" else if (isRehab) "Rehab Intern" else "Kegiatan Harian"
                                    }
                                }
                                val label = "$titleStr ($progressLabelText)"

                                // Distinct multi-item color palette: Assign dynamic color based on task ID and index unless Lunas (Emerald Green)
                                val barBg = if (isLunas) {
                                    Color(0xFF2E7D32) // Emerald Green for LUNAS / Completed
                                } else {
                                    val colorIndex = kotlin.math.abs(task.id * 31 + spanIdx * 17 + task.title.hashCode()) % calendarTaskPalettes.size
                                    calendarTaskPalettes[colorIndex]
                                }

                                val startCol = span.startCol
                                val endCol = span.endCol
                                val colSpan = endCol - startCol + 1

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (startCol > 0) {
                                        Spacer(modifier = Modifier.weight(startCol.toFloat()))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(colSpan.toFloat())
                                            .height(20.dp)
                                            .padding(horizontal = 1.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = if (span.isTaskStart) 6.dp else 1.dp,
                                                    bottomStart = if (span.isTaskStart) 6.dp else 1.dp,
                                                    topEnd = if (span.isTaskEnd) 6.dp else 1.dp,
                                                    bottomEnd = if (span.isTaskEnd) 6.dp else 1.dp
                                                )
                                            )
                                            .background(barBg)
                                            .clickable {
                                                if (span.startColDayNumber in 1..daysInMonth) {
                                                    onDayClick(span.startColDayNumber)
                                                }
                                            }
                                            .padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (!span.isTaskStart) {
                                                Icon(
                                                    imageVector = Icons.Default.ChevronLeft,
                                                    contentDescription = "Lanjutan",
                                                    tint = Color.White.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = when {
                                                        isLunas -> Icons.Default.CheckCircle
                                                        isProyek -> Icons.Default.TrendingUp
                                                        isRehab -> Icons.Default.Assignment
                                                        else -> Icons.Default.CheckCircle
                                                    },
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.95f),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                            }

                                            Text(
                                                text = label,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )

                                            if (!span.isTaskEnd) {
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = "Berlanjut",
                                                    tint = Color.White.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (endCol < 6) {
                                        Spacer(modifier = Modifier.weight((6 - endCol).toFloat()))
                                    }
                                }
                            }

                            if (hiddenCount > 0) {
                                Text(
                                    text = "+$hiddenCount kegiatan/proyek lainnya",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(start = 6.dp, top = 1.dp, bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend Footer with distinct colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 8.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF00796B)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Harian", fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 8.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF5E35B1)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Proyek (Multi-Warna)", fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 8.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE65100)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rehab", fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 8.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF2E7D32)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lunas (100%)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }
        }
    }
}

// --- TASK ITEM CARD COMPOSABLE ---
@Composable
fun ProjectTaskItemCard(
    task: ProjectTask,
    onToggleComplete: () -> Unit,
    onRecordProgress: () -> Unit,
    onViewHistory: () -> Unit,
    onDelete: () -> Unit
) {
    val isProyek = task.type.equals("Proyek Revitalisasi", ignoreCase = true)
    val isRehab = task.type.equals("Rehab Intern", ignoreCase = true)
    val isProyekOrRehab = isProyek || isRehab
    var expandedHistory by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Type Chip + Dates + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type Badge
                    val badgeBg = when {
                        isProyek -> PastelLavender.copy(alpha = 0.4f)
                        isRehab -> PastelButterYellow.copy(alpha = 0.5f)
                        else -> PastelMint.copy(alpha = 0.4f)
                    }
                    val badgeText = when {
                        isProyek -> "PROYEK REVITALISASI"
                        isRehab -> "REHAB INTERN"
                        else -> "KEGIATAN HARIAN"
                    }
                    val badgeColor = when {
                        isProyek -> PastelLavenderDark
                        isRehab -> PastelButterYellowDark
                        else -> PastelMintDark
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    if (task.subKategori.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PastelSkyBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = task.subKategori,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelSkyBlueDark
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
                        contentDescription = "Hapus Task",
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Title & Checkbox for Harian
            Row(verticalAlignment = Alignment.Top) {
                if (!isProyekOrRehab) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PastelMintDark,
                            uncheckedColor = TextSecondary
                        ),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted && !isProyekOrRehab) TextSecondary else TextPrimary,
                        textDecoration = if (task.isCompleted && !isProyekOrRehab) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (task.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.notes,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isProyekOrRehab) "Pelaksanaan: ${task.startDate} s.d. ${task.endDate} (${task.durasiHari} Hari)" else "Tanggal: ${task.startDate}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // --- PROGRESS SECTION FOR PROYEK REVITALISASI & REHAB INTERN ---
            if (isProyekOrRehab) {
                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                val progressFraction = (task.totalProgres / task.bobotPersen).coerceIn(0.0, 1.0).toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progres Fisik Lapangan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${formatProgressPercent(task.totalProgres)}% / ${formatProgressPercent(task.bobotPersen)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) PastelMintDark else if (isRehab) PastelButterYellowDark else PastelLavenderDark
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (task.isCompleted) PastelMintDark else if (isRehab) PastelButterYellowDark else PastelLavenderDark,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stat Row: Total Bobot, Total Progres, Sisa Kekurangan Progres
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PastelSkyBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text("Total Bobot", fontSize = 9.sp, color = TextSecondary)
                            Text("${formatProgressPercent(task.bobotPersen)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelSkyBlueDark)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PastelMint.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text("Capaian", fontSize = 9.sp, color = TextSecondary)
                            Text("${formatProgressPercent(task.totalProgres)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PastelMintDark)
                        }
                    }

                    // SISA KEKURANGAN PROGRES (AUTO CALCULATED & HIGHLIGHTED)
                    val sisa = task.sisaKekuranganProgres
                    val isLunas = sisa <= 0.0 || task.isCompleted
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isLunas) PastelMint.copy(alpha = 0.3f) else PastelPeach.copy(alpha = 0.3f)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text("Sisa Progres", fontSize = 9.sp, color = if (isLunas) PastelMintDark else PastelPeachDark, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isLunas) "0% (LUNAS)" else "${formatProgressPercent(sisa)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLunas) PastelMintDark else PastelPeachDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons for Proyek & Rehab: "Catat Progres Harian" & "Riwayat"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRecordProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRehab) PastelButterYellowDark else PastelLavenderDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Catat Progres", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (task.riwayatProgres.isNotBlank()) {
                        OutlinedButton(
                            onClick = onViewHistory,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isRehab) PastelButterYellowDark else PastelLavenderDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Riwayat", fontSize = 11.sp, color = if (isRehab) PastelButterYellowDark else PastelLavenderDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- STAT SUMMARY PILL ---
@Composable
fun StatSummaryPill(
    label: String,
    value: String,
    color: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(container)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = TextSecondary, maxLines = 1)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

data class SingleTaskSubmitData(
    val title: String,
    val type: String,
    val subKat: String,
    val notes: String,
    val startDate: String,
    val endDate: String,
    val durasi: Int,
    val bobot: Double
)

data class TaskInputItemState(
    var title: String = "",
    var bobotInput: String = "",
    var notes: String = ""
)

// --- DIALOG FILTER POP-UP 2 KOLOM ---
@Composable
fun FilterTasksDialog(
    currentTypeFilter: String,
    currentStatusFilter: String,
    onDismiss: () -> Unit,
    onApplyFilter: (typeFilter: String, statusFilter: String) -> Unit,
    onResetFilter: () -> Unit
) {
    var tempTypeFilter by remember { mutableStateOf(currentTypeFilter) }
    var tempStatusFilter by remember { mutableStateOf(currentStatusFilter) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Dialog Filter
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
                                .background(PastelSkyBlue.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = PastelSkyBlueDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Filter Data Agenda",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                // Grid 2 Kolom Sejajar Horizontal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kolom 1: Tipe Kegiatan
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tipe Kegiatan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        val types = listOf("Semua", "Harian", "Proyek Revitalisasi", "Rehab Intern")
                        types.forEach { t ->
                            val isSel = tempTypeFilter == t
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) PastelSkyBlue.copy(alpha = 0.4f) else Color(0xFFF8FAFC))
                                    .border(1.dp, if (isSel) PastelSkyBlueDark else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .clickable { tempTypeFilter = t }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = t,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) PastelSkyBlueDark else TextPrimary
                                )
                            }
                        }
                    }

                    // Kolom 2: Status Progres
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Status Progres",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        val statuses = listOf("Semua", "Selesai (100%)", "Dalam Proses (<100%)")
                        statuses.forEach { s ->
                            val isSel = tempStatusFilter == s
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) PastelMint.copy(alpha = 0.4f) else Color(0xFFF8FAFC))
                                    .border(1.dp, if (isSel) PastelMintDark else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .clickable { tempStatusFilter = s }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) PastelMintDark else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tombol Aksi Bawah: Reset & Terapkan Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempTypeFilter = "Semua"
                            tempStatusFilter = "Semua"
                            onResetFilter()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp, color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            onApplyFilter(tempTypeFilter, tempStatusFilter)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                    ) {
                        Text("Terapkan Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- DIALOG ADD/EDIT PROJECT TASK (DYNAMIC MULTI-ITEM) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProjectTaskDialog(
    subKategoriMasterList: List<SubKategoriMaster>,
    initialDate: String,
    onAddSubKategori: ((String, String) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSubmit: (tasks: List<SingleTaskSubmitData>) -> Unit
) {
    var type by remember { mutableStateOf("Harian") } // Default Harian for multi-item activities
    var selectedSubKat by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(initialDate) }
    var endDate by remember { mutableStateOf(initialDate) }
    var bobotInput by remember { mutableStateOf("5.0") }

    var dropdownExpanded by remember { mutableStateOf(false) }

    // Dynamic Multi-Item State List
    val taskItems = remember { mutableStateListOf(TaskInputItemState()) }

    // Auto calculate duration in days
    val durasiHari = remember(startDate, endDate) {
        calculateDaysBetween(startDate, endDate)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tambah Multi-Item Kegiatan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                // Mode Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val typeOptions = listOf(
                        Pair("Harian", "Harian"),
                        Pair("Proyek Revitalisasi", "Proyek Revitalisasi"),
                        Pair("Rehab Intern", "Rehab Intern")
                    )
                    typeOptions.forEach { (key, label) ->
                        val selected = type == key
                        FilterChip(
                            selected = selected,
                            onClick = {
                                type = key
                                if (key == "Harian") {
                                    bobotInput = "5.0"
                                } else if (key == "Rehab Intern") {
                                    bobotInput = "10.0"
                                } else {
                                    bobotInput = "15.0"
                                }
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (key) {
                                    "Rehab Intern" -> PastelButterYellow
                                    "Proyek Revitalisasi" -> PastelLavender
                                    else -> PastelMint
                                },
                                selectedLabelColor = when (key) {
                                    "Rehab Intern" -> PastelButterYellowDark
                                    "Proyek Revitalisasi" -> PastelLavenderDark
                                    else -> PastelMintDark
                                },
                                containerColor = Color.White
                            )
                        )
                    }
                }

                // If Proyek Revitalisasi or Rehab Intern -> Sub-Kategori Master Dropdown & Bobot %
                if (type == "Proyek Revitalisasi" || type == "Rehab Intern") {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        val options = subKategoriMasterList.map { it.namaSubKategori }.distinct()
                        val isCustom = selectedSubKat.isNotBlank() && !options.any { it.equals(selectedSubKat.trim(), ignoreCase = true) }
                        val context = androidx.compose.ui.platform.LocalContext.current

                        OutlinedTextField(
                            value = selectedSubKat,
                            onValueChange = {
                                selectedSubKat = it
                                dropdownExpanded = true
                            },
                            readOnly = false,
                            label = { Text(if (type == "Rehab Intern") "Sub-Pekerjaan Rehab Intern" else "Sub-Kategori Pekerjaan") },
                            placeholder = { Text("Ketik nama sub-pekerjaan...", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.6f)) },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCustom && onAddSubKategori != null) {
                                        IconButton(
                                            onClick = {
                                                onAddSubKategori(selectedSubKat.trim(), "Sub-Pekerjaan $type")
                                                android.widget.Toast.makeText(context, "Sub-Kategori '${selectedSubKat.trim()}' disimpan ke Master", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircle, contentDescription = "Simpan ke Master", tint = PastelMintDark, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            if (isCustom && onAddSubKategori != null) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = PastelMintDark, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Tambah \"${selectedSubKat.trim()}\" ke Master", color = PastelMintDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        onAddSubKategori(selectedSubKat.trim(), "Sub-Pekerjaan $type")
                                        dropdownExpanded = false
                                        android.widget.Toast.makeText(context, "Sub-Kategori '${selectedSubKat.trim()}' disimpan ke Master", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                                HorizontalDivider()
                            }
                            options.filter { it.contains(selectedSubKat, ignoreCase = true) }.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedSubKat = option
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Rentang Tanggal Input (Tanggal Mulai & Selesai)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatePickerField(
                            value = startDate,
                            onDateSelected = { startDate = it },
                            label = "Tgl Mulai",
                            modifier = Modifier.weight(1f)
                        )

                        DatePickerField(
                            value = endDate,
                            onDateSelected = { endDate = it },
                            label = "Tgl Selesai",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Duration info badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PastelSkyBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Durasi Pekerjaan: $durasiHari Hari",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSkyBlueDark
                        )
                    }

                    // Bobot Persentase (%) Input (Pagu Alokasi Utama / The Pool)
                    OutlinedTextField(
                        value = bobotInput,
                        onValueChange = { bobotInput = it },
                        label = { Text("Pagu Alokasi Utama / Target Total (%)") },
                        placeholder = { Text(if (type == "Rehab Intern") "Contoh: 10.0%" else "Contoh: 50.0%") },
                        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, tint = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // For Harian: Tanggal Pelaksanaan dengan Pop-up Date Picker Interaktif
                    DatePickerField(
                        value = startDate,
                        onDateSelected = { startDate = it; endDate = it },
                        label = "Tanggal Pelaksanaan (dd/MM/yyyy)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --- LOGIKA ALOKASI BOBOT DINAMIS (DYNAMIC PERCENTAGE DEDUCTOR) ---
                val dfDeductor = remember { java.text.DecimalFormat("#,##0.####", java.text.DecimalFormatSymbols(Locale.US)) }
                fun round4(v: Double): Double = kotlin.math.round(v * 10000.0) / 10000.0

                val rawPaguPoolVal = bobotInput.replace(",", ".").toDoubleOrNull() ?: 100.0
                val paguPoolVal = round4(rawPaguPoolVal)

                val subItemsWithBobotList = taskItems.mapNotNull {
                    val b = it.bobotInput.replace(",", ".").toDoubleOrNull()
                    if (b != null && b > 0.0) round4(b) else null
                }
                val rawTotalSubAllocatedVal = taskItems.sumOf {
                    val b = it.bobotInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                    round4(b)
                }
                val totalSubAllocatedVal = round4(rawTotalSubAllocatedVal)

                val rawSisaPaguVal = paguPoolVal - totalSubAllocatedVal
                val sisaPaguVal = if (kotlin.math.abs(rawSisaPaguVal) < 0.0001) 0.0 else round4(rawSisaPaguVal)
                val isFallbackModeVal = subItemsWithBobotList.isEmpty()
                val isExceededVal = totalSubAllocatedVal > (paguPoolVal + 0.0001)

                // --- INDIKATOR KARTU DYNAMIC PERCENTAGE DEDUCTOR (HANYA UNTUK PROYEK / REHAB / RENCANA) ---
                if (type != "Harian") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isExceededVal -> Color(0xFFFEF2F2)
                                !isFallbackModeVal && sisaPaguVal == 0.0 -> Color(0xFFECFDF5)
                                !isFallbackModeVal && sisaPaguVal > 0.0 -> PastelSkyBlueContainer.copy(alpha = 0.5f)
                                else -> PastelButterYellow.copy(alpha = 0.4f)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isExceededVal -> Color(0xFFFCA5A5)
                                !isFallbackModeVal && sisaPaguVal == 0.0 -> Color(0xFFA7F3D0)
                                !isFallbackModeVal && sisaPaguVal > 0.0 -> PastelSkyBlueDark.copy(alpha = 0.5f)
                                else -> PastelButterYellowDark.copy(alpha = 0.5f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Dynamic Deductor",
                                        tint = if (isExceededVal) Color(0xFFB91C1C) else PastelSkyBlueDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Alokasi Bobot Proyek Dinamis",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExceededVal) Color(0xFFB91C1C) else TextPrimary
                                    )
                                }
                                if (isFallbackModeVal) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PastelButterYellowDark)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Fallback Mode", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            // 3 Kolom Metrik
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Stat 1: Pagu Utama Pool
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.85f))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Pagu Utama", fontSize = 9.sp, color = TextSecondary)
                                        Text("${dfDeductor.format(paguPoolVal)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }

                                // Stat 2: Total Teralokasi Sub
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.85f))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Rincian Sub", fontSize = 9.sp, color = TextSecondary)
                                        Text(
                                            text = "${dfDeductor.format(totalSubAllocatedVal)}%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isExceededVal) Color(0xFFDC2626) else PastelSkyBlueDark
                                        )
                                    }
                                }

                                // Stat 3: Sisa Pagu (Live Deductor Result)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isExceededVal) Color(0xFFFEE2E2) else Color.White.copy(alpha = 0.85f)
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Sisa Pagu", fontSize = 9.sp, color = TextSecondary)
                                        Text(
                                            text = "${dfDeductor.format(sisaPaguVal)}%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isExceededVal -> Color(0xFFB91C1C)
                                                sisaPaguVal == 0.0 && !isFallbackModeVal -> Color(0xFF059669)
                                                else -> TextPrimary
                                            }
                                        )
                                    }
                                }
                            }

                            // Live Visual Progress Bar Track
                            val progressFractionVal = if (paguPoolVal > 0) (totalSubAllocatedVal / paguPoolVal).coerceIn(0.0, 1.0).toFloat() else 0f
                            LinearProgressIndicator(
                                progress = { progressFractionVal },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = when {
                                    isExceededVal -> Color(0xFFEF4444)
                                    sisaPaguVal == 0.0 && !isFallbackModeVal -> Color(0xFF10B981)
                                    else -> PastelSkyBlueDark
                                },
                                trackColor = Color(0xFFE2E8F0)
                            )

                            // Live Status Banner Message
                            when {
                                isExceededVal -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "⚠️ Total rincian sub-pekerjaan (${dfDeductor.format(totalSubAllocatedVal)}%) MELEBIHI Pagu Utama (${dfDeductor.format(paguPoolVal)}%)!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B)
                                        )
                                    }
                                }
                                !isFallbackModeVal && sisaPaguVal > 0.0 -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = PastelSkyBlueDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ℹ️ Sisa Alokasi Pagu: ${dfDeductor.format(sisaPaguVal)}%. Siap dialokasikan ke sub-pekerjaan lainnya.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                                !isFallbackModeVal && sisaPaguVal == 0.0 -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "✅ Sempurna! Pagu Utama (${dfDeductor.format(paguPoolVal)}%) teralokasi 100% tepat tanpa sisa.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }
                                else -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = PastelButterYellowDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "⚡ Fallback Mode: Sub-pekerjaan dikosongkan. Nilai Pagu Utama (${dfDeductor.format(paguPoolVal)}%) otomatis dianggap sebagai nilai tunggal utuh.",
                                            fontSize = 11.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- DAFTAR BARIS INPUT SUB-PEKERJAAN DINAMIS ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (type == "Harian") "Daftar Catatan Kegiatan Harian (${taskItems.size})" else "Daftar Sub-Pekerjaan Opsional (${taskItems.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (type == "Harian") "Catatan kegiatan & deskripsi harian" else "Rincian sub-item & bobot masing-masing",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }

                    taskItems.forEachIndexed { index, itemState ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PastelBackground.copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (type == "Harian") "Kegiatan #${index + 1}" else "Sub-Pekerjaan #${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelSkyBlueDark
                                    )
                                    if (taskItems.size > 1) {
                                        IconButton(
                                            onClick = { taskItems.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Hapus Baris",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                // Nama / Catatan Kegiatan
                                OutlinedTextField(
                                    value = itemState.title,
                                    onValueChange = { newTitle ->
                                        taskItems[index] = taskItems[index].copy(title = newTitle)
                                    },
                                    label = { Text(if (type == "Harian") "Catatan Kegiatan / Judul Harian *" else if (type == "Rehab Intern") "Nama Sub-Pekerjaan Rehab *" else "Nama Sub-Pekerjaan Proyek *") },
                                    placeholder = { Text(if (type == "Harian") "Misal: Pembersihan dan perawatan AC Lab Komputer" else "Misal: Pengecatan dinding kelas") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Bobot Persentase Sub-Pekerjaan (%) Live Deductor Input (HANYA UNTUK PROYEK / REHAB / RENCANA)
                                if (type != "Harian") {
                                    OutlinedTextField(
                                        value = itemState.bobotInput,
                                        onValueChange = { newBobot ->
                                            taskItems[index] = taskItems[index].copy(bobotInput = newBobot)
                                        },
                                        label = { Text("Bobot Sub-Pekerjaan (%) [Opsional]") },
                                        placeholder = { Text("Misal: 10.0") },
                                        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                OutlinedTextField(
                                    value = itemState.notes,
                                    onValueChange = { newNotes ->
                                        taskItems[index] = taskItems[index].copy(notes = newNotes)
                                    },
                                    label = { Text(if (type == "Harian") "Detail Deskripsi Harian / Keterangan" else "Catatan / Keterangan Tambahan") },
                                    placeholder = { Text(if (type == "Harian") "Misal: Filter AC telah dibersihkan dan freon diperiksa..." else "Keterangan opsional...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            }
                        }
                    }

                    // Tombol Tambah Baris Sub-Pekerjaan / Kegiatan (+)
                    OutlinedButton(
                        onClick = { taskItems.add(TaskInputItemState()) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (type == "Harian") "+ Tambah Catatan Kegiatan Lain" else "+ Tambah Sub-Pekerjaan Lain",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Submit Button: Simpan Semua
                val contextDialog = androidx.compose.ui.platform.LocalContext.current
                Button(
                    onClick = {
                        val validItems = taskItems.filter { it.title.isNotBlank() }
                        if (validItems.isEmpty()) {
                            android.widget.Toast.makeText(contextDialog, if (type == "Harian") "Harap isi minimal 1 Catatan Kegiatan Harian!" else "Harap isi minimal 1 Nama Sub-Pekerjaan!", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (type != "Harian" && isExceededVal) {
                            android.widget.Toast.makeText(contextDialog, "Total rincian sub-pekerjaan melebihi Pagu Utama (${dfDeductor.format(paguPoolVal)}%)! Mohon kurangi bobot.", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val finalSubKat = if (type == "Harian") "Harian" else selectedSubKat

                        val submitList = validItems.map { item ->
                            val finalBobot = if (type == "Harian") {
                                0.0
                            } else {
                                val itemBobotVal = item.bobotInput.replace(",", ".").toDoubleOrNull()
                                if (itemBobotVal != null && itemBobotVal > 0.0) {
                                    round4(itemBobotVal)
                                } else if (isFallbackModeVal) {
                                    round4(paguPoolVal / validItems.size)
                                } else {
                                    val unweightedCount = validItems.count { it.bobotInput.replace(",", ".").toDoubleOrNull() == null }
                                    if (unweightedCount > 0 && sisaPaguVal > 0) round4(sisaPaguVal / unweightedCount) else paguPoolVal
                                }
                            }

                            SingleTaskSubmitData(
                                title = item.title,
                                type = type,
                                subKat = finalSubKat,
                                notes = item.notes,
                                startDate = startDate,
                                endDate = endDate,
                                durasi = durasiHari,
                                bobot = finalBobot
                            )
                        }
                        onSubmit(submitList)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (type) {
                            "Rehab Intern" -> PastelButterYellowDark
                            "Proyek Revitalisasi" -> PastelLavenderDark
                            else -> PastelSkyBlueDark
                        }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Semua", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- DIALOG RECORD DAILY PROGRESS ---
@Composable
fun RecordDailyProgressDialog(
    task: ProjectTask,
    onDismiss: () -> Unit,
    onSubmit: (addedProgress: Double, notes: String, dateStr: String) -> Unit
) {
    var addedInput by remember { mutableStateOf("5.0") }
    var notesInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    val addedVal = addedInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val currentTotal = task.totalProgres
    val targetBobot = task.bobotPersen
    val newTotal = (currentTotal + addedVal).coerceAtMost(100.0)
    val newSisa = (targetBobot - newTotal).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tambah Progres Harian",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Form penambahan kumulatif capaian progres harian",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                // Task Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (task.type.equals("Rehab Intern", ignoreCase = true)) PastelButterYellow.copy(alpha = 0.3f)
                            else PastelLavender.copy(alpha = 0.2f)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(task.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Sub-Kategori: ${task.subKategori}", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // Progress Input
                OutlinedTextField(
                    value = addedInput,
                    onValueChange = { addedInput = it },
                    label = { Text("Tambah Persentase Hari Ini (%)") },
                    placeholder = { Text("Misal: 0.224 atau 5.0") },
                    leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, tint = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Input dengan Pop-up Date Picker Interaktif
                DatePickerField(
                    value = dateInput,
                    onDateSelected = { dateInput = it },
                    label = "Tanggal Input (dd/MM/yyyy)",
                    modifier = Modifier.fillMaxWidth()
                )

                // Progress Notes Input
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan / Kendala Fisik Lapangan") },
                    placeholder = { Text("Misal: Pembongkaran plafon tua rampung 80%") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Realtime Preview Calculation Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Kalkulasi Realtime Otomatis:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Akumulasi Saat Ini:", fontSize = 11.sp, color = TextSecondary)
                            Text("${formatProgressPercent(currentTotal)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Akumulasi Baru:", fontSize = 11.sp, color = TextSecondary)
                            Text("${formatProgressPercent(newTotal)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelMintDark)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sisa Kekurangan Progres:", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (newSisa <= 0.0) "0% (LUNAS / TERCAPAI)" else "${formatProgressPercent(newSisa)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (newSisa <= 0.0) PastelMintDark else PastelPeachDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (addedVal <= 0.0) return@Button
                        onSubmit(addedVal, notesInput, dateInput)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.type.equals("Rehab Intern", ignoreCase = true)) PastelButterYellowDark else PastelLavenderDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("+ Tambah Progres Harian", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- DIALOG VIEW PROGRESS HISTORY ---
@Composable
fun ViewProgressHistoryDialog(
    task: ProjectTask,
    onDismiss: () -> Unit,
    onEditEntry: (Int, String, Double, String) -> Unit,
    onDeleteEntry: (Int) -> Unit
) {
    var editingEntry by remember { mutableStateOf<ProgressLogItem?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Progres Lapangan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelLavenderDark
                )

                val logItems = remember(task.riwayatProgres) { parseProgressHistory(task.riwayatProgres) }

                if (logItems.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PastelBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            Text("Belum ada riwayat progres harian yang dicatat.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logItems) { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PastelBackground,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.date.ifBlank { "Tanggal -" },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(PastelMint.copy(alpha = 0.5f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "+${formatProgressPercent(item.percent)}%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PastelMintDark
                                                )
                                            }
                                        }
                                        if (item.notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.notes,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }

                                    // Action buttons row: Edit pencil icon directly to the left of Delete trash icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { editingEntry = item },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Riwayat Progres",
                                                tint = PastelSkyBlueDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteEntry(item.index) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Riwayat Progres",
                                                tint = Color(0xFFE53935),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tutup", fontSize = 13.sp, color = PastelSkyBlueDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Sub-dialog pop-up when editing a specific log entry
    editingEntry?.let { entry ->
        EditProgressEntryDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onSubmit = { index, newDate, newPercent, newNotes ->
                onEditEntry(index, newDate, newPercent, newNotes)
                editingEntry = null
            }
        )
    }
}

// --- DIALOG EDIT INDIVIDUAL LOG ENTRY ---
@Composable
fun EditProgressEntryDialog(
    entry: ProgressLogItem,
    onDismiss: () -> Unit,
    onSubmit: (Int, String, Double, String) -> Unit
) {
    var dateInput by remember { mutableStateOf(entry.date) }
    var percentInput by remember { mutableStateOf(formatProgressPercent(entry.percent)) }
    var notesInput by remember { mutableStateOf(entry.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Riwayat Progres",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = percentInput,
                    onValueChange = { percentInput = it },
                    label = { Text("Capaian Progres (%)") },
                    placeholder = { Text("Misal: 0.224") },
                    leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, tint = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    value = dateInput,
                    onDateSelected = { dateInput = it },
                    label = "Tanggal (dd/MM/yyyy)",
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan / Kendala Fisik Lapangan") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Button(
                    onClick = {
                        val pVal = percentInput.replace(",", ".").toDoubleOrNull() ?: entry.percent
                        onSubmit(entry.index, dateInput, pVal, notesInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Simpan Perubahan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- HELPER DATE CALCULATOR ---
fun calculateDaysBetween(startDateStr: String, endDateStr: String): Int {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val start = sdf.parse(startDateStr) ?: return 1
        val end = sdf.parse(endDateStr) ?: return 1

        val diff = end.time - start.time
        val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        if (days <= 0) 1 else days
    } catch (e: Exception) {
        1
    }
}

fun isDateInTaskRange(targetDateStr: String, startDateStr: String, endDateStr: String): Boolean {
    if (targetDateStr == startDateStr || targetDateStr == endDateStr) return true
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val target = sdf.parse(targetDateStr) ?: return false
        val start = sdf.parse(startDateStr) ?: return false
        val end = if (endDateStr.isBlank()) start else (sdf.parse(endDateStr) ?: start)

        !target.before(start) && !target.after(end)
    } catch (e: Exception) {
        false
    }
}
