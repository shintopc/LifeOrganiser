package com.shintopc.lifeorganiser.di

import android.content.Context
import com.shintopc.lifeorganiser.data.local.*
import com.shintopc.lifeorganiser.data.repository.*
import com.shintopc.lifeorganiser.data.security.CryptoManager
import com.shintopc.lifeorganiser.data.security.SecurityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideFinanceDao(db: AppDatabase): FinanceDao = db.financeDao()

    @Provides
    fun provideFinanceDebtDao(db: AppDatabase): FinanceDebtDao = db.financeDebtDao()

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun providePasswordDao(db: AppDatabase): PasswordDao = db.passwordDao()

    @Provides
    fun provideDocumentDao(db: AppDatabase): DocumentDao = db.documentDao()

    @Provides
    fun provideEmergencyContactDao(db: AppDatabase): EmergencyContactDao = db.emergencyContactDao()

    @Provides
    fun provideFinanceBudgetDao(db: AppDatabase): FinanceBudgetDao = db.financeBudgetDao()

    @Provides
    fun provideLoanDao(db: AppDatabase): LoanDao = db.loanDao()

    @Provides
    fun provideTaskCategoryDao(db: AppDatabase): TaskCategoryDao = db.taskCategoryDao()

    @Provides
    fun provideSubTaskDao(db: AppDatabase): SubTaskDao = db.subTaskDao()

    @Provides
    @Singleton
    fun provideCryptoManager(): CryptoManager {
        return CryptoManager()
    }

    @Provides
    @Singleton
    fun provideSecurityManager(@ApplicationContext context: Context): SecurityManager {
        return SecurityManager(context)
    }
}
