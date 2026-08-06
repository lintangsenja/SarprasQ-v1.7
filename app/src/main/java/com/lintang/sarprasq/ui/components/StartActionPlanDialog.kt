package com.lintang.sarprasq.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary

@Composable
fun StartActionPlanDialog(
    plan: ActionPlan,
    onDismiss: () -> Unit,
    onSubmit: (
        tanggalMulai: String,
        pelaksana: String,
        namaTukang: String?,
        nomorWhatsapp: String?
    ) -> Unit
) {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    val today = sdf.format(java.util.Date())

    var isTodaySelected by remember { mutableStateOf(true) }
    var customTanggalMulai by remember { mutableStateOf(if (plan.tanggalMulai != "-") plan.tanggalMulai else today) }

    var pelaksana by remember { mutableStateOf(if (plan.pelaksana.isNotBlank()) plan.pelaksana else "Internal") }
    var namaTukang by remember { mutableStateOf(if (!plan.namaTukang.isNullOrBlank()) plan.namaTukang else if (pelaksana == "Internal") "Kevin Ricky Utama, S.Kom." else "") }
    var nomorWhatsapp by remember { mutableStateOf(plan.nomorWhatsapp ?: "") }

    val effectiveTanggalMulai = if (isTodaySelected) today else customTanggalMulai

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
                // Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = PastelSkyBlueDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mulai Agenda Pekerjaan",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = plan.agenda,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PastelSkyBlueDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Pop-up Konfirmasi Tanggal Mulai
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PastelBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = PastelSkyBlueDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Konfirmasi Tanggal Mulai",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Option A: Today Automatically
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isTodaySelected = true }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            RadioButton(
                                selected = isTodaySelected,
                                onClick = { isTodaySelected = true },
                                colors = RadioButtonDefaults.colors(selectedColor = PastelSkyBlueDark)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "Dimulai Hari Ini (Otomatis)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tanggal: $today",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Option B: Select Date Manually
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isTodaySelected = false }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            RadioButton(
                                selected = !isTodaySelected,
                                onClick = { isTodaySelected = false },
                                colors = RadioButtonDefaults.colors(selectedColor = PastelSkyBlueDark)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pilih Tanggal Lain (Manual)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        AnimatedVisibility(visible = !isTodaySelected) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                DatePickerField(
                                    value = customTanggalMulai,
                                    onDateSelected = { customTanggalMulai = it },
                                    label = "Pilih Tanggal Mulai Pekerjaan"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Form Pilihan Pelaksana (Internal / Eksternal)
                Text(
                    text = "Pelaksana Pekerjaan:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Internal Choice
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (pelaksana == "Internal") PastelSkyBlueContainer else Color.White)
                            .border(
                                width = if (pelaksana == "Internal") 2.dp else 1.dp,
                                color = if (pelaksana == "Internal") PastelSkyBlueDark else Color.LightGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { pelaksana = "Internal" }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (pelaksana == "Internal") PastelSkyBlueDark else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Internal",
                                fontSize = 13.sp,
                                fontWeight = if (pelaksana == "Internal") FontWeight.Bold else FontWeight.Medium,
                                color = if (pelaksana == "Internal") PastelSkyBlueDark else TextPrimary
                            )
                        }
                    }

                    // Eksternal Choice
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (pelaksana == "Eksternal") PastelLavender.copy(alpha = 0.4f) else Color.White)
                            .border(
                                width = if (pelaksana == "Eksternal") 2.dp else 1.dp,
                                color = if (pelaksana == "Eksternal") PastelLavenderDark else Color.LightGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { pelaksana = "Eksternal" }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = if (pelaksana == "Eksternal") PastelLavenderDark else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Eksternal (Tukang)",
                                fontSize = 13.sp,
                                fontWeight = if (pelaksana == "Eksternal") FontWeight.Bold else FontWeight.Medium,
                                color = if (pelaksana == "Eksternal") PastelLavenderDark else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Form Input Nama Tukang & Nomor WhatsApp (Opsional)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFAFAFA))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (pelaksana == "Eksternal") "Detail Tukang / Teknisi Eksternal (Opsional)" else "Petugas / Penanggung Jawab (Opsional)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = namaTukang,
                        onValueChange = { namaTukang = it },
                        label = { Text("Nama Tukang / Teknisi") },
                        placeholder = { Text(if (pelaksana == "Eksternal") "Contoh: Pak Slamet (Tukang Bangunan)" else "Contoh: Pak Joko (Teknisi Sekolah)") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = PastelSkyBlueDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nomorWhatsapp,
                        onValueChange = { nomorWhatsapp = it },
                        label = { Text("Nomor WhatsApp / Kontak HP") },
                        placeholder = { Text("Contoh: 081234567890") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PastelMintDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            onSubmit(
                                effectiveTanggalMulai,
                                pelaksana,
                                namaTukang.ifBlank { null },
                                nomorWhatsapp.ifBlank { null }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mulai Pekerjaan")
                    }
                }
            }
        }
    }
}
