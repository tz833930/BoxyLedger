package com.afei.boxyledger.ui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afei.boxyledger.data.model.Account
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.afei.boxyledger.ui.utils.IconMapper
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import com.afei.boxyledger.ui.theme.GradientYellowStart
import com.afei.boxyledger.ui.theme.GradientYellowEnd
import com.afei.boxyledger.ui.theme.InfoBlue
import com.afei.boxyledger.ui.theme.PrimaryYellow
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.CreditCard
import kotlin.math.abs

@Composable
fun AccountScreen(
    viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // State for Add/Edit Account Dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditAccountDialog by remember { mutableStateOf<Account?>(null) }
    var showEditTypeDialog by remember { mutableStateOf<String?>(null) }
    var showRepaymentDialog by remember { mutableStateOf(false) }
    var initialType by remember { mutableStateOf("") }

    if (showAddDialog) {
        AddAccountDialog(
            initialType = initialType,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, balance, icon, isCredit ->
                viewModel.addAccount(name, type, balance, icon, isCredit)
                showAddDialog = false
            }
        )
    }
    
    // Reuse AddAccountDialog for Editing
    if (showEditAccountDialog != null) {
        val account = showEditAccountDialog!!
        AddAccountDialog(
            initialType = account.type, // Lock type
            initialName = account.name,
            initialBalance = account.balance.toString(),
            initialIcon = account.icon,
            initialIsCredit = account.isCredit,
            isEditMode = true,
            onDismiss = { showEditAccountDialog = null },
            onConfirm = { name, type, balance, icon, isCredit ->
                // Update account logic
                viewModel.updateAccount(account.copy(name = name, type = type, balance = balance, icon = icon, isCredit = isCredit))
                showEditAccountDialog = null
            }
        )
    }

    if (showEditTypeDialog != null) {
        EditAccountTypeDialog(
            oldType = showEditTypeDialog!!,
            onDismiss = { showEditTypeDialog = null },
            onConfirm = { newType ->
                viewModel.updateAccountType(showEditTypeDialog!!, newType)
                showEditTypeDialog = null
            }
        )
    }
    
    if (showRepaymentDialog) {
        RepaymentDialog(
            accounts = uiState.accounts,
            onDismiss = { showRepaymentDialog = false },
            onConfirm = { creditId, payId, amount ->
                viewModel.repayCreditAccount(creditId, payId, amount)
                showRepaymentDialog = false
            }
        )
    }
    
    // Group accounts by type
    val groupedAccounts = remember(uiState.accounts) {
        uiState.accounts.groupBy { it.type }
    }
    
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showFabMenu) {
                     // Repayment Option
                     SmallFloatingActionButton(
                         onClick = {
                             showRepaymentDialog = true
                             showFabMenu = false
                         },
                         containerColor = MaterialTheme.colorScheme.surface,
                         contentColor = InfoBlue,
                         modifier = Modifier.padding(bottom = 12.dp)
                     ) {
                         Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text("还款", style = MaterialTheme.typography.labelLarge)
                         }
                     }
                     
                     // New Account Option
                     SmallFloatingActionButton(
                         onClick = {
                             initialType = "" // Level 1
                             showAddDialog = true
                             showFabMenu = false
                         },
                         containerColor = MaterialTheme.colorScheme.surface,
                         contentColor = PrimaryYellow, // Or dark
                         modifier = Modifier.padding(bottom = 12.dp)
                     ) {
                         Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text("新建账户", style = MaterialTheme.typography.labelLarge)
                         }
                     }
                }
                
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = PrimaryYellow,
                    contentColor = Color.Black
                ) {
                    Icon(if (showFabMenu) Icons.Default.ExpandMore else Icons.Default.Add, contentDescription = "操作菜单")
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
            // Total Assets Card
            TotalAssetsCard(netAssets = uiState.totalAssets, liabilities = uiState.totalLiabilities)

            // Grouped Account List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedAccounts.forEach { (type, accounts) ->
                    item {
                        AccountGroupItem(
                            type = type,
                            accounts = accounts,
                            onAddAccountClick = { 
                                initialType = type // Level 2: Pre-fill type
                                showAddDialog = true 
                            },
                            onEditType = { showEditTypeDialog = type },
                            onEditAccount = { account -> showEditAccountDialog = account },
                            onDeleteAccount = { account -> viewModel.deleteAccount(account) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccountDialog(
    initialType: String = "",
    initialName: String = "",
    initialBalance: String = "",
    initialIcon: String = "AccountBalanceWallet",
    initialIsCredit: Boolean = false,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Boolean) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var name by remember { mutableStateOf(initialName) }
    var balance by remember { mutableStateOf(if (initialBalance.isNotEmpty()) abs(initialBalance.toDoubleOrNull() ?: 0.0).toString() else "") }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var isCredit by remember { mutableStateOf(initialIsCredit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (isEditMode) "修改账户" else if (initialType.isEmpty()) "新增一级账户" else "新增账户", 
                color = Color.Black
            ) 
        },
        text = {
            Column {
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("账户类型 (如: 现金, 银行卡)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = initialType.isEmpty(), // Lock type if adding to existing group
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InfoBlue,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账户名称 (如: 钱包, 招商银行)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InfoBlue,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isCredit, 
                        onCheckedChange = { isCredit = it },
                        colors = CheckboxDefaults.colors(checkedColor = InfoBlue)
                    )
                    Text("信用账户 (负债)", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text(if (isCredit) "当前欠款 (正数)" else "初始余额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InfoBlue,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("选择图标:", style = MaterialTheme.typography.labelLarge, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.height(150.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(IconMapper.availableIcons) { iconName ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedIcon == iconName) InfoBlue.copy(alpha = 0.3f) else Color.Transparent)
                                    .clickable { selectedIcon = iconName }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconMapper.getIcon(iconName),
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balanceVal = balance.toDoubleOrNull() ?: 0.0
                    // If credit, convert to negative
                    val finalBalance = if (isCredit) -abs(balanceVal) else balanceVal
                    if (type.isNotBlank() && name.isNotBlank()) {
                        onConfirm(name, type, finalBalance, selectedIcon, isCredit)
                    }
                }
            ) {
                Text("确定", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.Black)
            }
        }
    )
}

@Composable
fun EditAccountTypeDialog(
    oldType: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newType by remember { mutableStateOf(oldType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改账户分类", color = Color.Black) },
        text = {
            OutlinedTextField(
                value = newType,
                onValueChange = { newType = it },
                label = { Text("分类名称") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InfoBlue,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newType.isNotBlank() && newType != oldType) {
                        onConfirm(newType)
                    }
                }
            ) {
                Text("保存", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.Black)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountGroupItem(
    type: String,
    accounts: List<Account>,
    onAddAccountClick: () -> Unit,
    onEditType: () -> Unit,
    onEditAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    
    val totalBalance = accounts.sumOf { it.balance }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.animateContentSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { expanded = !expanded },
                        onLongClick = onEditType
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        modifier = Modifier.rotate(rotationState)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${accounts.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = String.format("%.2f", totalBalance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Expanded Content
            if (expanded) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    accounts.forEach { account ->
                        SwipeableAccountItem(
                            account = account,
                            onEdit = { onEditAccount(account) },
                            onDelete = { onDeleteAccount(account) }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    
                    // Add Button - Reduced blank space
                    TextButton(
                        onClick = onAddAccountClick,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加账户", color = InfoBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableAccountItem(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Swipe Logic
    val density = LocalDensity.current
    val actionWidth = 140.dp // 70dp * 2
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp), // FIX: Reduced height
        contentAlignment = Alignment.CenterEnd
    ) {
        // Background Actions (Reveal)
        Row(
            modifier = Modifier
                .width(actionWidth)
                .fillMaxHeight(),
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
                    .background(androidx.compose.ui.graphics.Color.Red)
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
                .fillMaxHeight() // FIX: Fill Height
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
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Account Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if(account.isCredit) Color.Gray.copy(alpha=0.2f) else PrimaryYellow.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconMapper.getIcon(account.icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(account.name, style = MaterialTheme.typography.bodyLarge)
                        if (account.isCredit) {
                            Text("信用账户", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
                Text(
                    text = String.format("%.2f", account.balance),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (account.balance < 0) Color.Red else Color.Black
                )
            }
        }
    }
}

@Composable
fun AutoResizedText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    var resizedTextStyle by remember { mutableStateOf(style) }

    // Reset font size if text changes
    LaunchedEffect(text) {
        resizedTextStyle = style
    }

    Text(
        text = text,
        color = color,
        modifier = modifier,
        style = resizedTextStyle,
        softWrap = false,
        onTextLayout = { result ->
            if (result.didOverflowWidth) {
                if (resizedTextStyle.fontSize.value > 12) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = resizedTextStyle.fontSize * 0.9f
                    )
                }
            }
        }
    )
}

@Composable
fun TotalAssetsCard(netAssets: Double, liabilities: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
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
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Title Top-Left
                Text(
                    text = "资产",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Net Assets Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "净资产",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        AutoResizedText(
                            text = String.format("%.2f", netAssets),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Liabilities Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "负债",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("%.2f", liabilities),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (liabilities > 0) Color.Red.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun RepaymentDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Double) -> Unit // creditAccountId, paymentAccountId, amount
) {
    val creditAccounts = accounts.filter { it.isCredit }
    val paymentAccounts = accounts.filter { !it.isCredit }
    
    var selectedCreditAccount by remember { mutableStateOf(creditAccounts.firstOrNull()) }
    var selectedPaymentAccount by remember { mutableStateOf(paymentAccounts.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto-fill amount when credit account changes
    LaunchedEffect(selectedCreditAccount) {
        if (selectedCreditAccount != null) {
            val debt = abs(selectedCreditAccount!!.balance)
            if (debt > 0) {
                amount = String.format("%.2f", debt)
            } else {
                amount = ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("还款", color = Color.Black) },
        text = {
            if (creditAccounts.isEmpty()) {
                Text("没有信用账户可还款", color = Color.Gray)
            } else if (paymentAccounts.isEmpty()) {
                Text("没有可用资金账户", color = Color.Gray)
            } else {
                Column {
                    // Credit Account Selector
                    Text("还款账户 (负债):", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    AccountSelector(
                        accounts = creditAccounts,
                        selected = selectedCreditAccount,
                        onSelect = { selectedCreditAccount = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Payment Account Selector
                    Text("付款账户:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    AccountSelector(
                        accounts = paymentAccounts,
                        selected = selectedPaymentAccount,
                        onSelect = { selectedPaymentAccount = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it 
                            errorMessage = null
                        },
                        label = { Text("还款金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null,
                        supportingText = {
                            if (errorMessage != null) {
                                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InfoBlue,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (creditAccounts.isNotEmpty() && paymentAccounts.isNotEmpty()) {
                TextButton(
                    onClick = {
                        val amountVal = amount.toDoubleOrNull() ?: 0.0
                        if (amountVal <= 0) {
                            errorMessage = "请输入有效金额"
                            return@TextButton
                        }
                        if (selectedPaymentAccount != null && selectedPaymentAccount!!.balance < amountVal) {
                            errorMessage = "余额不足"
                            return@TextButton
                        }
                        if (selectedCreditAccount != null && selectedPaymentAccount != null) {
                            onConfirm(selectedCreditAccount!!.id, selectedPaymentAccount!!.id, amountVal)
                        }
                    }
                ) {
                    Text("确认还款", color = Color.Black)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.Black)
            }
        }
    )
}

@Composable
fun AccountSelector(
    accounts: List<Account>,
    selected: Account?,
    onSelect: (Account) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.name ?: "请选择",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ExpandMore, null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false, // Disable text input but allow click
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.Gray,
                disabledLabelColor = Color.Black,
                disabledTrailingIconColor = Color.Black
            )
        )
        // Overlay clickable area because enabled=false blocks click
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { 
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(account.name)
                            Text(String.format("%.2f", account.balance), color = Color.Gray)
                        }
                    },
                    onClick = {
                        onSelect(account)
                        expanded = false
                    }
                )
            }
        }
    }
}
