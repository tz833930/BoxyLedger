package com.afei.boxyledger.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afei.boxyledger.data.model.Category
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.afei.boxyledger.ui.theme.InfoBlue
import com.afei.boxyledger.ui.theme.PrimaryYellow

import com.afei.boxyledger.ui.utils.IconMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onBackClick: () -> Unit,
    viewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.Factory)
) {
    val selectedType by viewModel.selectedType.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    
    val currentCategories = if (selectedType == 0) expenseCategories else incomeCategories
    
    var showEditDialog by remember { mutableStateOf<Category?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryYellow,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Type Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                horizontalArrangement = Arrangement.Center
            ) {
                TabButton(text = "支出", selected = selectedType == 0) { viewModel.setType(0) }
                TabButton(text = "收入", selected = selectedType == 1) { viewModel.setType(1) }
            }

            // Grid List
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentCategories, key = { it.id }) { category ->
                    CategoryItem(
                        category = category,
                        onLongClick = { showEditDialog = category }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        CategoryDialog(
            title = "添加分类",
            initialName = "",
            initialIcon = "Star", // Default
            onDismiss = { showAddDialog = false },
            onConfirm = { name, icon ->
                viewModel.addCategory(name, icon, selectedType)
                showAddDialog = false
            }
        )
    }

    if (showEditDialog != null) {
        CategoryDialog(
            title = "修改分类",
            initialName = showEditDialog!!.name,
            initialIcon = showEditDialog!!.icon,
            onDismiss = { showEditDialog = null },
            onConfirm = { name, icon ->
                viewModel.updateCategory(showEditDialog!!.copy(name = name, icon = icon))
                showEditDialog = null
            },
            onDelete = {
                viewModel.deleteCategory(showEditDialog!!)
                showEditDialog = null
            }
        )
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.Black else Color.Gray
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { /* Haptic feedback? */ },
                    onDrag = { _, _ -> /* Drag logic placeholder */ },
                    onDragEnd = { /* Drop logic placeholder */ }
                )
            }
            .clickable { onLongClick() }, // Simplifying to click for edit as requested
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp) // Larger touch target
                .clip(RoundedCornerShape(16.dp)) // Rounded square like iOS/Image
                .background(Color.White), // White background for the icon container
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconMapper.getIcon(category.icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (category.type == 0) Color(0xFFFFB74D) else Color(0xFF4CAF50) // Use color based on type or just category color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun CategoryDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color.Black) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
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
                
                Box(modifier = Modifier.height(200.dp)) {
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
            TextButton(onClick = { onConfirm(name, selectedIcon) }) {
                Text("确定", color = Color.Black)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消", color = Color.Black)
                }
            }
        }
    )
}
