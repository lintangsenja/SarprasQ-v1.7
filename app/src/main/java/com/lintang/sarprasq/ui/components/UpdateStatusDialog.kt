package com.lintang.sarprasq.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateStatusDialog(
    report: HelpdeskReport,
    customStatusOptions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (
        newStatus: String,
        newTindakan: String,
        newAlasanPending: String?,
        newEstimasi: String?
    ) -> Unit
) {
    var status by remember { mutableStateOf(report.status) }
    var tindakan by remember { mutableStateOf(report.tindakan) }
    var alasanPending by remember { mutableStateOf(report.alasanPending ?: "") }
    var estimasiEksekusi by remember { mutableStateOf(report.estimasiEksekusi ?: "") }

    var expandedStatus by remember { mutableStateOf(false) }
    val defaultStatusOptions = listOf("Catat", "Laporan Masuk", "Pending", "Proses", "Segera", "Selesai", "Ditolak")
    val statusOptions = remember(customStatusOptions) {
        val base = if (customStatusOptions.isNotEmpty()) customStatusOptions else defaultStatusOptions
        val list = base.toMutableList()
        if (!list.contains("Catat")) {
            list.add(0, "Catat")
        }
        list.distinct()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Update Status Laporan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "${report.lokasi} - ${report.deskripsi}",
                    fontSize = 13.sp,
                    color = TextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Baru") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tindakan,
                    onValueChange = { tindakan = it },
                    label = { Text("Tindakan / Update Progress") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (status.equals("Pending", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = alasanPending,
                        onValueChange = { alasanPending = it },
                        label = { Text("Alasan Tertunda (Pending Reason)") },
                        placeholder = { Text("Mengapa pekerjaan ini tertunda?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DatePickerField(
                        value = estimasiEksekusi,
                        onDateSelected = { estimasiEksekusi = it },
                        label = "Estimasi Tanggal Eksekusi",
                        placeholder = "DD/MM/YYYY"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                                status,
                                tindakan,
                                if (status == "Pending") alasanPending else null,
                                if (status == "Pending") estimasiEksekusi else null
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Status")
                    }
                }
            }
        }
    }
}
