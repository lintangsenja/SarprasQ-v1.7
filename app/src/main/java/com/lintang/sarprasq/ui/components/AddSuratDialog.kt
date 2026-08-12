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
import com.lintang.sarprasq.data.model.SuratArsip
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSuratDialog(
    suratToEdit: SuratArsip? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        nomorSurat: String,
        tanggalSurat: String,
        perihal: String,
        jenisSurat: String,
        statusArsip: String
    ) -> Unit
) {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    val today = sdf.format(java.util.Date())

    var nomorSurat by remember { mutableStateOf(suratToEdit?.nomorSurat ?: "") }
    var tanggalSurat by remember { mutableStateOf(suratToEdit?.tanggalSurat?.ifBlank { today } ?: today) }
    var perihal by remember { mutableStateOf(suratToEdit?.perihal ?: "") }
    var jenisSurat by remember { mutableStateOf(suratToEdit?.jenisSurat?.ifBlank { "Surat Masuk" } ?: "Surat Masuk") }
    var statusArsip by remember { mutableStateOf(suratToEdit?.statusArsip?.ifBlank { "Arsip Fisik & Digital" } ?: "Arsip Fisik & Digital") }

    var expandedJenis by remember { mutableStateOf(false) }
    var expandedArsip by remember { mutableStateOf(false) }

    val jenisOptions = listOf("Surat Masuk", "Surat Keluar", "Nota Dinas", "Permohonan Perbaikan")
    val arsipOptions = listOf("Arsip Fisik & Digital", "Arsip Fisik", "Arsip Digital")

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
                    text = if (suratToEdit == null) "Tambah Arsip Surat Sarpras" else "Edit Arsip Surat Sarpras",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nomorSurat,
                    onValueChange = { nomorSurat = it },
                    label = { Text("Nomor Surat") },
                    placeholder = { Text("Contoh: 015/SRV/VII/2026") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                DatePickerField(
                    value = tanggalSurat,
                    onDateSelected = { tanggalSurat = it },
                    label = "Tanggal Surat"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = perihal,
                    onValueChange = { perihal = it },
                    label = { Text("Perihal / Instansi") },
                    placeholder = { Text("Contoh: Permohonan Perbaikan Plafon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedJenis,
                    onExpandedChange = { expandedJenis = !expandedJenis }
                ) {
                    OutlinedTextField(
                        value = jenisSurat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis Surat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJenis) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedJenis,
                        onDismissRequest = { expandedJenis = false }
                    ) {
                        jenisOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    jenisSurat = option
                                    expandedJenis = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedArsip,
                    onExpandedChange = { expandedArsip = !expandedArsip }
                ) {
                    OutlinedTextField(
                        value = statusArsip,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Kelengkapan Arsip") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedArsip) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedArsip,
                        onDismissRequest = { expandedArsip = false }
                    ) {
                        arsipOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    statusArsip = option
                                    expandedArsip = false
                                }
                            )
                        }
                    }
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
                            if (nomorSurat.isNotBlank() && perihal.isNotBlank()) {
                                onSubmit(nomorSurat, tanggalSurat, perihal, jenisSurat, statusArsip)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (suratToEdit == null) "Simpan Surat" else "Simpan Perubahan")
                    }
                }
            }
        }
    }
}
