package com.afei.boxyledger.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.afei.boxyledger.BoxyLedgerApplication
import com.afei.boxyledger.data.local.CategoryDao
import com.afei.boxyledger.data.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryDao: CategoryDao) : ViewModel() {

    // 0: Expense, 1: Income
    private val _selectedType = MutableStateFlow(0)
    val selectedType = _selectedType.asStateFlow()
    
    val expenseCategories = categoryDao.getCategoriesByType(0)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val incomeCategories = categoryDao.getCategoriesByType(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setType(type: Int) {
        _selectedType.value = type
    }

    fun addCategory(name: String, icon: String, type: Int) {
        viewModelScope.launch {
            val count = categoryDao.getCategoryCount() // Rough sort order
            categoryDao.insertCategory(Category(name = name, icon = icon, type = type, sortOrder = count))
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }
    
    // Simple swap for reordering (not full drag-drop persistence logic yet, but good enough for demo)
    fun swapCategories(cat1: Category, cat2: Category) {
        viewModelScope.launch {
            val order1 = cat1.sortOrder
            val order2 = cat2.sortOrder
            categoryDao.updateCategory(cat1.copy(sortOrder = order2))
            categoryDao.updateCategory(cat2.copy(sortOrder = order1))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BoxyLedgerApplication)
                CategoryViewModel(application.container.database.categoryDao())
            }
        }
    }
}
