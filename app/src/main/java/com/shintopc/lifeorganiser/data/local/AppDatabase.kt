package com.shintopc.lifeorganiser.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.TypeConverters

@Database(entities = [
    DocumentEntity::class, 
    PasswordEntity::class, 
    ReminderEntity::class,
    FinanceEntry::class,
    FinanceCategory::class,
    FinanceDebt::class,
    NoteEntity::class,
    EmergencyContactEntity::class,
    TaskEntity::class,

    FinanceBudget::class,
    LoanEntry::class,
    TaskCategory::class,
    SubTask::class
], version = 19, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun passwordDao(): PasswordDao
    abstract fun reminderDao(): ReminderDao
    abstract fun financeDao(): FinanceDao
    abstract fun financeDebtDao(): FinanceDebtDao
    abstract fun noteDao(): NoteDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun taskDao(): TaskDao
    abstract fun financeBudgetDao(): FinanceBudgetDao
    abstract fun loanDao(): LoanDao
    abstract fun taskCategoryDao(): TaskCategoryDao
    abstract fun subTaskDao(): SubTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_15_16 = object : androidx.room.migration.Migration(15, 16) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Create the new table with the foreign key constraint
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tasks_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `description` TEXT NOT NULL, 
                        `priority` TEXT NOT NULL, 
                        `isCompleted` INTEGER NOT NULL, 
                        `dueDate` INTEGER, 
                        `dueTime` INTEGER, 
                        `repeatMode` TEXT NOT NULL, 
                        `isReminderOn` INTEGER NOT NULL, 
                        `categoryId` INTEGER,
                        FOREIGN KEY(`categoryId`) REFERENCES `task_categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """)

                // 2. Copy data from the old table to the new table
                database.execSQL("""
                    INSERT INTO tasks_new (id, title, description, priority, isCompleted, dueDate, dueTime, repeatMode, isReminderOn, categoryId)
                    SELECT id, title, description, priority, isCompleted, dueDate, dueTime, repeatMode, isReminderOn, categoryId FROM tasks
                """)

                // 3. Remove the old table
                database.execSQL("DROP TABLE tasks")

                // 4. Rename the new table to the correct name
                database.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            }
        }

        private val MIGRATION_16_17 = object : androidx.room.migration.Migration(16, 17) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_17_18 = object : androidx.room.migration.Migration(17, 18) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE loan_entries ADD COLUMN reminderOffset INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        private val MIGRATION_18_19 = object : androidx.room.migration.Migration(18, 19) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN isAlarm INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE finance_debts ADD COLUMN reminderType TEXT NOT NULL DEFAULT 'NOTIFICATION'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_organiser_db"
                )
                .addMigrations(MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19)
                // .fallbackToDestructiveMigration() // Disabled for data safety
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
