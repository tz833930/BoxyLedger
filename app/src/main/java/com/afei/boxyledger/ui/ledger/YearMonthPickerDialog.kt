package com.afei.boxyledger.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afei.boxyledger.ui.theme.PrimaryYellow
import java.time.YearMonth

@Composable
fun YearMonthPickerDialog(
    initialYearMonth: YearMonth,
    onYearMonthSelected: (YearMonth) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialYearMonth.year) }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedYear-- }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Year")
                }
                Text(
                    text = "$selectedYear",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { selectedYear++ }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Year")
                }
            }
        },
        text = {
            Column {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(12) { index ->
                        val month = index + 1
                        val isSelected = month == initialYearMonth.monthValue && selectedYear == initialYearMonth.year
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryYellow else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryYellow else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onYearMonthSelected(YearMonth.of(selectedYear, month)) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${month}月",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.Black
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}
