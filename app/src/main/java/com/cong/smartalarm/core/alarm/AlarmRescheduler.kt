package com.cong.smartalarm.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cong.smartalarm.core.di.appContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Đặt lại lịch cho mọi báo thức đang bật.
 * AlarmManager mất hết lịch khi khởi động lại máy, và lịch cũ trỏ tới tên lớp Receiver cũ
 * sau khi cập nhật app — nên gọi lại khi máy khởi động và khi app vừa được cập nhật.
 * Không gọi mỗi lần mở app: cùng requestCode nên sẽ ghi đè lịch báo lại (snooze) đang chờ.
 */
object AlarmRescheduler {
    suspend fun rescheduleAll(context: Context) {
        val alarms = context.appContainer.alarmRepository
        val scheduler = context.appContainer.alarmScheduler
        alarms.getEnabledAlarms().forEach { scheduler.schedule(it) }
    }
}

/** Nhận BOOT_COMPLETED và MY_PACKAGE_REPLACED để đặt lại lịch báo thức. */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AlarmRescheduler.rescheduleAll(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
