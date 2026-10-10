package com.cong.smartalarm

import android.app.Application
import com.cong.smartalarm.core.alarm.AlarmRescheduler
import com.cong.smartalarm.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartAlarmApp : Application() {

    /** Phụ thuộc dùng chung của cả app (database, mạng, repository). Lấy qua `context.appContainer`. */
    val container: AppContainer by lazy { AppContainer(this) }

    /** Scope sống cùng tiến trình app, cho các việc nền ngắn (không gắn với màn hình nào). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Bảo đảm lịch báo thức luôn khớp với dữ liệu (sau khi cập nhật app, đổi giờ hệ thống…)
        appScope.launch(Dispatchers.IO) { AlarmRescheduler.rescheduleAll(this@SmartAlarmApp) }
    }
}
