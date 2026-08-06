package com.lintang.sarprasq.ui.components

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ManualImageCropDialog(
    sourceBitmap: Bitmap,
    onCropped: (Bitmap) -> Unit,
    onUseOriginal: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val cropWindowDp = 220.dp
    val cropWindowPx = 220f * 3f // approximate viewport px scale

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Crop,
                    contentDescription = null,
                    tint = PastelSkyBlueDark,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Crop / Potong Gambar Manual",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Geser, perbesar (zoom), atau atur posisi gambar sesuai area pemotongan yang Anda inginkan.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                // --- CROPPER CANVAS VIEWPORT ---
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5.0f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val imageBitmap = remember(sourceBitmap) { sourceBitmap.asImageBitmap() }

                    Canvas(
                        modifier = Modifier.fillMaxWidth().height(240.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Calculate aspect scale for source image to fit baseline
                        val srcAspect = sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat()
                        val baseDrawWidth: Float
                        val baseDrawHeight: Float
                        if (srcAspect > 1f) {
                            baseDrawWidth = canvasWidth
                            baseDrawHeight = canvasWidth / srcAspect
                        } else {
                            baseDrawHeight = canvasHeight
                            baseDrawWidth = canvasHeight * srcAspect
                        }

                        val drawW = baseDrawWidth * scale
                        val drawH = baseDrawHeight * scale

                        val left = (canvasWidth - drawW) / 2f + offsetX
                        val top = (canvasHeight - drawH) / 2f + offsetY

                        // Draw source image transformed
                        drawImage(
                            image = imageBitmap,
                            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                            dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt())
                        )

                        // Draw Crop Box Overlay with Dimmed Outer Layer
                        val cropBoxSize = min(canvasWidth, canvasHeight) * 0.82f
                        val cropLeft = (canvasWidth - cropBoxSize) / 2f
                        val cropTop = (canvasHeight - cropBoxSize) / 2f
                        val cropRight = cropLeft + cropBoxSize
                        val cropBottom = cropTop + cropBoxSize

                        // Path for outside dimmed background
                        val outerPath = Path().apply {
                            addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                            addRect(Rect(cropLeft, cropTop, cropRight, cropBottom))
                            fillType = PathFillType.EvenOdd
                        }
                        drawPath(outerPath, color = Color.Black.copy(alpha = 0.55f))

                        // Draw crop frame white/pastel border
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(cropLeft, cropTop),
                            size = Size(cropBoxSize, cropBoxSize),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw 3x3 grid guidelines inside crop frame
                        val thirdW = cropBoxSize / 3f
                        val thirdH = cropBoxSize / 3f
                        for (i in 1..2) {
                            // Vertical guidelines
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = Offset(cropLeft + thirdW * i, cropTop),
                                end = Offset(cropLeft + thirdW * i, cropBottom),
                                strokeWidth = 1.dp.toPx()
                            )
                            // Horizontal guidelines
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = Offset(cropLeft, cropTop + thirdH * i),
                                end = Offset(cropRight, cropTop + thirdH * i),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }

                // --- CONTROLS BAR (ZOOM SLIDER & RESET) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { scale = (scale - 0.2f).coerceAtLeast(0.5f) }) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextSecondary)
                    }

                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 0.5f..4.0f,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { scale = (scale + 0.2f).coerceAtMost(4.0f) }) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextSecondary)
                    }

                    IconButton(
                        onClick = {
                            scale = 1.0f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Posisi", tint = PastelPeachDark)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cropped = cropBitmapManually(
                        source = sourceBitmap,
                        scale = scale,
                        offsetX = offsetX,
                        offsetY = offsetY
                    )
                    onCropped(cropped)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark)
            ) {
                Text("Potong & Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onUseOriginal != null) {
                    TextButton(onClick = onUseOriginal) {
                        Text("Asli", color = PastelSkyBlueDark, fontWeight = FontWeight.SemiBold)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = TextSecondary)
                }
            }
        }
    )
}

/**
 * Calculates the exact crop area based on manual zoom & pan transform relative to the crop viewport
 */
private fun cropBitmapManually(
    source: Bitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): Bitmap {
    try {
        val srcW = source.width
        val srcH = source.height

        // Canvas viewport size constant
        val canvasDim = 240f
        val cropBoxDim = canvasDim * 0.82f
        val cropLeftOnCanvas = (canvasDim - cropBoxDim) / 2f
        val cropTopOnCanvas = (canvasDim - cropBoxDim) / 2f

        // Base layout of image inside canvas before zoom/pan
        val srcAspect = srcW.toFloat() / srcH.toFloat()
        val baseDrawWidth: Float
        val baseDrawHeight: Float
        if (srcAspect > 1f) {
            baseDrawWidth = canvasDim
            baseDrawHeight = canvasDim / srcAspect
        } else {
            baseDrawHeight = canvasDim
            baseDrawWidth = canvasDim * srcAspect
        }

        val currentDrawWidth = baseDrawWidth * scale
        val currentDrawHeight = baseDrawHeight * scale

        val imgLeftOnCanvas = (canvasDim - currentDrawWidth) / 2f + offsetX
        val imgTopOnCanvas = (canvasDim - currentDrawHeight) / 2f + offsetY

        // Map crop window coordinates back to original source bitmap coordinates
        val cropXInImagePx = ((cropLeftOnCanvas - imgLeftOnCanvas) / currentDrawWidth) * srcW
        val cropYInImagePx = ((cropTopOnCanvas - imgTopOnCanvas) / currentDrawHeight) * srcH
        val cropWInImagePx = (cropBoxDim / currentDrawWidth) * srcW
        val cropHInImagePx = (cropBoxDim / currentDrawHeight) * srcH

        // Clamp to bitmap boundaries securely
        val x = cropXInImagePx.roundToInt().coerceIn(0, srcW - 1)
        val y = cropYInImagePx.roundToInt().coerceIn(0, srcH - 1)
        val w = cropWInImagePx.roundToInt().coerceIn(1, srcW - x)
        val h = cropHInImagePx.roundToInt().coerceIn(1, srcH - y)

        return Bitmap.createBitmap(source, x, y, w, h)
    } catch (e: Exception) {
        // Fallback in case of boundary overflow
        return source
    }
}
