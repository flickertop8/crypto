package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppLockOverlay
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MarketViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MarketViewModel = viewModel()) {
    val context = LocalContext.current

    // Request POST_NOTIFICATIONS permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val currentTab by viewModel.currentTab.collectAsState()
    val securityState by viewModel.securityState.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val cashBalance by viewModel.cashBalance.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var activeDetailAssetId by remember { mutableStateOf<String?>(null) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUiMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 70.dp)
                ) { data ->
                    Snackbar(
                        containerColor = DarkSurfaceVariant,
                        contentColor = TextPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(data.visuals.message, fontWeight = FontWeight.Medium)
                    }
                }
            },
            bottomBar = {
                if (activeDetailAssetId == null) {
                    AppBottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            activeDetailAssetId = null
                            viewModel.selectTab(tab)
                        },
                        unreadNotificationCount = unreadCount
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (activeDetailAssetId != null) {
                    AssetDetailScreen(
                        assetId = activeDetailAssetId!!,
                        viewModel = viewModel,
                        onBackClick = { activeDetailAssetId = null },
                        onOpenWallet = { showWalletDialog = true }
                    )
                } else {
                    when (currentTab) {
                        AppTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { assetId -> activeDetailAssetId = assetId },
                                onNavigateToPortfolio = { viewModel.selectTab(AppTab.PORTFOLIO) },
                                onNavigateToMarket = { viewModel.selectTab(AppTab.MARKET) },
                                onOpenProfile = { showProfileDialog = true },
                                onOpenWallet = { showWalletDialog = true }
                            )
                        }
                        AppTab.MARKET -> {
                            MarketScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { assetId -> activeDetailAssetId = assetId },
                                onOpenProfile = { showProfileDialog = true },
                                onOpenWallet = { showWalletDialog = true }
                            )
                        }
                        AppTab.PORTFOLIO -> {
                            PortfolioScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { assetId -> activeDetailAssetId = assetId },
                                onOpenProfile = { showProfileDialog = true },
                                onOpenWallet = { showWalletDialog = true }
                            )
                        }
                        AppTab.NOTIFICATIONS -> {
                            NotificationsScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { assetId -> activeDetailAssetId = assetId },
                                onOpenProfile = { showProfileDialog = true },
                                onOpenWallet = { showWalletDialog = true }
                            )
                        }
                        AppTab.MORE -> {
                            SecuritySettingsScreen(
                                viewModel = viewModel,
                                onOpenProfile = { showProfileDialog = true },
                                onOpenWallet = { showWalletDialog = true }
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen App PIN Lock Overlay
        if (securityState.isAppLocked) {
            AppLockOverlay(
                onUnlockSuccess = { pin -> viewModel.unlockApp(pin) },
                onBiometricUnlock = { viewModel.unlockApp("1234") }
            )
        }

        // Wallet Quick Dialog
        if (showWalletDialog) {
            WalletDialog(
                cashBalance = cashBalance,
                currency = currency,
                onDismiss = { showWalletDialog = false },
                onDeposit = {
                    viewModel.depositFunds(25000.0)
                    showWalletDialog = false
                }
            )
        }

        // Profile Quick Dialog
        if (showProfileDialog) {
            ProfileDialog(
                securityPin = securityState.pinHash,
                onDismiss = { showProfileDialog = false },
                onLockApp = {
                    showProfileDialog = false
                    viewModel.lockApp()
                }
            )
        }
    }
}

@Composable
fun WalletDialog(
    cashBalance: Double,
    currency: String,
    onDismiss: () -> Unit,
    onDeposit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ট্রেডিং ওয়ালেট", color = TextPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceVariant)
                        .padding(16.dp)
                ) {
                    Column {
                        Text("বর্তমান ব্যবহারযোগ্য ক্যাশ ব্যালেন্স:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$currency${String.format("%,.2f", cashBalance)}",
                            color = NeonGreenLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "এই ব্যালেন্স দিয়ে আপনি তাত্ক্ষণিকভাবে যেকোনো স্টক বা ক্রিপ্টোকারেন্সি ট্রেড করতে পারবেন।",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDeposit,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("+২৫,০০০ জমা করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TextSecondary)
            }
        }
    )
}

@Composable
fun ProfileDialog(
    securityPin: String,
    onDismiss: () -> Unit,
    onLockApp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("প্রোফাইল ও নিরাপত্তা", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ব্যবহারকারী: Zahidul Islam", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("স্ট্যাটাস: ভেরিফাইড ট্রেডার (Verified)", color = NeonGreenLight, fontSize = 12.sp)
                Text("সিকিউরিটি পিন: $securityPin", color = TextSecondary, fontSize = 12.sp)
                Text("দ্বি-স্তরীয় যাচাইকরণ (2FA): সক্রিয়", color = ElectricBlue, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onLockApp,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("এখনই অ্যাপ লক করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ঠিক আছে", color = TextSecondary)
            }
        }
    )
}

// Keep Greeting composable so existing tests remain valid
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0E14)
@Composable
fun MainAppPreview() {
    MyApplicationTheme {
        com.example.ui.screens.HomeScreenPreview()
    }
}
