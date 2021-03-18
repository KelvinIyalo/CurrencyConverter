package com.plcoding.currencyconverter.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.currencyconverter.data.models.Rates
import com.plcoding.currencyconverter.util.CurrencyEvent
import com.plcoding.currencyconverter.util.DispatcherProvider
import com.plcoding.currencyconverter.util.Resource
import com.plcoding.currencyconverter.util.getRateForCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.round

class MainViewModel @ViewModelInject constructor(
    private val repository: MainRepository,
    private val dispatchers: DispatcherProvider
): ViewModel() {



    private val _conversion = MutableStateFlow<CurrencyEvent>(CurrencyEvent.Empty)
    val conversion: StateFlow<CurrencyEvent> = _conversion

    fun convert(amountStr: String, fromCurrency: String, toCurrency: String) {
        val fromAmount = amountStr.toFloatOrNull()
        if(fromAmount == null) {
            _conversion.value = CurrencyEvent.Failure("Not a valid amount")
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _conversion.value = CurrencyEvent.Loading
            when(val ratesResponse = repository.getRates(fromCurrency)) {
                is Resource.Error -> _conversion.value = CurrencyEvent.Failure(ratesResponse.message!!)
                is Resource.Success -> {
                    val rates = ratesResponse.data!!.rates
                    val rate = getRateForCurrency(toCurrency, rates)
                    if(rate == null) {
                        _conversion.value = CurrencyEvent.Failure("Unexpected error")
                    } else {
                        val convertedCurrency = round(fromAmount * rate * 100) / 100
                        _conversion.value = CurrencyEvent.Success(
                            "$fromAmount $fromCurrency = $convertedCurrency $toCurrency"
                        )
                    }
                }
            }
        }
    }


}