package com.salarytimer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salarytimer.data.SalaryCalculator
import com.salarytimer.data.SalaryDataStore
import com.salarytimer.data.SalaryResult
import com.salarytimer.data.SalarySettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SalaryViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SalaryDataStore(application)

    /** 设置 Flow */
    val settings: StateFlow<SalarySettings> = dataStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, SalarySettings())

    /** 当前时间戳（每秒更新，触发重算） */
    private val tick = MutableStateFlow(LocalDateTime.now())

    /** 实时计算结果 */
    val result: StateFlow<SalaryResult> = combine(settings, tick) { s, _ ->
        SalaryCalculator.calculate(s, LocalDateTime.now())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalaryCalculator.calculate(settings.value))

    init {
        // 每秒触发一次重算
        viewModelScope.launch {
            while (true) {
                delay(1000)
                tick.value = LocalDateTime.now()
            }
        }
    }

    /** 保存设置 */
    fun saveSettings(newSettings: SalarySettings) {
        viewModelScope.launch {
            dataStore.save(newSettings)
        }
    }
}
