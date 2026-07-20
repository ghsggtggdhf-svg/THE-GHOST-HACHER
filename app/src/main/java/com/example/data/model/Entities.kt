package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val xp: Int = 0,
    val level: Int = 1,
    val avatar: String? = null
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String, // e.g., "Food", "Transportation", etc.
    val note: String,
    val timestamp: Long,
    val currencyCode: String = "EGP"
)

@Entity(tableName = "income")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String, // e.g., "Salary", "Freelance", etc.
    val note: String,
    val timestamp: Long,
    val currencyCode: String = "EGP"
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val period: String, // "weekly", "monthly"
    val category: String? = null, // null means overall budget, otherwise specific category
    val startDate: Long,
    val endDate: Long
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long,
    val isCompleted: Boolean = false
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String, // Unique identifier e.g., "first_saving"
    val titleEn: String,
    val titleAr: String,
    val descEn: String,
    val descAr: String,
    val badgeType: String, // "bronze", "silver", "gold"
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleEn: String,
    val titleAr: String,
    val bodyEn: String,
    val bodyAr: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey val id: Int = 1,
    val languageCode: String = "ar", // "ar", "en"
    val themeMode: String = "dark", // "dark", "light"
    val isSecurityEnabled: Boolean = false,
    val pinCode: String? = null,
    val defaultCurrency: String = "EGP"
)

@Entity(tableName = "currencies")
data class Currency(
    @PrimaryKey val code: String, // e.g., "EGP", "USD", "EUR"
    val symbol: String,
    val nameAr: String,
    val nameEn: String,
    val exchangeRateToEGP: Double // Rate to default currency (EGP)
)
