package com.afei.boxyledger.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afei.boxyledger.ui.theme.ExpenseRed
import com.afei.boxyledger.ui.theme.IncomeGreen
import com.afei.boxyledger.ui.theme.PrimaryYellow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.afei.boxyledger.data.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit,
    ledgerId: Long? = null,
    viewModel: AddTransactionViewModel = viewModel(factory = AddTransactionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Load transaction if in edit mode
    LaunchedEffect(ledgerId) {
        if (ledgerId != null) {
            viewModel.loadTransaction(ledgerId)
        }
    }

    var showDiscountDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val currentCategories = if (uiState.type == 0) uiState.expenseCategories else uiState.incomeCategories

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        TabButton("支出", uiState.type == 0) { viewModel.onTypeChange(0) }
                        TabButton("收入", uiState.type == 1) { viewModel.onTypeChange(1) }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Keypad area
            NumericKeypad(
                onKeyPress = { key -> viewModel.onKeypadInput(key) },
                onComplete = {
                    val error = viewModel.saveTransaction()
                    if (error == null) {
                        onBackClick()
                    } else {
                        // Show Error (Toast or Snackbar)
                        // Using a simple context toast for now as Scaffold snackbar requires state hosting
                        android.widget.Toast.makeText(
                            context, 
                            error, 
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Category Grid
            CategoryGrid(
                selectedCategory = uiState.selectedCategory,
                onCategorySelect = { viewModel.onCategoryChange(it) },
                categories = currentCategories,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Input Area
            InputArea(
                amount = uiState.amountInput,
                note = uiState.note,
                onNoteChange = { viewModel.onNoteChange(it) },
                type = uiState.type
            )

            // Chips Row
            ChipsRow(
                uiState = uiState,
                onAccountClick = { showAccountDialog = true },
                onDateClick = { showDatePicker = true },
                onDiscountClick = { showDiscountDialog = true }
            )
        }
    }

    if (showDiscountDialog) {
        DiscountInputBar(
            currentAmount = uiState.amountInput.toDoubleOrNull() ?: 0.0,
            onDismiss = { showDiscountDialog = false },
            onConfirm = { discount ->
                viewModel.onDiscountChange(discount)
                showDiscountDialog = false
            }
        )
    }

    if (showAccountDialog) {
        AccountSelectionDialog(
            accounts = if (uiState.type == 1) uiState.accounts.filter { !it.isCredit } else uiState.accounts,
            onDismiss = { showAccountDialog = false },
            onAccountSelect = { account ->
                viewModel.onAccountChange(account.id)
                showAccountDialog = false
            }
        )
    }

    if (showDatePicker) {
        DateTimePickerDialog(
            initialMillis = uiState.selectedDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { timestamp ->
                viewModel.onDateChange(timestamp)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.Black else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 4.dp), // Reduced vertical padding
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) PrimaryYellow else Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategoryGrid(
    selectedCategory: Category?,
    onCategorySelect: (Category) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<Category>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(categories) { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onCategorySelect(category) }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedCategory?.id == category.id) PrimaryYellow else Color(0xFFF0F0F0) // Light Gray for unselected
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder for Icon. Using First letter if icon not mapped, or specific icons if available
                    // For now using Text or basic Material Icons if possible.
                    // Simulating icons with Text first char for robustness
                    Text(
                        text = category.name.take(1),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Always black as per request
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black // Always black as per request
                )
            }
        }
    }
}

@Composable
fun InputArea(
    amount: String,
    note: String,
    onNoteChange: (String) -> Unit,
    type: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = note,
            onValueChange = onNoteChange,
            textStyle = MaterialTheme.typography.bodyMedium,
            decorationBox = { innerTextField ->
                if (note.isEmpty()) {
                    Text("点击填写备注", color = Color.Gray.copy(alpha = 0.5f))
                }
                innerTextField()
            },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = if (type == 0) ExpenseRed else IncomeGreen
        )
        Text(
            text = " ¥",
            style = MaterialTheme.typography.titleMedium,
            color = if (type == 0) ExpenseRed else IncomeGreen
        )
    }
}

