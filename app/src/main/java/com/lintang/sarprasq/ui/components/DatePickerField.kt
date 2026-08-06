package com.lintang.sarprasq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lintang.sarprasq.ui.theme.PastelCardBorder
import com.lintang.sarprasq.ui.theme.PastelLavenderDark
import com.lintang.sarprasq.ui.theme.PastelMintDark
import com.lintang.sarprasq.ui.theme.PastelPeachDark
import com.lintang.sarprasq.ui.theme.PastelSkyBlue
import com.lintang.sarprasq.ui.theme.PastelSkyBlueContainer
import com.lintang.sarprasq.ui.theme.PastelSkyBlueDark
import com.lintang.sarprasq.ui.theme.PastelSurface
import com.lintang.sarprasq.ui.theme.TextMuted
import com.lintang.sarprasq.ui.theme.TextPrimary
import com.lintang.sarprasq.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "DD/MM/YYYY",
    readOnly: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onDateSelected(it) },
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            placeholder = { Text(placeholder, fontSize = 12.sp, color = TextMuted) },
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pilih Tanggal Kalender",
                        tint = PastelSkyBlueDark
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PastelSkyBlueDark,
                unfocusedBorderColor = PastelCardBorder,
                focusedContainerColor = PastelSurface,
                unfocusedContainerColor = PastelSurface
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        val calendar = Calendar.getInstance()
        if (value.isNotBlank()) {
            try {
                val parts = value.split("/")
                if (parts.size == 3) {
                    val day = parts[0].trim().toInt()
                    val month = parts[1].trim().toInt() - 1
                    val year = parts[2].trim().toInt()
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                } else {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val parsed = sdf.parse(value)
                    if (parsed != null) calendar.time = parsed
                }
            } catch (e: Exception) {
                // Fallback to current date
            }
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )

        val customDatePickerColors = DatePickerDefaults.colors(
            containerColor = Color.White,
            titleContentColor = PastelSkyBlueDark,
            headlineContentColor = PastelSkyBlueDark,
            weekdayContentColor = PastelLavenderDark,
            subheadContentColor = TextSecondary,
            yearContentColor = TextPrimary,
            currentYearContentColor = PastelMintDark,
            selectedYearContentColor = Color.White,
            selectedYearContainerColor = PastelSkyBlueDark,
            dayContentColor = TextPrimary,
            disabledDayContentColor = TextMuted.copy(alpha = 0.4f),
            selectedDayContentColor = Color.White,
            selectedDayContainerColor = PastelSkyBlueDark,
            todayContentColor = PastelPeachDark,
            todayDateBorderColor = PastelPeachDark,
            dividerColor = PastelCardBorder,
            navigationContentColor = PastelSkyBlueDark
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = millis
                            }
                            val formatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
                            onDateSelected(formatted)
                        }
                        showPicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSkyBlueDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pilih Tanggal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPicker = false },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Batal", color = TextSecondary, fontSize = 12.sp)
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            // Header spanduk ceria untuk kalender
            Surface(
                color = PastelSkyBlueContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PastelSkyBlue.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = PastelSkyBlueDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "KALENDER OPERASIONAL SARPRASQ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelSkyBlueDark
                        )
                        Text(
                            text = label.ifBlank { "Pilih Tanggal Penanggalan" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            DatePicker(
                state = datePickerState,
                colors = customDatePickerColors
            )
        }
    }
}
