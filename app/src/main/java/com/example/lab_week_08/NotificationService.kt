package com.example.lab_week_08

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // Buat handler thread untuk menjalankan proses di background
        val handlerThread = HandlerThread("SecondThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)

        // Bangun dan tampilkan notifikasi foreground
        notificationBuilder = createForegroundNotification()
    }

    // Fungsi untuk membangun dan menjalankan foreground notification
    private fun createForegroundNotification(): NotificationCompat.Builder {
        val pendingIntent = getPendingIntent()
        val channelId = createNotificationChannel()

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second worker process is done")
            .setContentText("Check it out!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Second worker process is done, check it out!")
            .setOngoing(true)

        // Mulai service di foreground agar notifikasi muncul
        startForeground(NOTIFICATION_ID, builder.build())

        return builder
    }

    // Buat PendingIntent untuk membuka MainActivity ketika notifikasi diklik
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    // Buat notification channel (wajib untuk Android 8.0+)
    private fun createNotificationChannel(): String {
        val channelId = "lab_week_08_channel"
        val channelName = "Foreground Service Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            )!!
            notificationManager.createNotificationChannel(channel)
        }

        return channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(intent, flags, startId)

        // Gets the channel id passed from the MainActivity through the Intent
        val id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        // Jalankan proses di thread handler
        serviceHandler.post {
            // Count down 10 ke 0 di notifikasi
            countDownFromTenToZero(notificationBuilder)

            // Notifikasi ke MainActivity bahwa service selesai
            notifyCompletion(id)

            // Hentikan foreground dan service
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return returnValue
    }

    // A function to update the notification to display a count down from 10 to 0
    private fun countDownFromTenToZero(notificationBuilder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        for (i in 10 downTo 0) {
            Thread.sleep(1000L)
            notificationBuilder
                .setContentText("$i seconds until last warning")
                .setSilent(true)

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    // Update the LiveData with the returned channel id after countdown
    private fun notifyCompletion(id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = id
        }
    }

    companion object {
        const val NOTIFICATION_ID = 0xCA7
        const val EXTRA_ID = "Id"

        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
