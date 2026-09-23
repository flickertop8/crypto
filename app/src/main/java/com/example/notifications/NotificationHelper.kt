package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_PRICE_ALERTS = "price_alerts_channel"
        const val CHANNEL_SECURITY = "security_channel"
        const val CHANNEL_PORTFOLIO = "portfolio_channel"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val priceChannel = NotificationChannel(
                CHANNEL_PRICE_ALERTS,
                "Price Alerts (প্রাইস অ্যালার্ট)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts when stock or crypto hits target price"
                enableVibration(true)
            }

            val securityChannel = NotificationChannel(
                CHANNEL_SECURITY,
                "Security & Account (নিরাপত্তা)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Account security, PIN and 2FA notifications"
                enableVibration(true)
            }

            val portfolioChannel = NotificationChannel(
                CHANNEL_PORTFOLIO,
                "Portfolio & Trading (ট্রেডিং)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Order confirmations and portfolio updates"
            }

            notificationManager.createNotificationChannels(
                listOf(priceChannel, securityChannel, portfolioChannel)
            )
        }
    }

    fun postNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_PRICE_ALERTS,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Cannot post system notification without permission, but in-app notification is still saved
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted or restricted
        }
    }
}
