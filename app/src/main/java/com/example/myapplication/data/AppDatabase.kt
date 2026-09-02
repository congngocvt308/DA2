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
        QRCodeEntity::class, AlarmQRLinkEntity::class,
        UserStatsEntity::class,
        // --- 2 ENTITIES MỚI ---
        AlarmFaceMissionConfigEntity::class,
        PictureEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS qr_codes (
                        qrId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        codeValue TEXT NOT NULL,
                        codeType TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS alarm_qr_link (
                        alarmId INTEGER NOT NULL,
                        qrId INTEGER NOT NULL,
                        PRIMARY KEY(alarmId, qrId),
                        FOREIGN KEY(alarmId) REFERENCES alarms(alarmId) ON DELETE CASCADE,
                        FOREIGN KEY(qrId) REFERENCES qr_codes(qrId) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_alarm_qr_link_alarmId ON alarm_qr_link(alarmId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_alarm_qr_link_qrId ON alarm_qr_link(qrId)")
            }
        }

        // MIGRATION TỪ VERSION 3 SANG 4 (Đã bỏ FK User)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Tạo bảng alarm_face_mission_config
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `alarm_face_mission_config` (
                        `configId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `alarmId` INTEGER NOT NULL,
                        `backgroundImageUri` TEXT,
                        `eyeOpenSoundUri` TEXT,
                        `smileSoundUri` TEXT,
                        `comboSoundUri` TEXT,
                        `holdPoseSoundUri` TEXT,
                        `requiredHoldDurationSec` INTEGER NOT NULL DEFAULT 3,
                        FOREIGN KEY(`alarmId`) REFERENCES `alarms`(`alarmId`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_alarm_face_mission_config_alarmId` ON `alarm_face_mission_config` (`alarmId`)")

                // 2. Tạo bảng pictures (Chỉ giữ FK tới alarm_history)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pictures` (
                        `pictureId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL DEFAULT 1,
                        `alarmHistoryId` INTEGER,
                        `imageUri` TEXT NOT NULL,
                        `description` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`alarmHistoryId`) REFERENCES `alarm_history`(`historyId`) ON DELETE SET NULL
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_pictures_userId` ON `pictures` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_pictures_alarmHistoryId` ON `pictures` (`alarmHistoryId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_3_4)
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}