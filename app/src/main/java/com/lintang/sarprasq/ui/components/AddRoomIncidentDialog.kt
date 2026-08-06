package com.lintang.sarprasq.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.ui.theme.PastelBackground
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelMint
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RoomItemFormState(
    var namaItem: String = "",
    var kategori: String = "Elektronik & Audio Visual",
    var urgensi: String = "Sedang",
    var keterangan: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomIncidentDialog(
    ruangMasterList: List<Ruang> = emptyList(),
    kategoriMasterList: List<String> = emptyList(),
    preSelectedRuangId: Int? = null,
    onAddKategoriToMaster: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        roomId: Int,
        namaRuang: String,
        namaPelapor: String,
        tanggalLapor: String,
        items: List<RoomItemFormState>
    ) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    val defaultKategoriList = remember(kategoriMasterList) {
        kategoriMasterList
    }

    val defaultUrgensiList = listOf("Darurat", "Penting", "Sedang", "Rendah")

    // Find initial preselected room if any
    val initialRuang = remember(preSelectedRuangId, ruangMasterList) {
        ruangMasterList.find { it.id == preSelectedRuangId } ?: ruangMasterList.firstOrNull()
    }

    var selectedRuang by remember { mutableStateOf<Ruang?>(initialRuang) }
    var expandedRuangDropdown by remember { mutableStateOf(false) }

    var namaPelapor by remember { mutableStateOf("Kevin Ricky Utama, S.Kom.") }
    var tanggalLapor by remember { mutableStateOf(currentDateStr) }

    val itemList = remember {
        mutableStateListOf(
            RoomItemFormState(namaItem = "", kategori = defaultKategoriList.first(), urgensi = "Sedang", keterangan = "")
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = PastelSurface,
            border = BorderStroke(1.dp, PastelCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PastelPeach),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = PastelPeachDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Laporan Kerusakan Masal",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Catat beberapa kerusakan sekaligus per ruangan",
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- SECTION 1: LOKASI RUANGAN & PELAPOR ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = PastelSkyBlueContainer,
                        border = BorderStroke(1.dp, PastelSkyBlue.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "1. LOKASI RUANGAN (PILIH SEBALIKNYA SAKALI DI AWAL)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PastelSkyBlueDark,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Room Dropdown Selector
                            ExposedDropdownMenuBox(
                                expanded = expandedRuangDropdown,
                                onExpandedChange = { expandedRuangDropdown = !expandedRuangDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedRuang?.let { "${it.namaRuang} (${it.kodeRuang})" } ?: "Pilih Ruangan",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Pilih Master Ruangan *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRuangDropdown) },
                                    leadingIcon = { Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = PastelSkyBlueDark) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedRuangDropdown,
                                    onDismissRequest = { expandedRuangDropdown = false }
                                ) {
                                    ruangMasterList.forEach { r ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(text = r.namaRuang, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                                    Text(text = "Kode: ${r.kodeRuang} • ${r.kategori}", fontSize = 11.sp, color = TextSecondary)
                                                }
                                            },
                                            onClick = {
                                                selectedRuang = r
                                                expandedRuangDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = namaPelapor,
                                    onValueChange = { namaPelapor = it },
                                    label = { Text("Nama Pelapor") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = tanggalLapor,
                                    onValueChange = { tanggalLapor = it },
                                    label = { Text("Tanggal Lapor") },
                                    modifier = Modifier.width(130.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // --- SECTION 2: BARIS INLINE KERUSAKAN MULTI-ITEM ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. DAFTAR ITEM KERUSAKAN (${itemList.size} Item)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PastelPeach)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Inline Multi-Item",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelPeachDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render each item row
                        itemList.forEachIndexed { index, itemState ->
                            ItemRowInputCard(
                                itemIndex = index + 1,
                                itemState = itemState,
                                kategoriOptions = defaultKategoriList,
                                urgensiOptions = defaultUrgensiList,
                                canDelete = itemList.size > 1,
                                onAddKategoriToMaster = onAddKategoriToMaster,
                                onDelete = { itemList.removeAt(index) },
                                onUpdate = { updated -> itemList[index] = updated }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Button "+ Tambah Item Lain di Ruangan Ini"
                        OutlinedButton(
                            onClick = {
                                itemList.add(
                                    RoomItemFormState(
                                        namaItem = "",
                                        kategori = defaultKategoriList.firstOrNull() ?: "",
                                        urgensi = "Sedang",
                                        keterangan = ""
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, PastelSkyBlueDark),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelSkyBlueDark)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+ Tambah Item Lain di Ruangan Ini",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Batal", color = TextSecondary)
                    }

                    val isValid = selectedRuang != null && itemList.any { it.namaItem.isNotBlank() }

                    Button(
                        onClick = {
                            val r = selectedRuang ?: return@Button
                            val validItems = itemList.filter { it.namaItem.isNotBlank() }
                            if (validItems.isNotEmpty()) {
                                onSubmit(r.id, r.namaRuang, namaPelapor, tanggalLapor, validItems)
                            }
                        },
                        enabled = isValid,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PastelSkyBlueDark,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Simpan ${itemList.count { it.namaItem.isNotBlank() }} Item Laporan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemRowInputCard(
    itemIndex: Int,
    itemState: RoomItemFormState,
    kategoriOptions: List<String>,
    urgensiOptions: List<String>,
    canDelete: Boolean,
    onAddKategoriToMaster: ((String) -> Unit)? = null,
    onDelete: () -> Unit,
    onUpdate: (RoomItemFormState) -> Unit
) {
    var expandedKat by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val isCustom = itemState.kategori.isNotBlank() && !kategoriOptions.any { it.equals(itemState.kategori.trim(), ignoreCase = true) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, PastelCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Item Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PastelSkyBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$itemIndex",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PastelSkyBlueDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Item Kerusakan #$itemIndex",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Baris",
                            tint = PastelPeachDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nama Barang / Fasilitas
            OutlinedTextField(
                value = itemState.namaItem,
                onValueChange = { onUpdate(itemState.copy(namaItem = it)) },
                label = { Text("Nama Barang / Fasilitas Rusak *") },
                placeholder = { Text("misal: AC Indoor Unit 1 / Lampu / Stopkontak", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Kategori Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedKat,
                onExpandedChange = { expandedKat = !expandedKat }
            ) {
                OutlinedTextField(
                    value = itemState.kategori,
                    onValueChange = { onUpdate(itemState.copy(kategori = it)); expandedKat = true },
                    readOnly = false,
                    label = { Text("Kategori Barang") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCustom && onAddKategoriToMaster != null) {
                                IconButton(
                                    onClick = {
                                        onAddKategoriToMaster(itemState.kategori.trim())
                                        android.widget.Toast.makeText(context, "Kategori '${itemState.kategori.trim()}' disimpan ke Master", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = "Simpan ke Master", tint = PastelMintDark, modifier = Modifier.size(18.dp))
                                }
                            }
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKat)
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedKat,
                    onDismissRequest = { expandedKat = false }
                ) {
                    if (isCustom && onAddKategoriToMaster != null) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = PastelMintDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah \"${itemState.kategori.trim()}\" ke Master", color = PastelMintDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                onAddKategoriToMaster(itemState.kategori.trim())
                                expandedKat = false
                                android.widget.Toast.makeText(context, "Kategori '${itemState.kategori.trim()}' disimpan ke Master", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider()
                    }
                    kategoriOptions.filter { it.contains(itemState.kategori, ignoreCase = true) }.forEach { kat ->
                        DropdownMenuItem(
                            text = { Text(kat, fontSize = 13.sp) },
                            onClick = {
                                onUpdate(itemState.copy(kategori = kat))
                                expandedKat = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tingkat Urgensi Selector Chips
            Text(
                text = "Tingkat Urgensi Kerusakan:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                urgensiOptions.forEach { urg ->
                    val isSelected = itemState.urgensi.equals(urg, ignoreCase = true)
                    val (chipBg, chipText) = when {
                        urg.equals("Darurat", true) -> if (isSelected) Pair(PastelPeachDark, Color.White) else Pair(PastelPeach.copy(alpha = 0.5f), PastelPeachDark)
                        urg.equals("Penting", true) -> if (isSelected) Pair(PastelButterYellowDark, Color.Black) else Pair(PastelBackground, TextPrimary)
                        urg.equals("Sedang", true) -> if (isSelected) Pair(PastelSkyBlueDark, Color.White) else Pair(PastelSkyBlueContainer, PastelSkyBlueDark)
                        else -> if (isSelected) Pair(PastelMintDark, Color.White) else Pair(PastelMint.copy(alpha = 0.5f), PastelMintDark)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(1.dp, if (isSelected) chipBg else PastelCardBorder, RoundedCornerShape(10.dp))
                            .clickable { onUpdate(itemState.copy(urgensi = urg)) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = urg,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = chipText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Keterangan Kerusakan
            OutlinedTextField(
                value = itemState.keterangan,
                onValueChange = { onUpdate(itemState.copy(keterangan = it)) },
                label = { Text("Keterangan Kerusakan") },
                placeholder = { Text("Rincian detail fisik kerusakan...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )
        }
    }
}
