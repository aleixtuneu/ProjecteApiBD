package cat.itb.dam.m78.dbdemo3.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.dam.m78.dbdemo3.db.MyTable

import kotlinx.coroutines.launch

class DatabaseViewModel : ViewModel() {
    val allTexts = mutableStateOf<List<MyTable>>(emptyList())

    init {
        _fetchAllTexts()
    }

    private fun _fetchAllTexts() {
        viewModelScope.launch {
            val myTableQueries = database.myTableQueries
            allTexts.value = myTableQueries.selectAll().executeAsList()
        }
    }

    fun insertText(text: String) {
        viewModelScope.launch {
            val myTableQueries = database.myTableQueries
            myTableQueries.insert(text)
            _fetchAllTexts()
        }
    }

    fun deleteText(id: Long) {
        viewModelScope.launch {
            val myTableQueries = database.myTableQueries
            myTableQueries.delete( id)
            _fetchAllTexts()
        }
    }
}