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
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPeminjamanDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        bulanTahun: String,
        namaBarang: String,
        jumlahPeminjaman: Int,
        kondisi: String
    ) -> Unit
) {
    var bulanTahun by remember { mutableStateOf("Juli 2026") }
    var namaBarang by remember { mutableStateOf("") }
    var jumlahString by remember { mutableStateOf("1") }
    var kondisi by remember { mutableStateOf("Aman") }

    var expandedKondisi by remember { mutableStateOf(false) }
    val kondisiOptions = listOf("Aman", "Perlu Maintenance", "Kurang Baik")

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
                    text = "Tambah Rekap Peminjaman Makro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bulanTahun,
                    onValueChange = { bulanTahun = it },
                    label = { Text("Periode Bulan & Tahun") },
                    placeholder = { Text("Contoh: Juli 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = namaBarang,
                    onValueChange = { namaBarang = it },
                    label = { Text("Nama Barang / Fasilitas (Ketik Manual)") },
                    placeholder = { Text("Contoh: Sound System Wireless Portable") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = jumlahString,
                    onValueChange = { jumlahString = it },
                    label = { Text("Total Kali Peminjaman (Global)") },
                    placeholder = { Text("10") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedKondisi,
                    onExpandedChange = { expandedKondisi = !expandedKondisi }
                ) {
                    OutlinedTextField(
                        value = kondisi,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kondisi Fisik Fasilitas") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKondisi) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedKondisi,
                        onDismissRequest = { expandedKondisi = false }
                    ) {
                        kondisiOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    kondisi = option
                                    expandedKondisi = false
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
                            val count = jumlahString.toIntOrNull() ?: 1
                            if (namaBarang.isNotBlank()) {
                                onSubmit(bulanTahun, namaBarang, count, kondisi)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan Rekap")
                    }
                }
            }
        }
    }
}
