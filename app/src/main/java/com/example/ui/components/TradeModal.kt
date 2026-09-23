package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketAsset
import com.example.data.model.TransactionType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeModal(
    asset: MarketAsset,
    type: TransactionType,
    cashBalance: Double,
    ownedShares: Double,
    onDismiss: () -> Unit,
    onConfirmTrade: (shares: Double, pin: String) -> Unit
) {
    var shareInput by remember { mutableStateOf("1") }
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val shares = shareInput.toDoubleOrNull() ?: 0.0
    val totalCost = shares * asset.currentPrice

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (type == TransactionType.BUY) "শেয়ার ক্রয় করুন (BUY)" else "শেয়ার বিক্রি করুন (SELL)",
                        color = if (type == TransactionType.BUY) NeonGreenLight else CrimsonRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${asset.bengaliName} (${asset.symbol})",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price & Balance Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("বর্তমান রেট", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "${asset.currency}${asset.currentPrice}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (type == TransactionType.BUY) "ট্রেডিং ব্যালেন্স" else "আপনার শেয়ার",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        if (type == TransactionType.BUY) "${asset.currency}${cashBalance.toInt()}" else "$ownedShares টি",
                        color = ElectricBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Shares Input
            Text("পরিমাণ / শেয়ার সংখ্যা", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = shareInput,
                onValueChange = { shareInput = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shares_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Quick increment chips (+1, +5, +10, MAX)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 5, 10, 25).forEach { qty ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { shareInput = qty.toString() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+$qty", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Amount Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("মোট খরচ / প্রাপ্তি:", color = TextSecondary, fontSize = 14.sp)
                Text(
                    "${asset.currency}${String.format("%.2f", totalCost)}",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Security PIN Input (Required for transactions!)
            Text(
                "নিরাপত্তা পিন (Security PIN - ডিফল্ট: 1234)",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = pinInput,
                onValueChange = {
                    if (it.length <= 6) pinInput = it
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "PIN",
                        tint = ElectricBlue
                    )
                },
                placeholder = { Text("৪ ডিজিটের পিন লিখুন", color = TextTertiary, fontSize = 13.sp) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = CrimsonRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (shares <= 0) {
                        errorMessage = "সঠিক পরিমাণ লিখুন"
                        return@Button
                    }
                    if (pinInput.isEmpty()) {
                        errorMessage = "নিরাপত্তা পিন আবশ্যক (ডিফল্ট: 1234)"
                        return@Button
                    }
                    onConfirmTrade(shares, pinInput)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_trade_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == TransactionType.BUY) NeonGreen else CrimsonRed
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (type == TransactionType.BUY) "ক্রয় নিশ্চিত করুন (CONFIRM BUY)" else "বিক্রয় নিশ্চিত করুন (CONFIRM SELL)",
                    color = if (type == TransactionType.BUY) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Biometric Confirm Simulation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        pinInput = "1234"
                        onConfirmTrade(shares, "1234")
                    }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometrics",
                    tint = ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "বায়োমেট্রিক ফিঙ্গারপ্রিন্ট দিয়ে কনফার্ম করুন",
                    color = ElectricBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
