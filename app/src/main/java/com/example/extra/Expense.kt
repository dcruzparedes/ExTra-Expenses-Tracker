package com.example.extra

import androidx.room.Entity
import androidx.room.PrimaryKey

// Expenses entity, representing a single expense
@Entity(tableName = "expenses")
data class Expense (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val date: String,
    val createdAt: Long = System.currentTimeMillis(),
    val jornadaId: Int? = null
)

// Jornada entity, representing a closed session of expenses
@Entity(tableName = "jornadas")
data class Jornada (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double
)

// Category entity, representing a category of expenses
@Entity(tableName = "categories")
data class Category (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

@Entity(tableName = "expense_category_join",
    primaryKeys = ["expenseId", "categoryId"]
)
data class ExpenseCategoryCrossRef (
    val expenseId: Int,
    val categoryId: Int
)