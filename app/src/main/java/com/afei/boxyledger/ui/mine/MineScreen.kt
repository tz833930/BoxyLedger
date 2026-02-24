package com.afei.boxyledger.ui.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afei.boxyledger.ui.theme.PrimaryYellow

@Composable
fun MineScreen(
    onCategoryClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // User Profile Card
        ProfileCard()

        Spacer(modifier = Modifier.height(20.dp))

        // Settings / Data Center
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.PieChart,
                title = "预算管理",
                subtitle = "暂不开发",
                onClick = {}
            )
            Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
            SettingsItem(
                icon = Icons.Default.Category,
                title = "分类管理",
                subtitle = "管理收支分类",
                onClick = onCategoryClick
            )
            Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
            SettingsItem(
                icon = Icons.Default.Backup,
                title = "备份与恢复",
                subtitle = "导出 JSON/CSV",
                onClick = {}
            )
            Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "数据重置",
                subtitle = "清空所有数据",
                isDestructive = true,
                onClick = {}
            )
        }
    }
}

@Composable
fun ProfileCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryYellow)
            .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(40.dp),
                    tint = PrimaryYellow
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "本地账户",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "记录每一笔收支，管理美好生活",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Color.Red else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) Color.Red else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
