package com.shintopc.lifeorganiser.data

import android.content.Context
import android.content.SharedPreferences
import com.shintopc.lifeorganiser.data.local.*
import com.shintopc.lifeorganiser.data.model.Task
import com.shintopc.lifeorganiser.data.model.TaskPriority
import com.shintopc.lifeorganiser.data.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tasksRepository: TasksRepository,
    private val notesRepository: NotesRepository,
    private val remindersRepository: RemindersRepository,
    private val financeRepository: FinanceRepository,
    private val documentsRepository: DocumentsRepository,
    private val passwordsRepository: PasswordsRepository,
    private val emergencyRepository: EmergencyRepository,
    private val loanRepository: LoanRepository
) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
    }

    private val KEY_FIRST_INSTALL = "is_first_install"

    fun initialize() {
        val isFirstInstall = prefs.getBoolean(KEY_FIRST_INSTALL, true)
        if (isFirstInstall) {
            CoroutineScope(Dispatchers.IO).launch {
                insertSampleData()
                prefs.edit().putBoolean(KEY_FIRST_INSTALL, false).apply()
            }
        }
    }

    private suspend fun insertSampleData() {
        // 1. Tasks
        tasksRepository.addTask(
            Task(
                id = 0L,
                title = "Swipe right to complete a task",
                description = "Swipe to manage tasks quickly. Sample task.",
                priority = TaskPriority.MEDIUM,
                isCompleted = false,
                dueDate = System.currentTimeMillis() + 86400000,
                dueTime = null,
                repeatMode = "NONE",
                isReminderOn = false,
                categoryId = null,
                isDeleted = false
            )
        )
        tasksRepository.addTask(
            Task(
                id = 0L,
                title = "Swipe left to delete a task",
                description = "Sample task demonstrates deletion.",
                priority = TaskPriority.LOW,
                isCompleted = false,
                dueDate = null,
                dueTime = null,
                repeatMode = "NONE",
                isReminderOn = false,
                categoryId = null,
                isDeleted = false
            )
        )
        tasksRepository.addTask(
            Task(
                id = 0L,
                title = "Sample Completed Task",
                description = "You can restore completed tasks.",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                dueDate = System.currentTimeMillis() - 86400000,
                dueTime = null,
                repeatMode = "NONE",
                isReminderOn = false,
                categoryId = null,
                isDeleted = false
            )
        )

        // 2. Notes
        notesRepository.addNote(
            NoteEntity(
                title = "This is a sample note",
                content = "Use notes to save quick thoughts. This is a sample.",
                color = 0,
                timestamp = System.currentTimeMillis()
            )
        )
        tasksRepository.addTask(
            Task(
                id = 0L,
                title = "Task with Reminder (Sample)",
                description = "This task has a reminder set for tomorrow.",
                priority = TaskPriority.HIGH,
                isCompleted = false,
                dueDate = System.currentTimeMillis() + 86400000,
                dueTime = 1000L, // Placeholder time
                repeatMode = "NONE",
                isReminderOn = true,
                categoryId = null,
                isDeleted = false
            )
        )

        // 2. Notes
        notesRepository.addNote(
            NoteEntity(
                title = "This is a sample note",
                content = "Use notes to save quick thoughts. This is a sample.",
                color = 0,
                timestamp = System.currentTimeMillis()
            )
        )
        notesRepository.addNote(
             NoteEntity(
                title = "Shopping List (Example)",
                content = "- Milk\n- Eggs\n- Bread",
                color = 1,
                timestamp = System.currentTimeMillis()
            )
        )

        // 3. Reminders
        remindersRepository.addReminder(
            ReminderEntity(
                title = "Sample Birthday",
                description = "This is a sample birthday reminder (No alarm)",
                dateTime = System.currentTimeMillis() + 604800000,
                type = "Birthday",
                color = 0xFFFFCC00L,
                repeatMode = "YEARLY",
                isAlarm = false
            )
        )
         remindersRepository.addReminder(
            ReminderEntity(
                title = "Meeting (Sample)",
                description = "Weekly team sync",
                dateTime = System.currentTimeMillis() + 86400000,
                type = "Work",
                color = 0xFF6699FFL,
                repeatMode = "WEEKLY",
                isAlarm = true
            )
        )

        // 4. Finance
        financeRepository.seedCategories()
        
        // Income
        financeRepository.addEntry(
            FinanceEntry(
                amount = 10000.0,
                type = "INCOME",
                category = "Salary",
                date = System.currentTimeMillis(),
                note = "Salary (Sample)"
            )
        )

        // Expense
        financeRepository.addEntry(
            FinanceEntry(
                amount = 2000.0,
                type = "EXPENSE",
                category = "Food",
                date = System.currentTimeMillis(),
                note = "Groceries (Sample)"
            )
        )

        financeRepository.addDebt(
            FinanceDebt(
                personName = "Friend (Sample)",
                amount = 500.0,
                type = "Gave",
                date = System.currentTimeMillis(),
                note = "I lent to friend (Sample)",
                isPaid = false,
                reminderEnabled = false
            )
        )
         financeRepository.addDebt(
            FinanceDebt(
                personName = "Colleague (Sample)",
                amount = 100.0,
                type = "Took",
                date = System.currentTimeMillis(),
                note = "I borrowed for lunch (Sample)",
                isPaid = false,
                reminderEnabled = false
            )
        )

        // 5. Loans
        loanRepository.addLoan(
            LoanEntry(
                title = "Sample EMI",
                type = "LOAN",
                totalAmount = 50000.0,
                emiAmount = 1500.0,
                dueDay = 5,
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis() + 31536000000, // +1 year
                interestRate = 10.0,
                reminderEnabled = false,
                reminderType = "NOTIFICATION",
                status = "ACTIVE"
            )
        )

        // 6. Documents
        documentsRepository.insertDocument(
            DocumentEntity(
                title = "Sample Aadhaar Document",
                docNumber = "1234 5678 9012 (Sample)",
                docType = "Aadhaar Card",
                filePath = "",
                fileType = "PDF",
                timestamp = System.currentTimeMillis()
            )
        )

        // 7. Passwords
        passwordsRepository.insertPassword(
             PasswordEntity(
                 title = "Sample Email Account",
                 username = "user@example.com (Copy disabled)",
                 password = "password123", // Will be encrypted by repo
                 category = "Email"
             )
        )

        // 8. Emergency
        emergencyRepository.addContact(
            EmergencyContactEntity(
                name = "Police",
                number = "100",
                category = "Emergency",
                note = "Sample Emergency Number"
            )
        )
        emergencyRepository.addContact(
            EmergencyContactEntity(
                name = "Ambulance",
                number = "108",
                category = "Medical",
                note = "Sample Emergency Number"
            )
        )
         emergencyRepository.addContact(
            EmergencyContactEntity(
                name = "Electricity Board (Sample)",
                number = "1234567890",
                category = "Utilities",
                note = "Consumer No"
            )
        )
    }
}
