package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

class FinanceRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val expenseDao = db.expenseDao()
    private val incomeDao = db.incomeDao()
    private val budgetDao = db.budgetDao()
    private val goalDao = db.goalDao()
    private val achievementDao = db.achievementDao()
    private val notificationDao = db.notificationDao()
    private val settingDao = db.settingDao()
    private val currencyDao = db.currencyDao()

    // Public Flows
    val user: Flow<User?> = userDao.getUser()
    val setting: Flow<Setting?> = settingDao.getSetting()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allIncome: Flow<List<Income>> = incomeDao.getAllIncome()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
    val allAchievements: Flow<List<Achievement>> = achievementDao.getAllAchievements()
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()
    val allCurrencies: Flow<List<Currency>> = currencyDao.getAllCurrencies()

    // Initialize default tables
    suspend fun initializeDatabaseIfNeeded() {
        try {
            // Check Setting
            val existingSetting = setting.firstOrNull()
            if (existingSetting == null) {
                settingDao.insertOrUpdate(Setting())
            }

            // Check User
            val existingUser = user.firstOrNull()
            if (existingUser == null) {
                userDao.insertOrUpdate(User(name = "محمد صبري", xp = 0, level = 1))
            }

            // Check Currencies
            val existingCurrencies = allCurrencies.first()
            if (existingCurrencies.isEmpty()) {
                val defaults = listOf(
                    Currency("EGP", "ج.م", "الجنيه المصري", "Egyptian Pound", 1.0),
                    Currency("USD", "$", "الدولار الأمريكي", "US Dollar", 49.0),
                    Currency("EUR", "€", "اليورو", "Euro", 53.0),
                    Currency("SAR", "ر.س", "الريال السعودي", "Saudi Riyal", 13.0),
                    Currency("AED", "د.إ", "الدرهم الإماراتي", "UAE Dirham", 13.3)
                )
                currencyDao.insertCurrencies(defaults)
            }

            // Check Achievements
            val existingAchievements = allAchievements.first()
            if (existingAchievements.isEmpty()) {
                val defaults = listOf(
                    Achievement(
                        code = "first_saving",
                        titleEn = "First Saving Step",
                        titleAr = "أول خطوة ادخار",
                        descEn = "Added your first financial operation",
                        descAr = "أضفت أول عملية مالية في التطبيق",
                        badgeType = "bronze"
                    ),
                    Achievement(
                        code = "first_goal_completed",
                        titleEn = "Dream Achiever",
                        titleAr = "محقق الأحلام",
                        descEn = "Successfully achieved your first saving goal",
                        descAr = "حققت أول هدف ادخاري مالي بالكامل",
                        badgeType = "silver"
                    ),
                    Achievement(
                        code = "thirty_days_streak",
                        titleEn = "Dedicated Tracker",
                        titleAr = "المتابع الملتزم",
                        descEn = "Logged your operations for 30 consecutive days",
                        descAr = "سجلت مصروفاتك بنجاح لـ 30 يوماً متتالياً",
                        badgeType = "gold"
                    ),
                    Achievement(
                        code = "hundred_operations",
                        titleEn = "Financial Master",
                        titleAr = "الخبير المالي",
                        descEn = "Recorded over 100 financial transactions",
                        descAr = "سجلت أكثر من 100 عملية مالية",
                        badgeType = "gold"
                    )
                )
                achievementDao.insertAchievements(defaults)
            }
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Error initializing database", e)
        }
    }

    // User Operations
    suspend fun updateUsername(name: String) {
        val currentUser = user.first() ?: User(name = name, xp = 0, level = 1)
        userDao.insertOrUpdate(currentUser.copy(name = name))
    }

    // Gamification & XP Logic
    suspend fun awardXp(amount: Int) {
        val currentUser = user.first() ?: return
        val newXp = currentUser.xp + amount
        // Level calculation: level = (xp / 100) + 1
        val newLevel = (newXp / 100) + 1
        val updatedUser = currentUser.copy(xp = newXp, level = newLevel)
        userDao.insertOrUpdate(updatedUser)

        if (newLevel > currentUser.level) {
            createNotification(
                titleEn = "Level Up! 🎉",
                titleAr = "ترقية المستوى! 🎉",
                bodyEn = "Congratulations! You have reached Level $newLevel!",
                bodyAr = "تهانينا! لقد وصلت إلى المستوى $newLevel!"
            )
        }
    }

    // Expense Operations
    suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
        awardXp(10)
        checkAndUnlockAchievements()
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Int) {
        expenseDao.deleteExpenseById(id)
    }

    // Income Operations
    suspend fun addIncome(income: Income) {
        incomeDao.insertIncome(income)
        awardXp(10)
        checkAndUnlockAchievements()
    }

    suspend fun updateIncome(income: Income) {
        incomeDao.updateIncome(income)
    }

    suspend fun deleteIncome(income: Income) {
        incomeDao.deleteIncome(income)
    }

    suspend fun deleteIncomeById(id: Int) {
        incomeDao.deleteIncomeById(id)
    }

    // Budget Operations
    suspend fun addBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteBudgetById(id: Int) {
        budgetDao.deleteBudgetById(id)
    }

    // Goal Operations
    suspend fun addGoal(goal: Goal) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: Goal) {
        val isNewlyCompleted = goal.currentAmount >= goal.targetAmount && !goal.isCompleted
        val updatedGoal = if (isNewlyCompleted) goal.copy(isCompleted = true) else goal
        goalDao.updateGoal(updatedGoal)

        if (isNewlyCompleted) {
            awardXp(100)
            createNotification(
                titleEn = "Goal Achieved! 🎯",
                titleAr = "تم تحقيق الهدف! 🎯",
                bodyEn = "Excellent job! You successfully completed: ${goal.name}",
                bodyAr = "عمل رائع! لقد حققت بنجاح هدف: ${goal.name}"
            )
            unlockAchievement("first_goal_completed")
        }
    }

    suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }

    suspend fun deleteGoalById(id: Int) {
        goalDao.deleteGoalById(id)
    }

    // Notification Operations
    suspend fun createNotification(titleEn: String, titleAr: String, bodyEn: String, bodyAr: String) {
        val notif = Notification(
            titleEn = titleEn,
            titleAr = titleAr,
            bodyEn = bodyEn,
            bodyAr = bodyAr,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notif)
    }

    suspend fun markNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun clearNotifications() {
        notificationDao.clearAllNotifications()
    }

    // Settings Operations
    suspend fun updateSetting(setting: Setting) {
        settingDao.insertOrUpdate(setting)
    }

    // Achievements Unlocking Helper
    private suspend fun unlockAchievement(code: String) {
        val achievements = allAchievements.first()
        val target = achievements.find { it.code == code && !it.isUnlocked }
        if (target != null) {
            val updated = target.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis())
            achievementDao.updateAchievement(updated)
            createNotification(
                titleEn = "Achievement Unlocked! 🏆",
                titleAr = "إنجاز جديد مكتمل! 🏆",
                bodyEn = "Unlocked badge: ${updated.titleEn}",
                bodyAr = "حصلت على شارة: ${updated.titleAr}"
            )
        }
    }

    private suspend fun checkAndUnlockAchievements() {
        val expenses = allExpenses.first()
        val income = allIncome.first()
        val totalCount = expenses.size + income.size

        if (totalCount >= 1) {
            unlockAchievement("first_saving")
        }
        if (totalCount >= 100) {
            unlockAchievement("hundred_operations")
        }
        // Simulated streak achievement check
        if (totalCount >= 30) {
            unlockAchievement("thirty_days_streak")
        }
    }
}
