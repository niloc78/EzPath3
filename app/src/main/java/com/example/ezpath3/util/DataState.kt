package com.example.ezpath3.util

import java.lang.Exception

sealed class DataState<out R> {

    data class Success<out T>(val data : T) : DataState<T>()

    data class Error(val exception: Exception) : DataState<Nothing>()

    object Loading : DataState<Nothing>()

    override fun toString(): String {
        return when(this) {
            is Success -> data.toString()
            is Error -> "Error: " + exception.message.toString()
            else -> "No Data for this datastate"
        }
    }

}