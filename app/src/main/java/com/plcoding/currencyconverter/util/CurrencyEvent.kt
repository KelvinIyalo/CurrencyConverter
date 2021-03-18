package com.plcoding.currencyconverter.util

import com.plcoding.currencyconverter.main.MainViewModel

sealed class CurrencyEvent {
    class Success(val resultText: String): CurrencyEvent()
    class Failure(val errorText: String): CurrencyEvent()
    object Loading :CurrencyEvent()
    object Empty : CurrencyEvent()
}