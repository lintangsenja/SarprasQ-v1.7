package com.lintang.sarprasq.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lintang.sarprasq.ui.viewmodel.SarprasViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lintang.sarprasq.data.model.PeminjamanMakro
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DynamicPeminjamanItemState(
    val id: String = UUID.randomUUID().toString(),
    var namaBarang: String = "",
    var jumlahString: String = "1",
    var kondisi: String = ""
)

data class DynamicPeminjamanDateState(
    val id: String = UUID.randomUUID().toString(),
    var tanggalPinjam: String = getCurrentFormattedDate(),
    var tanggalKembali: String = getCurrentFormattedDate()
)

private fun getCurrentFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
    return sdf.format(Date())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPeminjamanDialog(
    viewModel: SarprasViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSubmit: (
        bulanTahun: String,
        namaBarang: String,
        jumlahPeminjaman: Int,
        kondisi: String
    ) -> Unit = { _, _, _, _ -> },
    onSubmitBatch: ((List<PeminjamanMakro>) -> Unit)? = null
) {
    val context = LocalContext.current

    var namaPeminjam by remember { mutableStateOf("") }

    val masterKondisiList by viewModel.allKondisi.collectAsState()

    val kondisiOptions = remember(masterKondisiList) {
        masterKondisiList.map { it.namaKondisi }
    }

    // Multi-Item State
    val itemsList = remember {
        mutableStateListOf(DynamicPeminjamanItemState(kondisi = kondisiOptions.firstOrNull().orEmpty()))
    }

    LaunchedEffect(kondisiOptions) {
        if (kondisiOptions.isNotEmpty()) {
            val defaultCond = kondisiOptions.firstOrNull().orEmpty()
            itemsList.forEach { item ->
                if (item.kondisi.isBlank() || item.kondisi !in kondisiOptions) {
                    item.kondisi = defaultCond
                }
            }
        }
    }

    // Multi-Date State
    val datesList = remember {
        mutableStateListOf(DynamicPeminjamanDateState())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // --- HEADER TITLE ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PastelSkyBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = PastelSkyBlueDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Form Transaksi Peminjaman",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Mendukung multi-item barang & multi-tanggal beruntun",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- SCROLLABLE FORM BODY ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. DATA PEMINJAM / KEGIATAN (OPSIONAL)
                    OutlinedTextField(
                        value = namaPeminjam,
                        onValueChange = { namaPeminjam = it },
                        label = { Text("Nama Peminjam / Unit / Kegiatan (Opsional)", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: OSIS / Rapat Pleno", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = PastelSkyBlueDark,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PastelSkyBlueDark,
                            unfocusedBorderColor = PastelCardBorder
                        )
                    )

                    // 2. FITUR MULTI-DATE (KALENDER INTERAKTIF & TANGGAL BERUNTUN)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PastelLavender.copy(alpha = 0.25f))
                            .border(1.dp, PastelLavenderDark.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = PastelLavenderDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Jadwal Tanggal Peminjaman",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelLavenderDark
                                )
                            }

                            if (datesList.size > 1) {
                                Text(
                                    text = "${datesList.size} Periode Schedule",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelLavenderDark
                                )
                            }
                        }

                        datesList.forEachIndexed { dateIdx, dateState ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        DatePickerField(
                                            value = dateState.tanggalPinjam,
                                            onDateSelected = { dateState.tanggalPinjam = it },
                                            label = "Tgl Pinjam #${dateIdx + 1}",
                                            modifier = Modifier.weight(1f)
                                        )

                                        DatePickerField(
                                            value = dateState.tanggalKembali,
                                            onDateSelected = { dateState.tanggalKembali = it },
                                            label = "Tgl Kembali #${dateIdx + 1}",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                if (datesList.size > 1) {
                                    IconButton(
                                        onClick = { datesList.removeAt(dateIdx) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Hapus Tanggal",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // TOMBOL INTERAKTIF "+ Tambah Tanggal Peminjaman Lain"
                        OutlinedButton(
                            onClick = {
                                datesList.add(DynamicPeminjamanDateState())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = PastelLavenderDark
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavenderDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+ Tambah Tanggal Peminjaman Lain",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3. FITUR MULTI-ITEM (FORM MULTI-BARANG)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PastelSurface)
                            .border(1.dp, PastelCardBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Barang yang Dipinjam (${itemsList.size} Jenis)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        itemsList.forEachIndexed { itemIdx, itemState ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, PastelCardBorder, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Barang #${itemIdx + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelSkyBlueDark
                                    )

                                    if (itemsList.size > 1) {
                                        IconButton(
                                            onClick = { itemsList.removeAt(itemIdx) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Barang",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = itemState.namaBarang,
                                    onValueChange = { itemState.namaBarang = it },
                                    label = { Text("Nama / Kode Alat / Barang", fontSize = 11.5.sp) },
                                    placeholder = { Text("Contoh: Projector Epson EB-X400", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = itemState.jumlahString,
                                        onValueChange = { itemState.jumlahString = it },
                                        label = { Text("Jumlah Unit", fontSize = 11.5.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    var expandedKondisi by remember { mutableStateOf(false) }

                                    ExposedDropdownMenuBox(
                                        expanded = expandedKondisi,
                                        onExpandedChange = { expandedKondisi = !expandedKondisi },
                                        modifier = Modifier.weight(1.4f)
                                    ) {
                                        OutlinedTextField(
                                            value = itemState.kondisi,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Kondisi", fontSize = 11.5.sp) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKondisi) },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expandedKondisi,
                                            onDismissRequest = { expandedKondisi = false }
                                        ) {
                                            kondisiOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option, fontSize = 12.sp) },
                                                    onClick = {
                                                        itemState.kondisi = option
                                                        expandedKondisi = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // TOMBOL INTERAKTIF "+ Tambah Barang Lain"
                        Button(
                            onClick = {
                                itemsList.add(DynamicPeminjamanItemState(kondisi = kondisiOptions.firstOrNull().orEmpty()))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PastelSkyBlueDark,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+ Tambah Barang Lain",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- FOOTER BUTTONS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal", fontSize = 12.5.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            // Validasi input
                            val invalidItem = itemsList.any { it.namaBarang.isBlank() }
                            if (invalidItem) {
                                Toast.makeText(
                                    context,
                                    "Mohon isi nama/kode barang pada semua baris peminjaman!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            // Format schedule string
                            val formattedDatesString = datesList.joinToString(" & ") { dateState ->
                                if (dateState.tanggalPinjam == dateState.tanggalKembali) {
                                    "Tgl ${dateState.tanggalPinjam}"
                                } else {
                                    "${dateState.tanggalPinjam} s/d ${dateState.tanggalKembali}"
                                }
                            }

                            val peminjamPrefix = if (namaPeminjam.isNotBlank()) "[$namaPeminjam] " else ""
                            val finalPeriodeTgl = "$peminjamPrefix$formattedDatesString"

                            val resultPeminjamanList = mutableListOf<PeminjamanMakro>()
                            val nowTime = System.currentTimeMillis()

                            itemsList.forEachIndexed { index, itemState ->
                                val count = itemState.jumlahString.toIntOrNull() ?: 1
                                resultPeminjamanList.add(
                                    PeminjamanMakro(
                                        bulanTahun = finalPeriodeTgl,
                                        namaBarang = itemState.namaBarang,
                                        jumlahPeminjaman = count,
                                        kondisi = itemState.kondisi,
                                        timestamp = nowTime - (index * 50)
                                    )
                                )
                            }

                            if (onSubmitBatch != null) {
                                onSubmitBatch(resultPeminjamanList)
                            } else {
                                resultPeminjamanList.forEach { item ->
                                    onSubmit(item.bulanTahun, item.namaBarang, item.jumlahPeminjaman, item.kondisi)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelMintDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Simpan Transaksi (${itemsList.size} Barang)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
