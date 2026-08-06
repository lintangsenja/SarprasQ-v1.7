package com.lintang.sarprasq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark

@Composable
fun ProfileAvatar(
    imagePath: String?,
    modifier: Modifier = Modifier,
    fallbackIcon: ImageVector = Icons.Default.Person,
    iconSize: Dp = 24.dp,
    shape: Shape = CircleShape,
    backgroundColor: Color = PastelSkyBlue.copy(alpha = 0.4f),
    borderColor: Color = PastelSkyBlueDark
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.5.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        if (!imagePath.isNullOrBlank()) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Foto / Logo Profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
            )
        } else {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = "Foto Profil Kosong",
                tint = borderColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
