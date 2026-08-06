package com.lintang.sarprasq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary

import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

@Composable
fun ActionPlanDetailDialog(
    plan: ActionPlan,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onQuickProgress: (newStatus: String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detail Rencana Kerja Proaktif",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelLavenderDark
                        )
                        Text(
                            text = plan.agenda,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JenisRencanaBadge(jenis = plan.jenisRencana)
                    ProgresBadge(progres = plan.statusProgres)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PastelLavender.copy(alpha = 0.2f))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailRow(icon = Icons.Default.Category, label = "Sub-Kategori Agenda", value = plan.kategori)
                        DetailRow(icon = Icons.Default.Engineering, label = "Pelaksana Pekerjaan", value = plan.pelaksana)

                        if (!plan.detailAset.isNullOrBlank()) {
                            val label = when (plan.jenisRencana) {
                                "Rencana Pengadaan" -> "Nama Barang Baru"
                                "Rencana Belanja Barang / Jasa" -> "Barang / Jasa Operasional"
                                else -> "Aset / Fasilitas"
                            }
                            DetailRow(icon = Icons.Default.Build, label = label, value = plan.detailAset)
                        }

                        if (!plan.lokasiRuang.isNullOrBlank()) {
                            val label = when (plan.jenisRencana) {
                                "Rencana Belanja Barang / Jasa" -> "Keperluan / Bagian"
                                else -> "Lokasi Ruangan"
                            }
                            DetailRow(icon = Icons.Default.Place, label = label, value = plan.lokasiRuang)
                        }

                        if (!plan.tingkatKerusakan.isNullOrBlank()) {
                            DetailRow(icon = Icons.Default.Warning, label = "Tingkat Kerusakan", value = plan.tingkatKerusakan)
                        }

                        if (!plan.spesifikasi.isNullOrBlank()) {
                            DetailRow(icon = Icons.Default.Description, label = "Spesifikasi Singkat", value = plan.spesifikasi)
                        }

                        if (!plan.jumlahSatuan.isNullOrBlank()) {
                            DetailRow(icon = Icons.Default.Inventory2, label = "Jumlah & Satuan", value = plan.jumlahSatuan)
                        }

                        if (!plan.estimasiAnggaran.isNullOrBlank()) {
                            val digitsOnly = plan.estimasiAnggaran.filter { it.isDigit() }
                            val displayValue = if (digitsOnly.isNotEmpty()) {
                                val parsed = digitsOnly.toLongOrNull()
                                if (parsed != null) "Rp. ${java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(parsed)}" else plan.estimasiAnggaran
                            } else plan.estimasiAnggaran
                            DetailRow(icon = Icons.Default.Payments, label = "Estimasi Anggaran", value = displayValue)
                        }

                        if (!plan.namaTukang.isNullOrBlank()) {
                            DetailRow(icon = Icons.Default.Badge, label = "Nama Tukang / Teknisi", value = plan.namaTukang)
                        }
                        if (!plan.nomorWhatsapp.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DetailRow(icon = Icons.Default.Phone, label = "Nomor WhatsApp Kontak", value = plan.nomorWhatsapp)
                                OutlinedButton(
                                    onClick = {
                                        val cleanPhone = plan.nomorWhatsapp.replace(Regex("[^0-9]"), "")
                                        val waUrl = "https://wa.me/$cleanPhone"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelMintDark),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Chat WA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (!plan.fotoUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Lampiran Foto Dokumentasi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                            ) {
                                coil.compose.AsyncImage(
                                    model = plan.fotoUrl,
                                    contentDescription = "Foto Dokumentasi",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        DetailRow(icon = Icons.Default.CalendarToday, label = "Target Waktu Finish", value = plan.targetWaktu)
                        if (plan.tanggalMulai != "-") {
                            DetailRow(icon = Icons.Default.PlayArrow, label = "Tanggal Mulai Pekerjaan", value = plan.tanggalMulai)
                        }
                        DetailRow(icon = Icons.Default.CheckCircleOutline, label = "Tanggal Realisasi Selesai", value = plan.tanggalRealisasi)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Aksi Manajemen Agenda:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (plan.statusProgres.equals("Belum Mulai", ignoreCase = true)) {
                        Button(
                            onClick = onStart,
                            colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Mulai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (plan.statusProgres.equals("Dalam Proses", ignoreCase = true)) {
                        Button(
                            onClick = { onQuickProgress("Selesai") },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}
