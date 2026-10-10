package com.cong.smartalarm

import android.app.Application
import com.cong.smartalarm.core.di.AppContainer

/**
 * Lịch báo thức được đặt lại bởi RescheduleReceiver (khởi động máy / cập nhật app), không đặt lại
 * mỗi lần mở app: báo lại (snooze) dùng chung requestCode với lịch chính nên sẽ bị ghi đè mất.
 */
class SmartAlarmApp : Application() {

    /** Phụ thuộc dùng chung của cả app (database, mạng, repository). Lấy qua `context.appContainer`. */
    val container: AppContainer by lazy { AppContainer(this) }
}
