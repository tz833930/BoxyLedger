package com.afei.boxyledger.ui.ledger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afei.boxyledger.data.model.LedgerRecord
import com.afei.boxyledger.ui.theme.*
import com.afei.boxyledger.ui.utils.IconMapper
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LedgerScreen(
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onCalendarClick: () -> Unit,
    viewModel: LedgerViewModel = viewModel(factory = LedgerViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Flatten records for the list, sorted by date descending
    val allRecords = remember(uiState.ledgerRecords) {
        uiState.ledgerRecords.values.flatten().sortedByDescending { it.date }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = PrimaryYellow,
                contentColor = Color.Black,
                shape = CircleShape,
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("记一笔", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Top Month Summary Card
            item {
                MonthSummaryCard(
                    income = uiState.monthlyIncome,
                    expense = uiState.monthlyExpense
                )
            }

            // Daily Summary & Calendar Entry (Sticky)
            stickyHeader {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                     DailySummaryCard(
                        income = uiState.todayIncome,
                        expense = uiState.todayExpense,
                        onClick = onCalendarClick
                    )
                }
            }

            // Ledger List (All Records)
            if (allRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "还没有记账，快去记一笔吧~",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(allRecords, key = { it.id }) { record ->
                    SwipeableLedgerItem(
                        record = record,
                        iconName = uiState.categoryIcons[record.category] ?: "Category",
                        accountName = uiState.accountNames[record.accountId] ?: "账户",
                        onDelete = { viewModel.deleteRecord(record) },
                        onEdit = { onEditClick(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DailySummaryCard(
    income: Double,
    expense: Double,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd EEE", Locale.CHINA)
    val dateStr = today.format(formatter)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(56.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }

            // Right: Income/Expense
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "收",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.2f", income),
                    style = MaterialTheme.typography.bodyMedium,
                    color = IncomeGreen
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "支",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.2f", expense),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ExpenseRed
                )
            }
        }
    }
}

@Composable
fun MonthSummaryCard(income: Double, expense: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp), // 大圆角
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientYellowStart, GradientYellowEnd),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
            Text(
                text = "本月结余",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Black.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.2f", income - expense),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "本月收入",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("%.2f", income),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                }
                Column {
                    Text(
                        text = "本月支出",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("%.2f", expense),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
}

@Composable
fun SwipeableLedgerItem(
    record: LedgerRecord,
    iconName: String,
    accountName: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Swipe Logic
    val density = LocalDensity.current
    val actionWidth = 140.dp // 70dp * 2
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 1.dp) // Minimal spacing
                .height(72.dp), // Increased height for 2 rows
            contentAlignment = Alignment.CenterEnd
        ) {
            // Background Actions
            Row(
                modifier = Modifier
                    .width(actionWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp)),
                horizontalArrangement = Arrangement.End
            ) {
                // Edit Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(InfoBlue)
                        .clickable { 
                             scope.launch { offsetX.animateTo(0f) }
                             onEdit() 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                }
                // Delete Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(ExpenseRed)
                        .clickable {
                            scope.launch { offsetX.animateTo(0f) }
                            onDelete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                }
            }

            // Foreground Content
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                val target = offsetX.value + delta
                                if (target <= 0 && target >= -actionWidthPx) {
                                    offsetX.snapTo(target)
                                }
                            }
                        },
                        onDragStopped = {
                            if (offsetX.value < -actionWidthPx / 2) {
                                offsetX.animateTo(-actionWidthPx, tween(300))
                            } else {
                                offsetX.animateTo(0f, tween(300))
                            }
                        }
                    )
            ) {
                LedgerItemContent(record, iconName, accountName)
            }
        }
    }
}

@Composable
fun LedgerItemContent(record: LedgerRecord, iconName: String, accountName: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (record.type == 0) ExpenseRed.copy(alpha = 0.1f) else IncomeGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(iconName),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (record.type == 0) ExpenseRed else IncomeGreen
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Row 1: Category Name
                Text(
                    text = record.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Row 2: Time + Note
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatRecordTime(record.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (!record.note.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = record.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Right Side
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Row 1: Amount
                Text(
                    text = if (record.type == 0) "-${String.format("%.2f", record.amount)}" else "+${String.format("%.2f", record.amount)}",
                    color = if (record.type == 0) ExpenseRed else IncomeGreen,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Row 2: Account Name
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

fun formatRecordTime(timestamp: Long): String {
    val recordDate = java.time.Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    val timeStr = java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(java.time.Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneId.systemDefault()))
    val today = LocalDate.now()
    
    return when {
        recordDate.isEqual(today) -> "今天 $timeStr"
        recordDate.isEqual(today.minusDays(1)) -> "昨天 $timeStr"
        recordDate.isEqual(today.minusDays(2)) -> "前天 $timeStr"
        else -> "${recordDate.format(DateTimeFormatter.ofPattern("MM-dd"))} $timeStr"
    }
}
