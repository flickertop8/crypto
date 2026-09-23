package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab

@Composable
fun AppBottomNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NavigationBarBg)
            .border(width = 0.5.dp, color = DarkBorderSubtle)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentTab == AppTab.HOME,
                onClick = { onTabSelected(AppTab.HOME) },
                testTag = "nav_home"
            )

            NavItem(
                icon = Icons.Default.ShowChart,
                label = "Market",
                isSelected = currentTab == AppTab.MARKET,
                onClick = { onTabSelected(AppTab.MARKET) },
                testTag = "nav_market"
            )

            NavItem(
                icon = Icons.Default.PieChart,
                label = "Portfolio",
                isSelected = currentTab == AppTab.PORTFOLIO,
                onClick = { onTabSelected(AppTab.PORTFOLIO) },
                testTag = "nav_portfolio"
            )

            NavItem(
                icon = Icons.Default.Notifications,
                label = "Notifications",
                isSelected = currentTab == AppTab.NOTIFICATIONS,
                badgeCount = unreadNotificationCount,
                onClick = { onTabSelected(AppTab.NOTIFICATIONS) },
                testTag = "nav_notifications"
            )

            NavItem(
                icon = Icons.Default.MoreHoriz,
                label = "More",
                isSelected = currentTab == AppTab.MORE,
                onClick = { onTabSelected(AppTab.MORE) },
                testTag = "nav_more"
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    testTag: String = ""
) {
    if (isSelected) {
        // Active Pill matching the screenshot (Electric Blue rounded pill with white icon)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(ElectricBlue)
                .clickable(onClick = onClick)
                .testTag(testTag)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Inactive Icon with optional badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .testTag(testTag)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = ElectricBlue,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
