package com.afei.boxyledger.ui.ledger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afei.boxyledger.data.model.LedgerRecord
import com.afei.boxyledger.ui.theme.ExpenseRed
import com.afei.boxyledger.ui.theme.IncomeGreen
import com.afei.boxyledger.ui.theme.PrimaryYellow
import com.afei.boxyledger.ui.utils.IconMapper
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.afei.boxyledger.ui.utils.LunarUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: LedgerViewModel = viewModel(factory = LedgerViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Pager state for Month View
    // Initial page is a large number to allow swiping back
    val initialPage = 5000 
    val pagerState = rememberPagerState(initialPage = initialPage) { 10000 }
    
    // Map page index to YearMonth
    // Page 5000 is current month
    val currentMonthPage = remember { YearMonth.now() }
    val displayedMonth = remember(pagerState.currentPage) {
        currentMonthPage.plusMonths((pagerState.currentPage - initialPage).toLong())
    }
    
    // Calculate dynamic height for the HorizontalPager based on displayed month
    val daysInMonth = displayedMonth.lengthOfMonth()
    val firstDayOfMonth = displayedMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val totalDays = daysInMonth + startDayOfWeek
    val rows = (totalDays + 6) / 7
    val cellHeight = 48.dp
    val weekHeaderHeight = 32.dp
    val gridHeight = (rows * cellHeight.value).dp
    val pagerHeight = gridHeight + weekHeaderHeight
    val animatedPagerHeight by androidx.compose.animation.core.animateDpAsState(targetValue = pagerHeight)

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showYearMonthPicker by remember { mutableStateOf(false) }
    
    // When month changes via swipe, update selected date to first day of new month ONLY if selected date is not in new month
    LaunchedEffect(displayedMonth) {
        if (YearMonth.from(selectedDate) != displayedMonth) {
            selectedDate = displayedMonth.atDay(1)
        }
    }
    
    // Year Month Picker Dialog
    if (showYearMonthPicker) {
        YearMonthPickerDialog(
            initialYearMonth = displayedMonth,
            onYearMonthSelected = { newYearMonth ->
                val newDate = newYearMonth.atDay(1)
                selectedDate = newDate
                showYearMonthPicker = false
            },
            onDismissRequest = { showYearMonthPicker = false }
        )
    }
    
    // Scroll pager when selectedDate changes (e.g. from Picker)
    LaunchedEffect(selectedDate) {
        val targetMonth = YearMonth.from(selectedDate)
        if (targetMonth != displayedMonth) {
            val diff = java.time.temporal.ChronoUnit.MONTHS.between(currentMonthPage, targetMonth).toInt()
            val targetPage = initialPage + diff
            if (pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    // Calculate month stats for DISPLAYED MONTH
    val monthRecords = uiState.ledgerRecords.filterKeys { 
        val date = LocalDate.parse(it)
        YearMonth.from(date) == displayedMonth
    }
    
    val monthlyIncome = monthRecords.values.flatten().filter { it.type == 1 }.sumOf { it.amount }
    val monthlyExpense = monthRecords.values.flatten().filter { it.type == 0 }.sumOf { it.amount }
    val monthlyBalance = monthlyIncome - monthlyExpense
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showYearMonthPicker = true }
                    ) {
                        Text(
                            text = displayedMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Icon(Icons.Default.ExpandMore, contentDescription = null)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Today Button (Floating above FAB)
                AnimatedVisibility(
                    visible = selectedDate != LocalDate.now(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SmallFloatingActionButton(
                        onClick = { 
                            selectedDate = LocalDate.now()
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = PrimaryYellow,
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text("今", fontWeight = FontWeight.Bold)
                    }
                }

                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = PrimaryYellow, 
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Month Stats Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("收入 ${String.format("%.2f", monthlyIncome)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Text("支出 ${String.format("%.2f", monthlyExpense)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Text("结余 ${String.format("%.2f", monthlyBalance)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            // Horizontal Pager for Calendar
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.height(animatedPagerHeight), // Dynamic height
                verticalAlignment = Alignment.Top // Align top to avoid center drift
            ) { page ->
                val monthForPage = currentMonthPage.plusMonths((page - initialPage).toLong())
                
                CalendarGrid(
                    currentMonth = monthForPage,
                    selectedDate = selectedDate,
                    ledgerRecords = uiState.ledgerRecords,
                    onDateSelected = { selectedDate = it }
                )
            }
            
            // Selected Date Transaction List
            val selectedDateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val dailyRecords = uiState.ledgerRecords[selectedDateStr] ?: emptyList()
            
            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            // List Content
            Column(
                modifier = Modifier
                    .weight(1f) // Take remaining space
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Header: Date + Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dailyBalance = dailyRecords.sumOf { if (it.type == 1) it.amount else -it.amount }
                    val weekDay = selectedDate.format(DateTimeFormatter.ofPattern("EEE", Locale.CHINA))
                    val lunarDate = LunarUtils.getLunarDate(selectedDate)
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (selectedDate == LocalDate.now()) "今天 " else ""}$weekDay ${selectedDate.dayOfMonth}日",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lunarDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Text(
                        text = "结余 ${String.format("%.2f", dailyBalance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (dailyRecords.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("无记录", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(dailyRecords) { record ->
                            CalendarTransactionItem(
                                record = record,
                                iconName = uiState.categoryIcons[record.category] ?: "Category",
                                accountName = uiState.accountNames[record.accountId] ?: ""
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    ledgerRecords: Map<String, List<LedgerRecord>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
    
    // Calculate required rows for the grid
    val totalDays = daysInMonth + startDayOfWeek
    val rows = (totalDays + 6) / 7
    val cellHeight = 48.dp
    val gridHeight = (rows * cellHeight.value).dp
    
    // Animate the grid height
    val animatedGridHeight by androidx.compose.animation.core.animateDpAsState(targetValue = gridHeight)
    
    Column {
        // Week Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { 
                Text(
                    text = it, 
                    color = Color.Gray, 
                    fontSize = 10.sp
                )
            }
        }
        
        // Calendar Cells
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            val cellWidth = maxWidth / 7
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(animatedGridHeight),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Empty cells for previous month
                items(startDayOfWeek) {
                    Spacer(modifier = Modifier.size(40.dp))
                }
                
                // Days
                items(daysInMonth) { dayOffset ->
                    val day = dayOffset + 1
                    val date = currentMonth.atDay(day)
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    
                    // Get Daily Stats
                    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val records = ledgerRecords[dateStr] ?: emptyList()
                    val income = records.filter { it.type == 1 }.sumOf { it.amount }
                    val expense = records.filter { it.type == 0 }.sumOf { it.amount }
                    
                    // Lunar Date
                    val lunarText = LunarUtils.getLunarDate(date)
                    
                    Column(
                        modifier = Modifier
                            .height(cellHeight)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryYellow.copy(alpha = 0.3f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) PrimaryYellow else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onDateSelected(date) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.toString(),
                            fontSize = 12.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) Color(0xFFE6C200) else Color.Black
                        )
                        
                        if (income > 0 || expense > 0) {
                            Text(
                                text = if (expense > 0) "-${expense.toInt()}" else "+${income.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (expense > 0) ExpenseRed else IncomeGreen,
                                maxLines = 1
                            )
                        } else {
                             Text(
                                text = lunarText,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarTransactionItem(
    record: LedgerRecord,
    iconName: String,
    accountName: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (!record.note.isNullOrEmpty()) record.note else record.category,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(java.time.Instant.ofEpochMilli(record.date).atZone(java.time.ZoneId.systemDefault())),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        // Amount
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (record.type == 0) "-${String.format("%.2f", record.amount)}" else "+${String.format("%.2f", record.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (record.type == 0) ExpenseRed else IncomeGreen
            )
            Text(
                text = accountName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
