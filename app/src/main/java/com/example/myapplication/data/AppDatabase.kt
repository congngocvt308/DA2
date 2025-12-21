package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TopicEntity::class, QuestionEntity::class, AlarmEntity::class,
        AlarmTopicLink::class, QuestionProgressEntity::class,
        TopicStatsEntity::class, HistoryEntity::class, AlarmHistoryEntity::class,
        AlarmSelectedQuestionEntity::class,
        QRCodeEntity::class, AlarmQRLinkEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class) // Đăng ký Converter ở đây
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration từ version 1 sang 2: thêm bảng QR codes
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tạo bảng qr_codes
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS qr_codes (
                        qrId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        codeValue TEXT NOT NULL,
                        codeType TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
                
                // Tạo bảng alarm_qr_link
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS alarm_qr_link (
                        alarmId INTEGER NOT NULL,
                        qrId INTEGER NOT NULL,
                        PRIMARY KEY(alarmId, qrId),
                        FOREIGN KEY(alarmId) REFERENCES alarms(alarmId) ON DELETE CASCADE,
                        FOREIGN KEY(qrId) REFERENCES qr_codes(qrId) ON DELETE CASCADE
                    )
                """)
                
                // Tạo indices cho alarm_qr_link
                database.execSQL("CREATE INDEX IF NOT EXISTS index_alarm_qr_link_alarmId ON alarm_qr_link(alarmId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_alarm_qr_link_qrId ON alarm_qr_link(qrId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}