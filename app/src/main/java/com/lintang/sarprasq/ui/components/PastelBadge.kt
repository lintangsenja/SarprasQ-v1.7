package com.lintang.sarprasq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.ui.theme.PastelButterYellow
import com.lintang.sarprasq.ui.theme.PastelButterYellowDark
import com.lintang.sarprasq.ui.theme.PastelLavender
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelMintLight
import com.lintang.sarprasq.ui.theme.PastelPeach
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark

@Composable
fun UrgencyBadge(urgensi: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (urgensi.lowercase()) {
        "darurat" -> Pair(PastelPeach, PastelPeachDark)               // Pink Pastel (#FADBD8)
        "penting" -> Pair(PastelButterYellow, PastelButterYellowDark) // Yellow Butter Pastel (#FCF3CF)
        "sedang" -> Pair(PastelSkyBlue, PastelSkyBlueDark)            // Sky Blue Pastel
        else -> Pair(PastelLavender, PastelLavenderDark)              // Lavender
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = textColor.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = urgensi,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "catat" -> Pair(PastelSkyBlue, PastelSkyBlueDark)              // Biru Pastel Catat
        "pending" -> Pair(PastelPeach, PastelPeachDark)               // Merah Muda Pastel
        "segera" -> Pair(PastelLavender, PastelLavenderDark)          // Ungu Lavender
        "proses", "diproses" -> Pair(PastelButterYellow, PastelButterYellowDark) // Kuning Mentega
        "selesai" -> Pair(PastelMintLight, PastelMintDark)            // Hijau Mint
        "ditolak", "dibatalkan" -> Pair(PastelPeach.copy(alpha = 0.6f), Color(0xFF991B1B)) // Merah Ditolak
        else -> Pair(PastelSkyBlue, PastelSkyBlueDark)
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = textColor.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun ProgresBadge(progres: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (progres.lowercase()) {
        "selesai" -> Pair(PastelMintLight, PastelMintDark)
        "dalam proses" -> Pair(PastelButterYellow, PastelButterYellowDark)
        else -> Pair(Color(0xFFE2E8F0), Color(0xFF64748B)) // Abu pastel
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = textColor.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = progres,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun JenisRencanaBadge(jenis: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (jenis.lowercase()) {
        "rencana pengadaan" -> Pair(PastelSkyBlue, PastelSkyBlueDark)
        "rencana belanja barang / jasa" -> Pair(PastelLavender, PastelLavenderDark)
        else -> Pair(PastelPeach, PastelPeachDark) // "rencana perbaikan / perawatan" or fallback
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = textColor.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = jenis,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
