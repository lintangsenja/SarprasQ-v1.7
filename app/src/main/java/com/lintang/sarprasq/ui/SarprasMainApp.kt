package com.lintang.sarprasq.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Storage
import com.lintang.sarprasq.ui.screens.MasterDataScreen
import com.lintang.sarprasq.ui.screens.ReportExporterScreen
import com.lintang.sarprasq.ui.screens.TodoProjectScreen
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.R
import com.lintang.sarprasq.ui.components.AddActionPlanDialog
import com.lintang.sarprasq.ui.components.AddHelpdeskDialog
import com.lintang.sarprasq.ui.components.AddPeminjamanDialog
import com.lintang.sarprasq.ui.components.AddSuratDialog
import com.lintang.sarprasq.ui.screens.ActionPlanScreen
import com.lintang.sarprasq.ui.screens.AdministrasiScreen
import com.lintang.sarprasq.ui.screens.BackupRestoreScreen
import com.lintang.sarprasq.ui.screens.DashboardScreen
import com.lintang.sarprasq.ui.screens.HelpdeskScreen
import com.lintang.sarprasq.ui.screens.PendingControlScreen
import com.lintang.sarprasq.ui.screens.ProfileScreen
import com.lintang.sarprasq.ui.screens.RoomDamageScreen
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SarprasMainApp(viewModel: SarprasViewModel) {
    var selectedScreen by remember { mutableIntStateOf(0) } // 0..4 = main tabs, 5 = Backup, 6 = Profile
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (selectedScreen != 0) {
            selectedScreen = 0
        } else {
            showExitConfirmDialog = true
        }
    }

    val namaPetugas by viewModel.namaPetugas.collectAsState()
    val nipPetugas by viewModel.nipPetugas.collectAsState()
    val namaSekolah by viewModel.namaSekolah.collectAsState()
    val namaProgram by viewModel.namaProgram.collectAsState()
    val profileImagePath by viewModel.profileImagePath.collectAsState()

    val masterRuangList by viewModel.allRuang.collectAsState()
    val masterUrgensiList by viewModel.allUrgensi.collectAsState()
    val masterStatusList by viewModel.allStatusPenanganan.collectAsState()
    val masterKategoriList by viewModel.allKategori.collectAsState()
    val masterSubKategoriList by viewModel.allSubKategori.collectAsState()
    val masterSatuanList by viewModel.allSatuan.collectAsState()

    val ruangOptions = remember(masterRuangList) { masterRuangList.map { it.namaRuang } }
    val urgensiOptions = remember(masterUrgensiList) { masterUrgensiList.map { it.namaUrgensi } }
    val statusOptions = remember(masterStatusList) { masterStatusList.map { it.namaStatus } }
    val kategoriOptions = remember(masterKategoriList) { masterKategoriList.map { it.namaKategori } }

    var showAddHelpdeskDialog by remember { mutableStateOf(false) }
    var showAddActionPlanDialog by remember { mutableStateOf(false) }
    var showAddSuratDialog by remember { mutableStateOf(false) }
    var showAddPeminjamanDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PastelBackground,
                modifier = Modifier.width(290.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // --- COMPACT UNIFIED HEADER ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PastelSurface)
                            .border(1.dp, PastelCardBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.lintang.sarprasq.ui.components.ProfileAvatar(
                                    imagePath = profileImagePath,
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    iconSize = 20.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = namaProgram,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = namaSekolah,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = PastelCardBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PastelSkyBlueContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Petugas: $namaPetugas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelSkyBlueDark,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NIP: $nipPetugas",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- SECTION 1: MENU ARSIP & LAYANAN ---
                    Text(
                        text = "MENU PELENGKAP & ARSIP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        label = { Text("Arsip Serbaguna", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedScreen == 4,
                        onClick = {
                            selectedScreen = 4
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PastelSkyBlueContainer,
                            selectedIconColor = PastelSkyBlueDark,
                            selectedTextColor = PastelSkyBlueDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        label = { Text("Kerusakan Per Ruang", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedScreen == 9,
                        onClick = {
                            selectedScreen = 9
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PastelSkyBlueContainer,
                            selectedIconColor = PastelSkyBlueDark,
                            selectedTextColor = PastelSkyBlueDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Rule, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        label = { Text("Laporan & Ekspor Dokumen", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedScreen == 11,
                        onClick = {
                            selectedScreen = 11
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PastelSkyBlueContainer,
                            selectedIconColor = PastelSkyBlueDark,
                            selectedTextColor = PastelSkyBlueDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- SECTION 2: ADMINISTRASI & MASTER DATA ---
                    Text(
                        text = "ADMINISTRASI & MASTER DATA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        label = { Text("Master Data Sarpras", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedScreen == 7 || selectedScreen == 8,
                        onClick = {
                            selectedScreen = 7
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PastelSkyBlueContainer,
                            selectedIconColor = PastelSkyBlueDark,
                            selectedTextColor = PastelSkyBlueDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        label = { Text("Backup & Restore", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedScreen == 5,
                        onClick = {
                            selectedScreen = 5
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PastelSkyBlueContainer,
                            selectedIconColor = PastelSkyBlueDark,
                            selectedTextColor = PastelSkyBlueDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        label = { Text("Profil Petugas", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedScreen == 6,
                        onClick = {
                            selectedScreen = 6
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = PastelSkyBlueContainer,
                            selectedIconColor = PastelSkyBlueDark,
                            selectedTextColor = PastelSkyBlueDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = PastelCardBorder)
                    Spacer(modifier = Modifier.height(6.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp), tint = PastelPeachDark) },
                        label = { Text("Keluar Aplikasi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PastelPeachDark) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showExitConfirmDialog = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = PastelPeach.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Navigasi Drawer",
                                tint = TextPrimary
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (selectedScreen) {
                                    0 -> "$namaProgram - Ringkasan"
                                    1 -> "Helpdesk Laporan"
                                    2 -> "Kontrol Pending"
                                    3 -> "Action Plan"
                                    4 -> "Arsip Serbaguna"
                                    5 -> "Backup & Restore"
                                    6 -> "Profil Petugas"
                                    7, 8 -> "Master Data"
                                    9 -> "Kerusakan Per Ruang"
                                    10 -> "To-Do & Perencana Proyek"
                                    11 -> "Laporan & Ekspor Dokumen"
                                    else -> namaProgram
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PastelMintLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = namaSekolah,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelMintDark,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { selectedScreen = 6 },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            com.lintang.sarprasq.ui.components.ProfileAvatar(
                                imagePath = profileImagePath,
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                iconSize = 18.dp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PastelBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(55.dp)
                ) {
                    NavigationBarItem(
                        selected = selectedScreen == 0,
                        onClick = { selectedScreen = 0 },
                        icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Ringkasan", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PastelSkyBlueDark,
                            selectedTextColor = PastelSkyBlueDark,
                            indicatorColor = PastelSkyBlue.copy(alpha = 0.4f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 1,
                        onClick = { selectedScreen = 1 },
                        icon = { Icon(imageVector = Icons.Default.Build, contentDescription = "Helpdesk") },
                        label = { Text("Helpdesk", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PastelSkyBlueDark,
                            selectedTextColor = PastelSkyBlueDark,
                            indicatorColor = PastelSkyBlue.copy(alpha = 0.4f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 2,
                        onClick = { selectedScreen = 2 },
                        icon = { Icon(imageVector = Icons.Default.HourglassTop, contentDescription = "Pending") },
                        label = { Text("Pending", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PastelButterYellowDark,
                            selectedTextColor = PastelButterYellowDark,
                            indicatorColor = PastelSkyBlue.copy(alpha = 0.4f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 3,
                        onClick = { selectedScreen = 3 },
                        icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Action Plan") },
                        label = { Text("Rencana", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PastelMintDark,
                            selectedTextColor = PastelMintDark,
                            indicatorColor = PastelSkyBlue.copy(alpha = 0.4f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 10,
                        onClick = { selectedScreen = 10 },
                        icon = { Icon(imageVector = Icons.Default.TaskAlt, contentDescription = "To-Do & Proyek") },
                        label = { Text("To-Do", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PastelLavenderDark,
                            selectedTextColor = PastelLavenderDark,
                            indicatorColor = PastelSkyBlue.copy(alpha = 0.4f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            },
            containerColor = PastelBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedScreen) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateTab = { selectedScreen = it },
                        onOpenAddHelpdesk = { showAddHelpdeskDialog = true },
                        onOpenAddActionPlan = { showAddActionPlanDialog = true },
                        onOpenAddSurat = { showAddSuratDialog = true }
                    )
                    1 -> HelpdeskScreen(
                        viewModel = viewModel,
                        onOpenAddHelpdesk = { showAddHelpdeskDialog = true }
                    )
                    2 -> PendingControlScreen(
                        viewModel = viewModel
                    )
                    3 -> ActionPlanScreen(
                        viewModel = viewModel,
                        onOpenAddActionPlan = { showAddActionPlanDialog = true }
                    )
                    4 -> AdministrasiScreen(
                        viewModel = viewModel,
                        onOpenAddSurat = { showAddSuratDialog = true },
                        onOpenAddPeminjaman = { showAddPeminjamanDialog = true }
                    )
                    5 -> BackupRestoreScreen(
                        viewModel = viewModel
                    )
                    6 -> ProfileScreen(
                        viewModel = viewModel
                    )
                    7 -> MasterDataScreen(
                        viewModel = viewModel,
                        initialTab = 0
                    )
                    8 -> MasterDataScreen(
                        viewModel = viewModel,
                        initialTab = 1
                    )
                    9 -> RoomDamageScreen(
                        viewModel = viewModel
                    )
                    10 -> TodoProjectScreen(
                        viewModel = viewModel
                    )
                    11 -> ReportExporterScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // --- DIALOGS ---
    if (showAddHelpdeskDialog) {
        AddHelpdeskDialog(
            ruangList = ruangOptions,
            urgensiList = urgensiOptions,
            statusList = statusOptions,
            kategoriList = kategoriOptions,
            onAddKategoriToMaster = { viewModel.addKategori(it) },
            onDismiss = { showAddHelpdeskDialog = false },
            onSubmit = { pelapor, lokasi, deskripsi, urgensi, status, tindakan, alasanPending, estimasiEksekusi, tanggal, kategori, fotoUrl, catatan, petugas ->
                viewModel.addHelpdeskReport(
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
                showAddHelpdeskDialog = false
            }
        )
    }

    if (showAddActionPlanDialog) {
        AddActionPlanDialog(
            masterSubKategoriList = masterSubKategoriList,
            masterRuangList = masterRuangList,
            masterSatuanList = masterSatuanList,
            onDismiss = { showAddActionPlanDialog = false },
            onSubmit = { agenda, jenisRencana, kategori, targetWaktu, statusProgres, tanggalMulai, tanggalRealisasi, pelaksana, namaTukang, nomorWhatsapp, detailAset, lokasiRuang, tingkatKerusakan, spesifikasi, jumlahSatuan, estimasiAnggaran, fotoUrl ->
                viewModel.addActionPlan(
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
                showAddActionPlanDialog = false
            }
        )
    }

    if (showAddSuratDialog) {
        AddSuratDialog(
            onDismiss = { showAddSuratDialog = false },
            onSubmit = { nomorSurat, tanggalSurat, perihal, deskripsiSurat, jenisSurat, statusArsip ->
                viewModel.addSuratArsip(
                    nomorSurat = nomorSurat,
                    tanggalSurat = tanggalSurat,
                    perihal = perihal,
                    deskripsiSurat = deskripsiSurat,
                    jenisSurat = jenisSurat,
                    statusArsip = statusArsip
                )
                showAddSuratDialog = false
            }
        )
    }

    if (showAddPeminjamanDialog) {
        AddPeminjamanDialog(
            onDismiss = { showAddPeminjamanDialog = false },
            onSubmitBatch = { peminjamanList ->
                viewModel.insertPeminjamanMakroList(peminjamanList) {
                    Toast.makeText(
                        context,
                        "✓ Berhasil menyimpan ${peminjamanList.size} transaksi peminjaman barang!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                showAddPeminjamanDialog = false
            }
        )
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PastelPeach),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🥺", fontSize = 26.sp)
                }
            },
            title = {
                Text(
                    text = "Keluar dari SarprasQ?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Yakin mau meninggalkan aplikasi? Kami akan merindukanmu! 🥺✨\n\nSemua data Anda di $namaSekolah tetap tersimpan secara aman.",
                    fontSize = 13.5.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        Toast.makeText(context, "Sampai jumpa, $namaPetugas! 👋✨", Toast.LENGTH_SHORT).show()
                        (context as? Activity)?.finishAffinity()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeachDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya, Keluar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal / Stay", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}
