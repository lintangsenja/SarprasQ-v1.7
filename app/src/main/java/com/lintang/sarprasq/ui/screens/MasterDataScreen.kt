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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.data.model.KategoriMaster
import com.lintang.sarprasq.data.model.KondisiMaster
import com.lintang.sarprasq.data.model.SubKategoriMaster
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.data.model.SatuanMaster
import com.lintang.sarprasq.data.model.StatusPenanganan
import com.lintang.sarprasq.data.model.UrgensiMaster
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellow
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelMintLight
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel

fun cleanMasterDataText(text: String): String {
    return text
        .replace("Ruang Pembelajaran ", "", ignoreCase = true)
        .replace("Ruang Pembelajaran", "", ignoreCase = true)
        .replace("Fasilitas & Area ", "", ignoreCase = true)
        .replace("Fasilitas & Area", "", ignoreCase = true)
        .trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDataScreen(
    viewModel: SarprasViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 6)) }

    val kategoriList by viewModel.allKategori.collectAsState()
    val subKategoriList by viewModel.allSubKategori.collectAsState()
    val ruangList by viewModel.allRuang.collectAsState()
    val satuanList by viewModel.allSatuan.collectAsState()
    val statusList by viewModel.allStatusPenanganan.collectAsState()
    val urgensiList by viewModel.allUrgensi.collectAsState()
    val kondisiList by viewModel.allKondisi.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelBackground)
    ) {
        // --- TAB BAR (6 Menu Utama Master Data) ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = PastelSurface,
            contentColor = PastelSkyBlueDark,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PastelSkyBlueDark,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kategori (${kategoriList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sub-Kategori (${subKategoriList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            )

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ruang (${ruangList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            )

            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Satuan (${satuanList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            )

            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Rule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Progres (${statusList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            )

            Tab(
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Urgensi (${urgensiList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            )

            Tab(
                selected = selectedTab == 6,
                onClick = { selectedTab = 6 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kondisi (${kondisiList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            )
        }

        when (selectedTab) {
            0 -> KategoriTabContent(kategoriList = kategoriList, viewModel = viewModel)
            1 -> SubKategoriTabContent(subKategoriList = subKategoriList, viewModel = viewModel)
            2 -> RuangTabContent(ruangList = ruangList, viewModel = viewModel)
            3 -> SatuanTabContent(satuanList = satuanList, viewModel = viewModel)
            4 -> StatusPenangananTabContent(statusList = statusList, viewModel = viewModel)
            5 -> UrgensiTabContent(urgensiList = urgensiList, viewModel = viewModel)
            6 -> KondisiTabContent(kondisiList = kondisiList, viewModel = viewModel)
        }
    }
}

// ==========================================
// 1. TAB KATEGORI
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KategoriTabContent(
    kategoriList: List<KategoriMaster>,
    viewModel: SarprasViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingKategori by remember { mutableStateOf<KategoriMaster?>(null) }
    var deletingKategori by remember { mutableStateOf<KategoriMaster?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredList = remember(kategoriList, searchQuery) {
        if (searchQuery.isBlank()) kategoriList
        else kategoriList.filter {
            it.namaKategori.contains(searchQuery, ignoreCase = true) ||
            it.deskripsi.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Banner / Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PastelButterYellow),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PastelButterYellowDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kelola Master Kategori Sarpras",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Pengelompokan jenis dan kategori sarana prasarana sekolah secara fleksibel.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari kategori atau deskripsi...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }

            // Action Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Kategori (${filteredList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kosongkan Kategori", fontSize = 11.sp)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Belum ada data Kategori" else "Kategori tidak ditemukan",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                val chunkedList = filteredList.chunked(2)
                items(chunkedList, key = { pair -> pair.map { it.id }.joinToString("-") }) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (item in pair) {
                            Box(modifier = Modifier.weight(1f)) {
                                KategoriCardItem(
                                    kategori = item,
                                    onEdit = { editingKategori = item },
                                    onDelete = { deletingKategori = item }
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PastelButterYellowDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Kategori", modifier = Modifier.size(24.dp))
        }
    }

    // Dialog Tambah / Edit Kategori
    if (showAddDialog || editingKategori != null) {
        KategoriFormDialog(
            kategori = editingKategori,
            onDismiss = {
                showAddDialog = false
                editingKategori = null
            },
            onSave = { nama, desk ->
                val cleanNama = cleanMasterDataText(nama)
                val cleanDesk = cleanMasterDataText(desk)
                if (editingKategori != null) {
                    viewModel.updateKategori(
                        editingKategori!!.copy(
                            namaKategori = cleanNama,
                            deskripsi = cleanDesk
                        )
                    )
                } else {
                    viewModel.addKategori(
                        namaKategori = cleanNama,
                        deskripsi = cleanDesk
                    )
                }
                showAddDialog = false
                editingKategori = null
            }
        )
    }

    // Dialog Konfirmasi Hapus Kategori
    if (deletingKategori != null) {
        AlertDialog(
            onDismissRequest = { deletingKategori = null },
            title = { Text("Hapus Kategori?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus data kategori '${cleanMasterDataText(deletingKategori?.namaKategori ?: "")}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingKategori?.let { viewModel.deleteKategori(it) }
                        deletingKategori = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingKategori = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Kosongkan Kategori
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Kosongkan Master Kategori?", fontWeight = FontWeight.Bold) },
            text = { Text("Fitur ini akan mengosongkan seluruh data master kategori.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDefaultKategori()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark)
                ) {
                    Text("Kosongkan Data", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun KategoriCardItem(
    kategori: KategoriMaster,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayNama = cleanMasterDataText(kategori.namaKategori)
    val displayDeskripsi = cleanMasterDataText(kategori.deskripsi)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PastelCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PastelButterYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = PastelButterYellowDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PastelSkyBlueDark, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = PastelPeachDark, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = displayNama,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = displayDeskripsi.ifBlank { "Tidak ada deskripsi" },
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun KategoriFormDialog(
    kategori: KategoriMaster?,
    onDismiss: () -> Unit,
    onSave: (nama: String, deskripsi: String) -> Unit
) {
    var namaKategori by remember { mutableStateOf(cleanMasterDataText(kategori?.namaKategori ?: "")) }
    var deskripsi by remember { mutableStateOf(cleanMasterDataText(kategori?.deskripsi ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (kategori == null) "Tambah Kategori Baru" else "Edit Kategori", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = namaKategori,
                    onValueChange = { namaKategori = it },
                    label = { Text("Nama Kategori") },
                    placeholder = { Text("Contoh: Peralatan Elektronik") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Contoh: Laptop, Proyektor, Sound System") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namaKategori.isNotBlank()) {
                        onSave(namaKategori, deskripsi)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelButterYellowDark),
                enabled = namaKategori.isNotBlank()
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ==========================================
// 1B. TAB SUB-KATEGORI / SIFAT PEKERJAAN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubKategoriTabContent(
    subKategoriList: List<SubKategoriMaster>,
    viewModel: SarprasViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<SubKategoriMaster?>(null) }
    var deletingItem by remember { mutableStateOf<SubKategoriMaster?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredList = remember(subKategoriList, searchQuery) {
        if (searchQuery.isBlank()) subKategoriList
        else subKategoriList.filter {
            it.namaSubKategori.contains(searchQuery, ignoreCase = true) ||
            it.deskripsi.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Banner / Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PastelSkyBlueContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PastelSkyBlueDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Class,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kelola Sub-Kategori / Sifat Pekerjaan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Master sifat pekerjaan/sub-kategori yang terhubung langsung dengan opsi Rencana Kerja.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari sub-kategori / sifat pekerjaan...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }

            // Action Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Sub-Kategori (${filteredList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Default", fontSize = 11.sp)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Belum ada data Sub-Kategori Pekerjaan" else "Data tidak ditemukan",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                val chunkedList = filteredList.chunked(2)
                items(chunkedList, key = { pair -> pair.map { it.id }.joinToString("-") }) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (item in pair) {
                            Box(modifier = Modifier.weight(1f)) {
                                SubKategoriCardItem(
                                    subKategori = item,
                                    onEdit = { editingItem = item },
                                    onDelete = { deletingItem = item }
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PastelSkyBlueDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Sub-Kategori", modifier = Modifier.size(24.dp))
        }
    }

    // Dialog Tambah / Edit
    if (showAddDialog || editingItem != null) {
        SubKategoriFormDialog(
            subKategori = editingItem,
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onSave = { nama, desk ->
                val cleanNama = cleanMasterDataText(nama)
                val cleanDesk = cleanMasterDataText(desk)
                if (editingItem != null) {
                    viewModel.updateSubKategori(
                        editingItem!!.copy(
                            namaSubKategori = cleanNama,
                            deskripsi = cleanDesk
                        )
                    )
                } else {
                    viewModel.addSubKategori(
                        namaSubKategori = cleanNama,
                        deskripsi = cleanDesk
                    )
                }
                showAddDialog = false
                editingItem = null
            }
        )
    }

    // Dialog Hapus
    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Hapus Sub-Kategori Pekerjaan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Apakah Anda yakin ingin menghapus '${item.namaSubKategori}'?", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubKategori(item)
                        deletingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Reset Default
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Master Sub-Kategori", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Tindakan ini akan mengembalikan data Master Sub-Kategori ke opsi default standar (Pemeliharaan Berkala, Perbaikan Minor, Perbaikan Mayor, Pengadaan Barang Baru). Lanjutkan?", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDefaultSubKategori()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark)
                ) {
                    Text("Reset Default", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SubKategoriCardItem(
    subKategori: SubKategoriMaster,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayNama = cleanMasterDataText(subKategori.namaSubKategori)
    val displayDeskripsi = cleanMasterDataText(subKategori.deskripsi)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PastelCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PastelSkyBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Class,
                        contentDescription = null,
                        tint = PastelSkyBlueDark,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = PastelSkyBlueDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = displayNama,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = displayDeskripsi.ifBlank { "Tidak ada deskripsi" },
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SubKategoriFormDialog(
    subKategori: SubKategoriMaster?,
    onDismiss: () -> Unit,
    onSave: (nama: String, deskripsi: String) -> Unit
) {
    var nama by remember { mutableStateOf(cleanMasterDataText(subKategori?.namaSubKategori ?: "")) }
    var deskripsi by remember { mutableStateOf(cleanMasterDataText(subKategori?.deskripsi ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subKategori == null) "Tambah Sub-Kategori Pekerjaan" else "Edit Sub-Kategori", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Sub-Kategori / Sifat Pekerjaan") },
                    placeholder = { Text("Contoh: Pemeliharaan Berkala") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Contoh: Perawatan dan pemeliharaan rutin sarana prasarana") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank()) {
                        onSave(nama, deskripsi)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                enabled = nama.isNotBlank()
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ==========================================
// 2. TAB RUANG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuangTabContent(
    ruangList: List<Ruang>,
    viewModel: SarprasViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRuang by remember { mutableStateOf<Ruang?>(null) }
    var deletingRuang by remember { mutableStateOf<Ruang?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Accordion Expansion States
    var expandedGroupX by remember { mutableStateOf(false) }
    var expandedGroupXI by remember { mutableStateOf(false) }
    var expandedGroupXII by remember { mutableStateOf(false) }

    // Dynamic Filter Categories
    val availableCategories = remember(ruangList) {
        val base = listOf("Semua", "Ruang Kelas", "Ruang Kerja", "Fasilitas Umum")
        val custom = ruangList.map { cleanMasterDataText(it.kategori) }.filter { it.isNotBlank() }.distinct()
        (base + custom).distinct()
    }

    // Filter by Category & Search Query
    val filteredList = remember(ruangList, searchQuery, selectedCategory) {
        ruangList.filter { item ->
            val matchesCategory = if (selectedCategory == "Semua") true else cleanMasterDataText(item.kategori).equals(selectedCategory, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else (
                item.namaRuang.contains(searchQuery, ignoreCase = true) ||
                item.kodeRuang.contains(searchQuery, ignoreCase = true) ||
                item.kategori.contains(searchQuery, ignoreCase = true) ||
                item.penanggungJawab.contains(searchQuery, ignoreCase = true)
            )
            matchesCategory && matchesSearch
        }
    }

    // Separate into Classroom Groups vs Other Rooms
    val groupX = remember(filteredList) {
        filteredList.filter {
            it.namaRuang.contains("Kelas X ", ignoreCase = true) ||
            it.namaRuang.endsWith("Kelas X", ignoreCase = true) ||
            it.kodeRuang.contains("RK-X-", ignoreCase = true)
        }
    }
    val groupXI = remember(filteredList) {
        filteredList.filter {
            it.namaRuang.contains("Kelas XI ", ignoreCase = true) ||
            it.namaRuang.endsWith("Kelas XI", ignoreCase = true) ||
            it.kodeRuang.contains("RK-XI-", ignoreCase = true)
        }
    }
    val groupXII = remember(filteredList) {
        filteredList.filter {
            it.namaRuang.contains("Kelas XII ", ignoreCase = true) ||
            it.namaRuang.endsWith("Kelas XII", ignoreCase = true) ||
            it.kodeRuang.contains("RK-XII-", ignoreCase = true)
        }
    }
    val otherRooms = remember(filteredList, groupX, groupXI, groupXII) {
        val groupedIds = (groupX + groupXI + groupXII).map { it.id }.toSet()
        filteredList.filter { it.id !in groupedIds }
    }

    val isSearching = searchQuery.isNotBlank()
    val showAccordionX = groupX.isNotEmpty()
    val showAccordionXI = groupXI.isNotEmpty()
    val showAccordionXII = groupXII.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Banner / Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PastelSkyBlueContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PastelSkyBlueDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kelola Master Ruang Sarpras",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Pengelolaan daftar ruang dan lokasi fasilitas sekolah.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari ruang, kode, atau penanggung jawab...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }

            // Filter Chips Kategori
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(availableCategories) { cat ->
                        val isSelected = (cat == selectedCategory)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PastelSkyBlueDark else PastelSkyBlueContainer)
                                .border(1.dp, if (isSelected) PastelSkyBlueDark else PastelSkyBlue, RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else PastelSkyBlueDark
                            )
                        }
                    }
                }
            }

            // Action Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Ruang (${filteredList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kosongkan Ruang", fontSize = 11.sp)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Belum ada data Ruang" else "Ruang tidak ditemukan",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // Accordion 1: Kelas X
                if (showAccordionX) {
                    item {
                        ClassroomAccordionGroup(
                            title = "R. Kelas X (A - J)",
                            subtitle = "Daftar paralel kelas X",
                            count = groupX.size,
                            items = groupX,
                            isExpanded = expandedGroupX || isSearching,
                            onToggle = { expandedGroupX = !expandedGroupX },
                            onEdit = { editingRuang = it },
                            onDelete = { deletingRuang = it }
                        )
                    }
                }

                // Accordion 2: Kelas XI
                if (showAccordionXI) {
                    item {
                        ClassroomAccordionGroup(
                            title = "R. Kelas XI (A - J)",
                            subtitle = "Daftar paralel kelas XI",
                            count = groupXI.size,
                            items = groupXI,
                            isExpanded = expandedGroupXI || isSearching,
                            onToggle = { expandedGroupXI = !expandedGroupXI },
                            onEdit = { editingRuang = it },
                            onDelete = { deletingRuang = it }
                        )
                    }
                }

                // Accordion 3: Kelas XII
                if (showAccordionXII) {
                    item {
                        ClassroomAccordionGroup(
                            title = "R. Kelas XII (A - J)",
                            subtitle = "Daftar paralel kelas XII",
                            count = groupXII.size,
                            items = groupXII,
                            isExpanded = expandedGroupXII || isSearching,
                            onToggle = { expandedGroupXII = !expandedGroupXII },
                            onEdit = { editingRuang = it },
                            onDelete = { deletingRuang = it }
                        )
                    }
                }

                // Fasilitas & Ruangan Lain (Grid 2 Kolom)
                if (otherRooms.isNotEmpty()) {
                    if (showAccordionX || showAccordionXI || showAccordionXII) {
                        item {
                            Text(
                                text = "Fasilitas & Ruangan Lain (${otherRooms.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    val otherRows = otherRooms.chunked(2)
                    items(otherRows) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (item in pair) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CompactRuangGridCard(
                                        ruang = item,
                                        onEdit = { editingRuang = item },
                                        onDelete = { deletingRuang = item }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PastelSkyBlueDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Ruang", modifier = Modifier.size(24.dp))
        }
    }

    // Dialog Tambah / Edit Ruang
    if (showAddDialog || editingRuang != null) {
        RuangFormDialog(
            ruang = editingRuang,
            onDismiss = {
                showAddDialog = false
                editingRuang = null
            },
            onSave = { kode, nama, kat, pj ->
                val cleanKode = cleanMasterDataText(kode)
                val cleanNama = cleanMasterDataText(nama)
                val cleanKat = cleanMasterDataText(kat)
                val cleanPJ = cleanMasterDataText(pj)
                if (editingRuang != null) {
                    viewModel.updateRuang(
                        editingRuang!!.copy(
                            kodeRuang = cleanKode,
                            namaRuang = cleanNama,
                            kategori = cleanKat,
                            penanggungJawab = cleanPJ
                        )
                    )
                } else {
                    viewModel.addRuang(
                        kodeRuang = cleanKode,
                        namaRuang = cleanNama,
                        kategori = cleanKat,
                        penanggungJawab = cleanPJ
                    )
                }
                showAddDialog = false
                editingRuang = null
            }
        )
    }

    // Dialog Konfirmasi Hapus Ruang
    if (deletingRuang != null) {
        AlertDialog(
            onDismissRequest = { deletingRuang = null },
            title = { Text("Hapus Ruang?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus data ruang '${cleanMasterDataText(deletingRuang?.namaRuang ?: "")}' (${deletingRuang?.kodeRuang})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingRuang?.let { viewModel.deleteRuang(it) }
                        deletingRuang = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRuang = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Kosongkan Ruang
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Kosongkan Master Ruang?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Fitur ini akan mengosongkan seluruh data master ruang.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDefaultRuang()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark)
                ) {
                    Text("Kosongkan Data", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ClassroomAccordionGroup(
    title: String,
    subtitle: String,
    count: Int,
    items: List<Ruang>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEdit: (Ruang) -> Unit,
    onDelete: (Ruang) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PastelSkyBlue, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(vertical = 2.dp, horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PastelSkyBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = PastelSkyBlueDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PastelSkyBlueDark)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$count Ruang",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = PastelSkyBlueDark
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = PastelCardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                val chunkedItems = items.chunked(2)
                chunkedItems.forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (item in pair) {
                            Box(modifier = Modifier.weight(1f)) {
                                CompactRuangGridCard(
                                    ruang = item,
                                    onEdit = { onEdit(item) },
                                    onDelete = { onDelete(item) }
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CompactRuangGridCard(
    ruang: Ruang,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayNama = cleanMasterDataText(ruang.namaRuang)
    val displayKode = cleanMasterDataText(ruang.kodeRuang)
    val displayKategori = cleanMasterDataText(ruang.kategori)
    val displayPJ = cleanMasterDataText(ruang.penanggungJawab)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PastelCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PastelSkyBlueContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = displayKode.ifBlank { "R.00" },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelSkyBlueDark
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = PastelSkyBlueDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = PastelPeachDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = displayNama,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (displayPJ.isNotBlank() && displayPJ != "-") displayPJ else displayKategori.ifBlank { "Ruang" },
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RuangCardItem(
    ruang: Ruang,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    CompactRuangGridCard(
        ruang = ruang,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

// ==========================================
// 3. TAB SATUAN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SatuanTabContent(
    satuanList: List<SatuanMaster>,
    viewModel: SarprasViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSatuan by remember { mutableStateOf<SatuanMaster?>(null) }
    var deletingSatuan by remember { mutableStateOf<SatuanMaster?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredList = remember(satuanList, searchQuery) {
        if (searchQuery.isBlank()) satuanList
        else satuanList.filter {
            it.namaSatuan.contains(searchQuery, ignoreCase = true) ||
            it.deskripsi.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PastelMintLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PastelMintDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Class,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kelola Master Satuan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Pengelolaan 16 daftar satuan unit dan pengukuran sarana prasarana secara terstruktur.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari satuan atau deskripsi...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }

            // Action Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Satuan (${filteredList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Ke Default", fontSize = 11.sp)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Belum ada data Satuan" else "Satuan tidak ditemukan",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                val chunkedList = filteredList.chunked(2)
                items(chunkedList, key = { pair -> pair.map { it.id }.joinToString("-") }) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (item in pair) {
                            Box(modifier = Modifier.weight(1f)) {
                                SatuanCardItem(
                                    satuan = item,
                                    onEdit = { editingSatuan = item },
                                    onDelete = { deletingSatuan = item }
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PastelMintDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Satuan", modifier = Modifier.size(24.dp))
        }
    }

    // Dialog Tambah / Edit Satuan
    if (showAddDialog || editingSatuan != null) {
        SatuanFormDialog(
            satuan = editingSatuan,
            onDismiss = {
                showAddDialog = false
                editingSatuan = null
            },
            onSave = { nama, desk ->
                val cleanNama = cleanMasterDataText(nama)
                val cleanDesk = cleanMasterDataText(desk)
                if (editingSatuan != null) {
                    viewModel.updateSatuan(
                        editingSatuan!!.copy(
                            namaSatuan = cleanNama,
                            deskripsi = cleanDesk
                        )
                    )
                } else {
                    viewModel.addSatuan(cleanNama, cleanDesk)
                }
                showAddDialog = false
                editingSatuan = null
            }
        )
    }

    // Dialog Konfirmasi Hapus Satuan
    if (deletingSatuan != null) {
        AlertDialog(
            onDismissRequest = { deletingSatuan = null },
            title = { Text("Hapus Satuan?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus data satuan '${cleanMasterDataText(deletingSatuan?.namaSatuan ?: "")}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingSatuan?.let { viewModel.deleteSatuan(it) }
                        deletingSatuan = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSatuan = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Reset Default Satuan
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Master Satuan?", fontWeight = FontWeight.Bold) },
            text = { Text("Fitur ini akan mengembalikan 16 pilihan satuan default:\n\nBatang, Bungkus, Dus, Kg, Lembar, Liter, Meter, Pak, Pcs, Rim, Roll, Set, Unit, Botol, Box, Galon.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDefaultSatuan()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark)
                ) {
                    Text("Reset Default", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SatuanCardItem(
    satuan: SatuanMaster,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayNama = cleanMasterDataText(satuan.namaSatuan)
    val displayDeskripsi = cleanMasterDataText(satuan.deskripsi)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PastelCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(PastelMintLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayNama.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = PastelMintDark
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PastelSkyBlueDark, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = PastelPeachDark, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = displayNama,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = displayDeskripsi.ifBlank { "Satuan unit/ukur $displayNama" },
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 2
            )
        }
    }
}

@Composable
fun SatuanFormDialog(
    satuan: SatuanMaster?,
    onDismiss: () -> Unit,
    onSave: (nama: String, deskripsi: String) -> Unit
) {
    var namaSatuan by remember { mutableStateOf(cleanMasterDataText(satuan?.namaSatuan ?: "")) }
    var deskripsi by remember { mutableStateOf(cleanMasterDataText(satuan?.deskripsi ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (satuan == null) "Tambah Satuan Baru" else "Edit Satuan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = namaSatuan,
                    onValueChange = { namaSatuan = it },
                    label = { Text("Nama Satuan") },
                    placeholder = { Text("Contoh: Unit, Pcs, Set, Botol") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Contoh: Satuan unit/ukur") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namaSatuan.isNotBlank()) {
                        onSave(namaSatuan, deskripsi)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark),
                enabled = namaSatuan.isNotBlank()
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ==========================================
// 3. TAB STATUS PENANGANAN
// ==========================================
@Composable
fun StatusPenangananTabContent(
    statusList: List<StatusPenanganan>,
    viewModel: SarprasViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStatus by remember { mutableStateOf<StatusPenanganan?>(null) }
    var deletingStatus by remember { mutableStateOf<StatusPenanganan?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredList = remember(statusList, searchQuery) {
        if (searchQuery.isBlank()) statusList
        else statusList.filter {
            it.namaStatus.contains(searchQuery, ignoreCase = true) ||
            it.deskripsi.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Banner Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PastelMintLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PastelMintDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Rule,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daftar Opsi Status Penanganan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Opsi status selaras dan harmonis untuk dropdown pengerjaan laporan.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari status penanganan...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }

            // Default Reset Button bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Opsi (${filteredList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset 4 Option Default", fontSize = 11.sp)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Belum ada data Status Penanganan" else "Status tidak ditemukan",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    StatusPenangananCardItem(
                        status = item,
                        onEdit = { editingStatus = item },
                        onDelete = { deletingStatus = item }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PastelMintDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Status Penanganan", modifier = Modifier.size(24.dp))
        }
    }

    // Form Dialog Status Penanganan
    if (showAddDialog || editingStatus != null) {
        StatusPenangananFormDialog(
            status = editingStatus,
            onDismiss = {
                showAddDialog = false
                editingStatus = null
            },
            onSave = { nama, desk ->
                if (editingStatus != null) {
                    viewModel.updateStatusPenanganan(
                        editingStatus!!.copy(
                            namaStatus = nama,
                            deskripsi = desk
                        )
                    )
                } else {
                    viewModel.addStatusPenanganan(
                        namaStatus = nama,
                        deskripsi = desk
                    )
                }
                showAddDialog = false
                editingStatus = null
            }
        )
    }

    // Delete Confirmation
    if (deletingStatus != null) {
        AlertDialog(
            onDismissRequest = { deletingStatus = null },
            title = { Text("Hapus Status Penanganan?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus opsi status '${deletingStatus?.namaStatus}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingStatus?.let { viewModel.deleteStatusPenanganan(it) }
                        deletingStatus = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingStatus = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Reset Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Ke Opsi Default?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Fitur ini akan mengembalikan 4 pilihan default:\n1. Menunggu / Pending\n2. Diproses / Sedang Dikerjakan\n3. Selesai\n4. Ditolak / Dibatalkan")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDefaultStatusPenanganan()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark)
                ) {
                    Text("Reset Opsi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StatusPenangananCardItem(
    status: StatusPenanganan,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (badgeBg, badgeText, borderColor) = when {
        status.namaStatus.contains("Menunggu", ignoreCase = true) || status.namaStatus.contains("Pending", ignoreCase = true) || status.namaStatus.contains("Segera", ignoreCase = true) -> Triple(PastelSkyBlueContainer, PastelSkyBlueDark, PastelSkyBlue)
        status.namaStatus.contains("Diproses", ignoreCase = true) || status.namaStatus.contains("Proses", ignoreCase = true) || status.namaStatus.contains("Dikerjakan", ignoreCase = true) -> Triple(PastelButterYellow, PastelButterYellowDark, PastelButterYellowDark.copy(alpha = 0.3f))
        status.namaStatus.contains("Selesai", ignoreCase = true) -> Triple(PastelMintLight, PastelMintDark, PastelMintDark.copy(alpha = 0.3f))
        status.namaStatus.contains("Ditolak", ignoreCase = true) || status.namaStatus.contains("Dibatalkan", ignoreCase = true) -> Triple(PastelPeach.copy(alpha = 0.4f), PastelPeachDark, PastelPeachDark.copy(alpha = 0.3f))
        else -> Triple(PastelSkyBlueContainer, PastelSkyBlueDark, PastelSkyBlue)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PastelCardBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = status.namaStatus,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeText
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.deskripsi.ifBlank { "Opsi pilihan status penanganan" },
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PastelSkyBlueDark, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = PastelPeachDark, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ==========================================
// 4. TAB URGENSI
// ==========================================
@Composable
fun UrgensiTabContent(
    urgensiList: List<UrgensiMaster>,
    viewModel: SarprasViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingUrgensi by remember { mutableStateOf<UrgensiMaster?>(null) }
    var deletingUrgensi by remember { mutableStateOf<UrgensiMaster?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredList = urgensiList.filter {
        searchQuery.isBlank() ||
                it.namaUrgensi.contains(searchQuery, ignoreCase = true) ||
                it.deskripsi.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Banner Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PastelButterYellow.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, PastelPeachDark.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PastelPeachDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Daftar Opsi Urgensi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tingkat urgensi laporan (Darurat, Penting, Sedang, Rendah) dengan ikon visual khas sesuai keparahan.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari urgensi...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Info count & Reset
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Opsi (${filteredList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset 4 Option Default", fontSize = 11.sp)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Belum ada data Urgensi" else "Urgensi tidak ditemukan",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    UrgensiCardItem(
                        urgensi = item,
                        onEdit = { editingUrgensi = item },
                        onDelete = { deletingUrgensi = item }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PastelPeachDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Urgensi", modifier = Modifier.size(24.dp))
        }
    }

    // Form Dialog Urgensi
    if (showAddDialog || editingUrgensi != null) {
        UrgensiFormDialog(
            urgensi = editingUrgensi,
            onDismiss = {
                showAddDialog = false
                editingUrgensi = null
            },
            onSave = { nama, desk ->
                if (editingUrgensi != null) {
                    viewModel.updateUrgensi(
                        editingUrgensi!!.copy(
                            namaUrgensi = nama,
                            deskripsi = desk
                        )
                    )
                } else {
                    viewModel.addUrgensi(
                        namaUrgensi = nama,
                        deskripsi = desk
                    )
                }
                showAddDialog = false
                editingUrgensi = null
            }
        )
    }

    // Delete Confirmation
    if (deletingUrgensi != null) {
        AlertDialog(
            onDismissRequest = { deletingUrgensi = null },
            title = { Text("Hapus Opsi Urgensi?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus opsi urgensi '${deletingUrgensi?.namaUrgensi}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingUrgensi?.let { viewModel.deleteUrgensi(it) }
                        deletingUrgensi = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingUrgensi = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Reset Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Ke Opsi Default?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Fitur ini akan mengembalikan 4 pilihan default:\n1. Darurat / Kritis\n2. Tinggi\n3. Sedang\n4. Rendah")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDefaultUrgensi()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark)
                ) {
                    Text("Reset Default", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun UrgensiCardItem(
    urgensi: UrgensiMaster,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, bgCircle, iconTint) = when {
        urgensi.namaUrgensi.contains("Darurat", ignoreCase = true) || urgensi.namaUrgensi.contains("Kritis", ignoreCase = true) -> Triple(Icons.Default.Warning, PastelPeach.copy(alpha = 0.4f), PastelPeachDark)
        urgensi.namaUrgensi.contains("Tinggi", ignoreCase = true) || urgensi.namaUrgensi.contains("Penting", ignoreCase = true) -> Triple(Icons.Default.ReportProblem, PastelButterYellow, PastelButterYellowDark)
        urgensi.namaUrgensi.contains("Sedang", ignoreCase = true) || urgensi.namaUrgensi.contains("Biasa", ignoreCase = true) -> Triple(Icons.Default.Info, PastelSkyBlueContainer, PastelSkyBlueDark)
        urgensi.namaUrgensi.contains("Rendah", ignoreCase = true) -> Triple(Icons.Default.CheckCircle, PastelMintLight, PastelMintDark)
        else -> Triple(Icons.Default.Info, PastelSkyBlueContainer, PastelSkyBlueDark)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgCircle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = urgensi.namaUrgensi,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = urgensi.namaUrgensi,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                if (urgensi.deskripsi.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = urgensi.deskripsi,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = PastelSkyBlueDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = PastelPeachDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// FORM DIALOGS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuangFormDialog(
    ruang: Ruang?,
    onDismiss: () -> Unit,
    onSave: (kode: String, nama: String, kategori: String, pj: String) -> Unit
) {
    var kode by remember { mutableStateOf(ruang?.kodeRuang ?: "") }
    var nama by remember { mutableStateOf(ruang?.namaRuang ?: "") }
    var kategori by remember { mutableStateOf(ruang?.kategori ?: "Ruang Kelas") }
    var penanggungJawab by remember { mutableStateOf(ruang?.penanggungJawab ?: "") }

    val categories = listOf("Ruang Kelas", "Laboratorium", "Ruang Kerja", "Fasilitas & Area", "Fasilitas Umum", "Lainnya")
    var expandedKat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ruang == null) "Tambah Ruang Baru" else "Edit Data Ruang", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = kode,
                    onValueChange = { kode = it },
                    label = { Text("Kode Ruang (Contoh: R.01)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Ruang (Contoh: Lab Komputer Utama)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedKat,
                    onExpandedChange = { expandedKat = !expandedKat }
                ) {
                    OutlinedTextField(
                        value = kategori,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori / Tipe Ruang") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKat) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedKat,
                        onDismissRequest = { expandedKat = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    kategori = c
                                    expandedKat = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = penanggungJawab,
                    onValueChange = { penanggungJawab = it },
                    label = { Text("Penanggung Jawab (Contoh: Bu Siti)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank()) {
                        onSave(
                            if (kode.isBlank()) "R.${(10..99).random()}" else kode,
                            nama,
                            kategori,
                            penanggungJawab.ifBlank { "-" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                enabled = nama.isNotBlank()
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun StatusPenangananFormDialog(
    status: StatusPenanganan?,
    onDismiss: () -> Unit,
    onSave: (nama: String, deskripsi: String) -> Unit
) {
    var namaStatus by remember { mutableStateOf(status?.namaStatus ?: "") }
    var deskripsi by remember { mutableStateOf(status?.deskripsi ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (status == null) "Tambah Status Penanganan" else "Edit Status Penanganan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = namaStatus,
                    onValueChange = { namaStatus = it },
                    label = { Text("Nama Status Penanganan") },
                    placeholder = { Text("Contoh: Dalam Perbaikan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi Status") },
                    placeholder = { Text("Contoh: Pekerjaan sedang ditangani tim teknis") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namaStatus.isNotBlank()) {
                        onSave(namaStatus, deskripsi)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark),
                enabled = namaStatus.isNotBlank()
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun UrgensiFormDialog(
    urgensi: UrgensiMaster?,
    onDismiss: () -> Unit,
    onSave: (nama: String, deskripsi: String) -> Unit
) {
    var namaUrgensi by remember { mutableStateOf(urgensi?.namaUrgensi ?: "") }
    var deskripsi by remember { mutableStateOf(urgensi?.deskripsi ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (urgensi == null) "Tambah Opsi Urgensi" else "Edit Opsi Urgensi", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = namaUrgensi,
                    onValueChange = { namaUrgensi = it },
                    label = { Text("Nama Urgensi") },
                    placeholder = { Text("Contoh: Sangat Darurat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi Opsi") },
                    placeholder = { Text("Contoh: Membutuhkan penanganan instan < 1 jam") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namaUrgensi.isNotBlank()) {
                        onSave(namaUrgensi, deskripsi)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark),
                enabled = namaUrgensi.isNotBlank()
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ==========================================
// 7. TAB KONDISI
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KondisiTabContent(
    kondisiList: List<KondisiMaster>,
    viewModel: SarprasViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingKondisi by remember { mutableStateOf<KondisiMaster?>(null) }
    var deletingKondisi by remember { mutableStateOf<KondisiMaster?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredList = remember(searchQuery, kondisiList) {
        if (searchQuery.isBlank()) {
            kondisiList
        } else {
            kondisiList.filter {
                it.namaKondisi.contains(searchQuery, ignoreCase = true) ||
                it.deskripsi.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari status kondisi...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueContainer, contentColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset 4 Option Default", fontSize = 11.sp)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Belum ada data Kondisi" else "Kondisi tidak ditemukan",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    KondisiCardItem(
                        kondisi = item,
                        onEdit = { editingKondisi = item },
                        onDelete = { deletingKondisi = item }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PastelSkyBlueDark,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Kondisi", modifier = Modifier.size(24.dp))
        }
    }

    // Form Dialog Kondisi
    if (showAddDialog || editingKondisi != null) {
        KondisiFormDialog(
            kondisi = editingKondisi,
            onDismiss = {
                showAddDialog = false
                editingKondisi = null
            },
            onSave = { nama, desk ->
                if (editingKondisi != null) {
                    viewModel.updateKondisi(
                        editingKondisi!!.copy(
                            namaKondisi = nama,
                            deskripsi = desk
                        )
                    )
                } else {
                    viewModel.addKondisi(
                        namaKondisi = nama,
                        deskripsi = desk
                    )
                }
                showAddDialog = false
                editingKondisi = null
            }
        )
    }

    // Delete Confirmation
    if (deletingKondisi != null) {
        AlertDialog(
            onDismissRequest = { deletingKondisi = null },
            title = { Text("Hapus Opsi Kondisi?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus opsi kondisi '${deletingKondisi?.namaKondisi}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingKondisi?.let { viewModel.deleteKondisi(it) }
                        deletingKondisi = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PastelPeachDark)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingKondisi = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Reset Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Ke Opsi Default?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Fitur ini akan mengembalikan 4 pilihan default kondisi:\n1. Baik\n2. Rusak Sedang\n3. Rusak\n4. Perawatan")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDefaultKondisi()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark)
                ) {
                    Text("Reset Default", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun KondisiCardItem(
    kondisi: KondisiMaster,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, bgCircle, iconTint) = when {
        kondisi.namaKondisi.equals("Baik", ignoreCase = true) -> Triple(Icons.Default.CheckCircle, PastelMintLight, PastelMintDark)
        kondisi.namaKondisi.contains("Sedang", ignoreCase = true) -> Triple(Icons.Default.Warning, PastelButterYellow, PastelButterYellowDark)
        kondisi.namaKondisi.equals("Rusak", ignoreCase = true) || kondisi.namaKondisi.contains("Berat", ignoreCase = true) -> Triple(Icons.Default.ReportProblem, PastelPeach.copy(alpha = 0.4f), PastelPeachDark)
        kondisi.namaKondisi.contains("Perawatan", ignoreCase = true) || kondisi.namaKondisi.contains("Servis", ignoreCase = true) -> Triple(Icons.Default.Build, PastelSkyBlueContainer, PastelSkyBlueDark)
        else -> Triple(Icons.Default.Info, PastelSkyBlueContainer, PastelSkyBlueDark)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgCircle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = kondisi.namaKondisi,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kondisi.namaKondisi,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                if (kondisi.deskripsi.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = kondisi.deskripsi,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = PastelSkyBlueDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = PastelPeachDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KondisiFormDialog(
    kondisi: KondisiMaster?,
    onDismiss: () -> Unit,
    onSave: (nama: String, deskripsi: String) -> Unit
) {
    var namaKondisi by remember { mutableStateOf(kondisi?.namaKondisi ?: "") }
    var deskripsi by remember { mutableStateOf(kondisi?.deskripsi ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (kondisi == null) "Tambah Opsi Kondisi" else "Edit Opsi Kondisi", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = namaKondisi,
                    onValueChange = { namaKondisi = it },
                    label = { Text("Nama Kondisi") },
                    placeholder = { Text("Contoh: Perawatan Berkala") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi Kondisi") },
                    placeholder = { Text("Contoh: Kondisi barang sedang dalam masa servis berkala") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namaKondisi.isNotBlank()) {
                        onSave(namaKondisi, deskripsi)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                enabled = namaKondisi.isNotBlank()
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