@Composable
fun ChipsRow(
    uiState: AddTransactionUiState,
    onAccountClick: () -> Unit,
    onDateClick: () -> Unit,
    onDiscountClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Account Chip
        FilterChip(
            selected = false,
            onClick = onAccountClick,
            label = { Text(uiState.accounts.find { it.id == uiState.selectedAccountId }?.name ?: "无账户") },
            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black) },
            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        )
        
        // Date Chip
        FilterChip(
            selected = false,
            onClick = onDateClick,
            label = { Text(dateFormatter.format(Instant.ofEpochMilli(uiState.selectedDate))) },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black) },
            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        )

        // Discount Chip
        if (uiState.type == 0) { // Only show discount for Expense (type 0)
            FilterChip(
                selected = uiState.discount > 0,
                onClick = onDiscountClick,
                label = { Text(if (uiState.discount > 0.0) "优惠: ${uiState.discount}" else "优惠") },
                leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black) },
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onKeyPress: (String) -> Unit,
    onComplete: () -> Unit
) {
    // Custom Layout for Keypad
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0))
            .padding(8.dp)
    ) {
        // We use a Row to split Numbers (3 cols) and Ops (1 col)
        Row(Modifier.fillMaxWidth()) {
            // Main Number Pad (Col 1-3)
            Column(Modifier.weight(3f)) {
                Row(Modifier.fillMaxWidth()) {
                    KeypadButton("1", Modifier.weight(1f)) { onKeyPress("1") }
                    KeypadButton("2", Modifier.weight(1f)) { onKeyPress("2") }
                    KeypadButton("3", Modifier.weight(1f)) { onKeyPress("3") }
                }
                Row(Modifier.fillMaxWidth()) {
                    KeypadButton("4", Modifier.weight(1f)) { onKeyPress("4") }
                    KeypadButton("5", Modifier.weight(1f)) { onKeyPress("5") }
                    KeypadButton("6", Modifier.weight(1f)) { onKeyPress("6") }
                }
                Row(Modifier.fillMaxWidth()) {
                    KeypadButton("7", Modifier.weight(1f)) { onKeyPress("7") }
                    KeypadButton("8", Modifier.weight(1f)) { onKeyPress("8") }
                    KeypadButton("9", Modifier.weight(1f)) { onKeyPress("9") }
                }
                Row(Modifier.fillMaxWidth()) {
                    KeypadButton(".", Modifier.weight(1f)) { onKeyPress(".") }
                    KeypadButton("0", Modifier.weight(1f)) { onKeyPress("0") }
                    KeypadButton("=", Modifier.weight(1f)) { onKeyPress("=") } // "再记" -> "="
                }
            }
            // Ops Column (Col 4)
            Column(Modifier.weight(1f)) {
                // Row 1: DEL
                KeypadButton("DEL", Modifier.fillMaxWidth()) { onKeyPress("DEL") }
                
                // Row 2: + / - (Split vertically in one standard row height? No, user said "shrink". 
                // Let's put them side-by-side or stacked?
                // If we stack them, they fit in one "KeypadButton" height? No, that's too small.
                // If we want to fit 4 logical rows in this column, we need 4 buttons.
                // But we have +, -, x, /, Complete. That's 5 actions.
                // Wait, "Shrink + - x /".
                // Maybe they share rows?
                // Row 2: + and -
                Row(Modifier.fillMaxWidth()) {
                    KeypadButton("+", Modifier.weight(1f), fontSize = 18.sp) { onKeyPress("+") }
                    KeypadButton("-", Modifier.weight(1f), fontSize = 18.sp) { onKeyPress("-") }
                }
                // Row 3: x and /
                Row(Modifier.fillMaxWidth()) {
                    KeypadButton("×", Modifier.weight(1f), fontSize = 18.sp) { onKeyPress("×") }
                    KeypadButton("÷", Modifier.weight(1f), fontSize = 18.sp) { onKeyPress("÷") }
                }
                
                // Row 4: Complete
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .height(50.dp), // Match KeypadButton height
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp) // Minimize padding to fit text
                ) {
                    Text("完成", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .height(50.dp), // Fixed height
        contentAlignment = Alignment.Center
    ) {
        if (text == "DEL") {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = Color.Black)
        } else {
            Text(text = text, fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun AccountSelectionDialog(
    accounts: List<com.afei.boxyledger.data.model.Account>,
    onDismiss: () -> Unit,
    onAccountSelect: (com.afei.boxyledger.data.model.Account) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择账户") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
            ) {
                items(accounts) { account ->
                    Card(
                        onClick = { onAccountSelect(account) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(account.name, fontWeight = FontWeight.Bold)
                            Text("¥${String.format("%.2f", account.balance)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.Black)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    // Phase 1: Date Picker
    // Phase 2: Time Picker (Simplified as just one combined flow not supported natively)
    // We will use DatePickerState and standard DatePickerDialog then custom TimePicker or two dialogs?
    // Let's implement a DatePicker first. If user confirms, we show TimePicker?
    // Or just a standard DatePicker for now as requested "Date Selection". 
    // User said "Date + Time".
    // Let's do: DatePicker -> onConfirm -> Show TimePicker.
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    var showTimePicker by remember { mutableStateOf(false) }
    
    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) {
                    Text("下一步", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = Color.Black)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        // Time Picker Logic
        // Compose Material3 TimePicker is available in 1.2.0+ 
        // Assuming we have it.
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = initialMillis
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(java.util.Calendar.MINUTE)
        )
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("选择时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis ?: initialMillis
                        val c = java.util.Calendar.getInstance()
                        c.timeInMillis = selectedDateMillis
                        c.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        c.set(java.util.Calendar.MINUTE, timePickerState.minute)
                        onConfirm(c.timeInMillis)
                    }
                ) {
                    Text("确定", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("上一步", color = Color.Black)
                }
            }
        )
    }
}
@Composable
fun DiscountInputBar(
    currentAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Bottom Sheet look-alike overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .clickable(enabled = false) {} // Consume clicks
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { 
                        text = it
                        error = null
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text("请输入折扣金额", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
                
                Button(
                    onClick = {
                        val discount = text.toDoubleOrNull()
                        if (discount == null) {
                            error = "无效"
                        } else if (discount > currentAmount) {
                            error = "超额"
                        } else {
                            onConfirm(discount)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("完成")
                }
            }
            if (error != null) {
                Text(
                    text = error!!, 
                    color = MaterialTheme.colorScheme.error, 
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // Safe area or keyboard spacing
        }
    }
}
