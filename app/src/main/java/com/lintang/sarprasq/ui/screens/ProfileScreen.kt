package com.lintang.sarprasq.ui.screens

import android.app.Activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.ui.components.ManualImageCropDialog
import com.lintang.sarprasq.ui.components.ProfileAvatar
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelMintLight
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    viewModel: SarprasViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val namaPetugas by viewModel.namaPetugas.collectAsState()
    val nipPetugas by viewModel.nipPetugas.collectAsState()
    val namaSekolah by viewModel.namaSekolah.collectAsState()
    val namaProgram by viewModel.namaProgram.collectAsState()
    val profileImagePath by viewModel.profileImagePath.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var selectedBitmapForCrop by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher gallery picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    selectedBitmapForCrop = bitmap
                } else {
                    Toast.makeText(context, "Gagal membaca berkas gambar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PastelBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- AVATAR & HEADER CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Profil / Logo Instansi (Re-usable ProfileAvatar)
                ProfileAvatar(
                    imagePath = profileImagePath,
                    modifier = Modifier.size(108.dp),
                    shape = RoundedCornerShape(26.dp),
                    iconSize = 48.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tombol Ganti / Unggah Foto
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelSkyBlueDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Unggah Foto",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (profileImagePath.isNullOrBlank()) "Unggah Foto / Logo" else "Ganti Foto / Logo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!profileImagePath.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateProfileImage(null)
                                Toast.makeText(context, "Foto profil berhasil dihapus", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelPeachDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Foto",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nama Petugas
                Text(
                    text = namaPetugas,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Jabatan / NIP Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PastelMintLight)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NIP: $nipPetugas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelMintDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showEditProfileDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelSkyBlueDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Informasi Profil & Program", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- INFORMATION DETAILS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PastelSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PastelCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Informasi Sistem & Instansi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                ProfileDetailItem(
                    icon = Icons.Default.VerifiedUser,
                    iconTint = PastelSkyBlueDark,
                    label = "Nama Program / Sistem",
                    value = namaProgram
                )

                Divider(color = PastelCardBorder.copy(alpha = 0.5f))

                ProfileDetailItem(
                    icon = Icons.Default.School,
                    iconTint = PastelMintDark,
                    label = "Nama Sekolah / Instansi",
                    value = namaSekolah
                )

                Divider(color = PastelCardBorder.copy(alpha = 0.5f))

                ProfileDetailItem(
                    icon = Icons.Default.Person,
                    iconTint = PastelSkyBlueDark,
                    label = "Nama Petugas Sarpras",
                    value = namaPetugas
                )

                Divider(color = PastelCardBorder.copy(alpha = 0.5f))

                ProfileDetailItem(
                    icon = Icons.Default.Badge,
                    iconTint = PastelMintDark,
                    label = "NIP Petugas",
                    value = nipPetugas
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- TOMBOL KELUAR (LOGOUT) ---
        Button(
            onClick = { showLogoutConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PastelPeach,
                contentColor = PastelPeachDark
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Keluar App",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Keluar dari Aplikasi",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // --- MANUAL CROPPER DIALOG ---
    if (selectedBitmapForCrop != null) {
        ManualImageCropDialog(
            sourceBitmap = selectedBitmapForCrop!!,
            onCropped = { croppedBitmap ->
                try {
                    val outputFile = File(context.filesDir, "profile_photo.png")
                    val fos = FileOutputStream(outputFile)
                    croppedBitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
                    fos.flush()
                    fos.close()

                    viewModel.updateProfileImage(outputFile.absolutePath)
                    Toast.makeText(context, "Foto/Logo profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal menyimpan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    selectedBitmapForCrop = null
                }
            },
            onDismiss = { selectedBitmapForCrop = null }
        )
    }

    // --- DIALOG EDIT PROFILE & PROGRAM ---
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(namaPetugas) }
        var editNip by remember { mutableStateOf(nipPetugas) }
        var editSekolah by remember { mutableStateOf(namaSekolah) }
        var editProgram by remember { mutableStateOf(namaProgram) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("Edit Profil & Nama Program", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editProgram,
                        onValueChange = { editProgram = it },
                        label = { Text("Nama Program / Sistem") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Petugas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editNip,
                        onValueChange = { editNip = it },
                        label = { Text("NIP Petugas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editSekolah,
                        onValueChange = { editSekolah = it },
                        label = { Text("Nama Sekolah / Instansi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank() && editNip.isNotBlank() && editProgram.isNotBlank()) {
                            viewModel.updateProfileInfo(editName, editNip, editSekolah, editProgram)
                            Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            showEditProfileDialog = false
                        } else {
                            Toast.makeText(context, "Nama, NIP, dan Nama Program tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // --- DIALOG CONFIRM LOGOUT ---
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
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
                    text = "Yakin mau meninggalkan aplikasi? Kami akan merindukanmu! 🥺✨\n\nSemua data sarpras dan proyek Anda di $namaSekolah tetap tersimpan secara aman.",
                    fontSize = 13.5.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
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
                    onClick = { showLogoutConfirmDialog = false },
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

@Composable
private fun ProfileDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

