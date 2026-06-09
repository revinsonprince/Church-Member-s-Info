package com.example.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.ChurchDatabase
import com.example.data.Member
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class BirthdayReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = ChurchDatabase.getDatabase(applicationContext)
        val dao = database.churchDao()

        val members = dao.getAllMembersSync()
        val families = dao.getAllFamiliesSync()
        val today = LocalDate.now()

        var notificationId = 1000

        for (member in members) {
            // 1. Birthday Check
            val dobString = member.dateOfBirth
            if (dobString.isNotBlank()) {
                try {
                    val dob = LocalDate.parse(dobString, DateTimeFormatter.ISO_LOCAL_DATE)
                    var nextBirthday = dob.withYear(today.year)
                    if (nextBirthday.isBefore(today)) {
                        nextBirthday = nextBirthday.plusYears(1)
                    }
                    val daysUntil = ChronoUnit.DAYS.between(today, nextBirthday)
                    
                    if (daysUntil == 1L) {
                        sendNotification(member.fullName, "Birthday", notificationId++)
                    }
                } catch (e: DateTimeParseException) {
                    // Ignore invalid dates
                }
            }
        }

        for (family in families) {
            // 2. Wedding Date Check
            val weddingString = family.weddingDate
            if (!weddingString.isNullOrBlank()) {
                try {
                    val wedding = LocalDate.parse(weddingString, DateTimeFormatter.ISO_LOCAL_DATE)
                    var nextAnniv = wedding.withYear(today.year)
                    if (nextAnniv.isBefore(today)) {
                        nextAnniv = nextAnniv.plusYears(1)
                    }
                    val daysUntil = ChronoUnit.DAYS.between(today, nextAnniv)
                    
                    if (daysUntil == 1L) {
                        sendNotification(family.familyName, "Wedding Anniversary", notificationId++)
                    }
                } catch (e: DateTimeParseException) {
                    // Ignore invalid dates
                }
            }
        }

        return Result.success()
    }

    private fun sendNotification(name: String, eventType: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val channelId = "anniversary_reminders"
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Anniversary Reminders", 
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming birthdays and wedding anniversaries"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext, 
            notificationId, 
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Upcoming $eventType")
            .setContentText("Tomorrow is $name's $eventType!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
