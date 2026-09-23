package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AlertEntity
import com.example.data.db.NotificationEntity
import com.example.data.model.NotificationType
import com.example.ui.components.MarketTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: MarketViewModel,
    onNavigateToDetail: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "ALERTS" -> notifications.filter { it.type == NotificationType.PRICE_ALERT.name }
            "PORTFOLIO" -> notifications.filter { it.type == NotificationType.PORTFOLIO_UPDATE.name || it.type == NotificationType.TRANSACTION.name }
            "SECURITY" -> notifications.filter { it.type == NotificationType.SECURITY_ALERT.name }
            else -> notifications
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            MarketTopBar(
                onAvatarClick = onOpenProfile,
                onWalletClick = onOpenWallet,
                formattedDateTime = "11 Dec 2021\n4:40 PM"
            )
        }

        // Title Row & Mark All Read
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "পুশ নোটিফিকেশন সেন্টার",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ElectricBlue)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$unreadCount টি নতুন",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { viewModel.markAllNotificationsRead() },
                    modifier = Modifier.testTag("mark_all_read_button")
                ) {
                    Text("সব পঠিত", color = ElectricBlue, fontSize = 12.sp)
                }
            }
        }

        // Test Push Notification Trigger Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "লাইভ পুশ নোটিফিকেশন টেস্ট",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ডিভাইসের সিস্টেম নোটিফিকেশন বারে টেস্ট এলার্ট পাঠান",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerTestNotification() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("trigger_test_notification")
                    ) {
                        Text("টেস্ট পাঠান", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabChip("সকল", selectedFilter == "ALL") { selectedFilter = "ALL" }
                FilterTabChip("প্রাইস অ্যালার্ট", selectedFilter == "ALERTS") { selectedFilter = "ALERTS" }
                FilterTabChip("ট্রেডিং", selectedFilter == "PORTFOLIO") { selectedFilter = "PORTFOLIO" }
                FilterTabChip("নিরাপত্তা", selectedFilter == "SECURITY") { selectedFilter = "SECURITY" }
            }
        }

        // Active Price Alerts Section
        if (alerts.isNotEmpty()) {
            item {
                Text(
                    text = "সক্রিয় প্রাইস অ্যালার্ট (${alerts.size})",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            items(alerts, key = { it.id }) { alert ->
                ActiveAlertCard(
                    alert = alert,
                    onDelete = { viewModel.deletePriceAlert(alert.id) }
                )
            }
        }

        // Notification List Section
        item {
            Text(
                text = "নোটিফিকেশন হিস্ট্রি",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        if (filteredNotifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোনো নোটিফিকেশন পাওয়া যায়নি",
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredNotifications, key = { it.id }) { notif ->
                NotificationItemCard(
                    notification = notif,
                    onClick = {
                        viewModel.markNotificationAsRead(notif.id)
                        notif.targetAssetId?.let { onNavigateToDetail(it) }
                    }
                )
            }
        }
    }
}

@Composable
fun FilterTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ElectricBlue else DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun ActiveAlertCard(
    alert: AlertEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alert Active",
                    tint = NeonGreenLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${alert.symbol} (${alert.name})",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (alert.isAbove) "লক্ষ্যমাত্রা: >= ${alert.currency}${alert.targetPrice}" else "লক্ষ্যমাত্রা: <= ${alert.currency}${alert.targetPrice}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = CrimsonRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: NotificationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notification.timestamp))

    val iconColor = when (notification.type) {
        NotificationType.PRICE_ALERT.name -> NeonGreenLight
        NotificationType.SECURITY_ALERT.name -> CrimsonRed
        NotificationType.TRANSACTION.name -> ElectricBlue
        else -> NeonCyan
    }

    val iconVector = when (notification.type) {
        NotificationType.PRICE_ALERT.name -> Icons.Default.TrendingUp
        NotificationType.SECURITY_ALERT.name -> Icons.Default.Security
        NotificationType.TRANSACTION.name -> Icons.Default.ReceiptLong
        else -> Icons.Default.Notifications
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (notification.isRead) DarkSurfaceVariant else DarkSurfaceVariant.copy(alpha = 0.95f))
            .border(
                1.dp,
                if (!notification.isRead) ElectricBlue.copy(alpha = 0.6f) else DarkBorderSubtle,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.bengaliTitle,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold
                    )
                    Text(
                        text = timeFormatted,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.bengaliMessage,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
