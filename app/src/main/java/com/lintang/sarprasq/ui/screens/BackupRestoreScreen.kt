package com.lintang.sarprasq.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.zip.ZipInputStream
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import com.lintang.sarprasq.ui.components.DatePickerField
import com.lintang.sarprasq.ui.theme.TextMuted
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellow
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.BackupHistoryRecord
import com.lintang.sarprasq.ui.viewmodel.FirebaseConnState
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel
import java.io.File

@Composable
fun BackupRestoreScreen(
    viewModel: SarprasViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backupHistory by viewModel.backupHistoryRecords.collectAsState()
    val firebaseConnStatus by viewModel.firebaseConnStatus.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    var latestJsonString by remember { mutableStateOf<String?>(null) }
    var previewJsonString by remember { mutableStateOf<String?>(null) }
    var latestFile by remember { mutableStateOf<File?>(null) }

    var showRestoreTextDialog by remember { mutableStateOf(false) }
    var restoreInputJson by remember { mutableStateOf("") }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var jsonToRestore by remember { mutableStateOf("") }
    var selectedRestoreFileName by remember { mutableStateOf<String?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<BackupHistoryRecord?>(null) }
    var filterDate by remember { mutableStateOf("") }

    // Automatic diagnostic check on screen enter
    LaunchedEffect(Unit) {
        if (firebaseConnStatus is FirebaseConnState.Idle) {
            viewModel.testFirebaseConnection()
        }
    }

    // SAF Create Document Launcher for Export / Backup (Internal & External Storage)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { targetUri ->
            viewModel.exportBackupToUri(
                context = context,
                targetUri = targetUri,
                onSuccess = { _, count ->
                    Toast.makeText(
                        context,
                        "✓ Cadangan data berhasil disimpan ke folder pilihan Anda ($count data)!",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onError = { err ->
                    Toast.makeText(
                        context,
                        "Gagal menyimpan berkas cadangan: $err",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    // SAF Open Document Launcher for Import / Pemulihan Fleksibel Cari File
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            val result = readBackupFileContent(context, fileUri)
            if (result != null && result.second.isNotBlank()) {
                selectedRestoreFileName = result.first
                jsonToRestore = result.second
                showConfirmRestoreDialog = true
            } else {
                Toast.makeText(
                    context,
                    "Format file cadangan tidak valid atau berkas kosong!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PastelBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER BANNER RINGKAS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSkyBlue.copy(alpha = 0.35f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, PastelSkyBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Backup Restore Logo",
                        tint = PastelSkyBlueDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Backup & Restore Data",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Amankan dan pulihkan seluruh data operasional SarprasQ secara mandiri",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // =========================================================================
        // KARTU 1: OPERASIONAL & DIAGNOSTIK CLOUD FIREBASE
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Kartu Firebase Cloud
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PastelSkyBlue.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Sync Icon",
                            tint = PastelSkyBlueDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Operasional & Diagnostik Cloud Firebase",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sync manual dan uji koneksi real-time ke Firebase Console",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Sub-Seksi 1: Connection Diagnostic
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Uji Koneksi Firebase Console",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Connection Diagnostic (Real-time Inspector/Sekretaris)",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Tombol Cek Koneksi
                        OutlinedButton(
                            onClick = { viewModel.testFirebaseConnection() },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PastelSkyBlueDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Cek Koneksi",
                                tint = PastelSkyBlueDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cek Koneksi", fontSize = 11.sp, color = PastelSkyBlueDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    // TAMPILAN STATUS VISUAL KONEKSI
                    when (val status = firebaseConnStatus) {
                        is FirebaseConnState.Testing -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = PastelSkyBlueDark,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Memeriksa koneksi ke Firebase Console...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        is FirebaseConnState.Success -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Terhubung",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = status.message,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    }

                                    // Detail Diagnostik
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.85f))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Endpoint Cloud:", fontSize = 10.sp, color = TextSecondary)
                                            Text(status.endpoint, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Kecepatan Respon:", fontSize = 10.sp, color = TextSecondary)
                                            Text("${status.latencyMs} ms (Sangat Baik)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Status Proyek:", fontSize = 10.sp, color = TextSecondary)
                                            Text("SarprasQ Realtime Database Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        if (status.timestamp.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Waktu Diagnostik:", fontSize = 10.sp, color = TextSecondary)
                                                Text(status.timestamp, fontSize = 10.sp, color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        is FirebaseConnState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = "Gagal",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = status.message,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B)
                                        )
                                    }

                                    // Detail Error Box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Detail Kesalahan Diagnostik:",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB91C1C)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = status.errorDetail,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF450A0A)
                                            )

                                            if (status.errorDetail.contains("TIMEOUT", ignoreCase = true)) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFFFFBEB))
                                                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                                                        .padding(8.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text(
                                                            text = "💡 Panduan Solusi Timeout Koneksi:",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF92400E)
                                                        )
                                                        Text(
                                                            text = "Pastikan jaringan internet aktif dan URL Firebase Realtime Database (https://sarpras-134d7-default-rtdb.asia-southeast1.firebasedatabase.app) dapat diakses.",
                                                            fontSize = 9.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = Color(0xFF78350F)
                                                        )
                                                    }
                                                }
                                            }

                                            if (status.errorDetail.contains("SECURITY RULES", ignoreCase = true) || status.errorDetail.contains("Permission denied", ignoreCase = true)) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFFFFBEB))
                                                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                                                        .padding(8.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text(
                                                            text = "💡 Panduan Solusi Security Rules Firebase:",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF92400E)
                                                        )
                                                        Text(
                                                            text = "Aturan keamanan Firebase memblokir koneksi real-time. Buka Firebase Console -> Realtime Database -> Rules, ubah aturan menjadi:\n{\n  \"rules\": {\n    \"sarprasq\": {\n      \".read\": true,\n      \".write\": true\n    }\n  }\n}",
                                                            fontSize = 9.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = Color(0xFF78350F)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                // Sub-Seksi 2: Sinkronisasi Cloud & Waktu Sinkron Terakhir
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sinkronisasi & Pemulihan Cloud (Dua Arah)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Proses ini menarik seluruh koleksi data dari Firebase Cloud (To-Do, Proyek, Rehab, Helpdesk, Profil) ke perangkat lokal (Pull/Restore) serta mengunggah perubahan lokal ke server secara otomatis.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Terakhir: $lastSyncTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.manualSyncToFirestore { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyinkronkan Data...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sinkronkan Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // KARTU 2: MANAJEMEN DATA LOKAL (BACKUP & IMPORT)
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Kartu Lokal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PastelMint.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "Manajemen Data Lokal Icon",
                            tint = PastelMintDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Manajemen Data Lokal (SAF)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Bebas pilih folder penyimpanan (Internal/Eksternal) & cari file pemulihan fleksibel",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PastelMint.copy(alpha = 0.25f))
                        .border(1.dp, PastelMintDark.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "💡 Bebas memilih folder tujuan penyimpanan (Memori Internal/Kartu SD) saat Backup, dan dapat menelusuri file cadangan (.json/.zip) secara fleksibel dari direktori manapun di perangkat Anda.",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }

                // --- TOMBOL AKSI UTAMA (SATU BARIS: BACKUP & IMPORT) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tombol Backup
                    Button(
                        onClick = {
                            val timeStampStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                            val defaultFileName = "SarprasQ_Backup_$timeStampStr.json"
                            createDocumentLauncher.launch(defaultFileName)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup (Pilih Folder)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Tombol Import
                    Button(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf("application/json", "text/plain", "application/zip", "application/x-zip-compressed", "*/*")
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import (Cari File)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- MENU RIWAYAT BACKUP ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Riwayat Icon",
                        tint = PastelSkyBlueDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Riwayat Backup",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelSkyBlue.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${backupHistory.size} Catatan",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSkyBlueDark
                        )
                    }
                }

                // Filter Penanggalan Riwayat Backup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        value = filterDate,
                        onDateSelected = { filterDate = it },
                        label = "Filter Tanggal Riwayat",
                        placeholder = "Pilih tanggal...",
                        modifier = Modifier.weight(1f)
                    )

                    if (filterDate.isNotBlank()) {
                        OutlinedButton(
                            onClick = { filterDate = "" },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted)
                        ) {
                            Text("Reset", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                val filteredHistory = remember(backupHistory, filterDate) {
                    if (filterDate.isBlank()) backupHistory
                    else backupHistory.filter { it.timestamp.contains(filterDate) }
                }

                if (filteredHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.02f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterDate.isNotBlank()) "Tidak ada riwayat backup pada tanggal $filterDate." else "Belum ada riwayat backup tercatat.\nTekan tombol 'Backup' di atas untuk membuat cadangan baru.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredHistory.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(PastelMint.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SaveAlt,
                                            contentDescription = null,
                                            tint = PastelMintDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.timestamp,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${item.totalRecords} Catatan Data (.json)",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    // Tombol Aksi Langsung di Sebelah Kanan (Pratinjau, Bagikan & Hapus)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                previewJsonString = item.jsonString
                                                showPreviewDialog = true
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Pratinjau",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                shareJsonData(context, item.jsonString)
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Bagikan",
                                                tint = PastelSkyBlueDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                itemToDelete = item
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
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
    }

    // --- DIALOG: PREVIEW & COPY JSON ---
    if (showPreviewDialog && previewJsonString != null) {
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = {
                Text("Pratinjau Teks Cadangan Data", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Berikut adalah salinan teks dari data cadangan aplikasi:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = previewJsonString!!,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF334155)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SarprasQ Backup", previewJsonString)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Teks cadangan berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        showPreviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark)
                ) {
                    Text("Salin Ke Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreviewDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // --- DIALOG: PASTE JSON MANUAL RESTORE ---
    if (showRestoreTextDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreTextDialog = false },
            title = {
                Text("Tempel Data Cadangan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Tempelkan isi teks data cadangan SarprasQ di bawah ini:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreInputJson,
                        onValueChange = { restoreInputJson = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("{ \"app\": \"SarprasQ\", ... }", fontSize = 11.sp) },
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreInputJson.isNotBlank()) {
                            jsonToRestore = restoreInputJson
                            showRestoreTextDialog = false
                            showConfirmRestoreDialog = true
                        } else {
                            Toast.makeText(context, "Mohon masukkan teks data cadangan", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderDark)
                ) {
                    Text("Lanjutkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreTextDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // --- DIALOG: CONFIRM RESTORE ---
    if (showConfirmRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDialog = false },
            title = {
                Text("Konfirmasi Restore Data", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!selectedRestoreFileName.isNullOrBlank()) {
                        Text(
                            text = "Berkas Cadangan: $selectedRestoreFileName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelLavenderDark
                        )
                    }
                    Text(
                        text = "PERHATIAN: Memulihkan data dari cadangan akan memperbarui dan mengganti data lokal di database SarprasQ. Apakah Anda yakin ingin melanjutkan?",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRestoreDialog = false
                        viewModel.restoreBackupJson(
                            jsonContent = jsonToRestore,
                            onSuccess = { count ->
                                Toast.makeText(
                                    context,
                                    "✓ Berhasil memulihkan $count item data SarprasQ dari ${selectedRestoreFileName ?: "file cadangan"}!",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onError = { err ->
                                Toast.makeText(
                                    context,
                                    "Gagal restore: $err",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelLavenderDark)
                ) {
                    Text("Ya, Restore Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // --- DIALOG: DELETE HISTORY ITEM ---
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Catatan Riwayat Backup", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus catatan riwayat backup tanggal ${itemToDelete?.timestamp} ini?", fontSize = 13.sp, color = TextPrimary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteBackupHistoryRecord(it.id) }
                        itemToDelete = null
                        Toast.makeText(context, "Riwayat backup berhasil dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

private fun shareJsonData(context: Context, jsonStr: String) {
    try {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, jsonStr)
            putExtra(Intent.EXTRA_SUBJECT, "SarprasQ Data Backup")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Data Backup SarprasQ")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membagikan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun readBackupFileContent(context: Context, uri: Uri): Pair<String, String>? {
    var fileName = "cadangan_sarprasq.json"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val fetchedName = cursor.getString(nameIndex)
                if (!fetchedName.isNullOrBlank()) {
                    fileName = fetchedName
                }
            }
        }
    } catch (e: Exception) {
        // Fallback name
    }

    try {
        if (fileName.endsWith(".zip", ignoreCase = true)) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && (entry.name.endsWith(".json", ignoreCase = true) || entry.name.endsWith(".txt", ignoreCase = true))) {
                            val textContent = zipStream.bufferedReader(Charsets.UTF_8).readText()
                            if (textContent.isNotBlank()) {
                                return Pair(entry.name, textContent)
                            }
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }
        }

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val content = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            if (content.isNotBlank()) {
                return Pair(fileName, content)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
