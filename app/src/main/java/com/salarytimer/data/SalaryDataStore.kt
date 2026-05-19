package com.salarytimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salary_settings")

/**
 * 薪资设置持久化管理
 */
class SalaryDataStore(private val context: Context) {

    companion object {
        private val KEY_MONTHLY_SALARY = doublePreferencesKey("monthly_salary")
        private val KEY_WORK_START_HOUR = intPreferencesKey("work_start_hour")
        private val KEY_WORK_START_MINUTE = intPreferencesKey("work_start_minute")
        private val KEY_WORK_END_HOUR = intPreferencesKey("work_end_hour")
        private val KEY_WORK_END_MINUTE = intPreferencesKey("work_end_minute")
        private val KEY_LUNCH_START_HOUR = intPreferencesKey("lunch_start_hour")
        private val KEY_LUNCH_START_MINUTE = intPreferencesKey("lunch_start_minute")
        private val KEY_LUNCH_END_HOUR = intPreferencesKey("lunch_end_hour")
        private val KEY_LUNCH_END_MINUTE = intPreferencesKey("lunch_end_minute")
        private val KEY_REST_TYPE = stringPreferencesKey("rest_type")
        private val KEY_CUSTOM_WORK_DAYS = intPreferencesKey("custom_work_days")
    }

    /** 读取设置 Flow */
    val settings: Flow<SalarySettings> = context.dataStore.data.map { prefs ->
        SalarySettings(
            monthlySalary = prefs[KEY_MONTHLY_SALARY] ?: 10000.0,
            workStartHour = prefs[KEY_WORK_START_HOUR] ?: 9,
            workStartMinute = prefs[KEY_WORK_START_MINUTE] ?: 0,
            workEndHour = prefs[KEY_WORK_END_HOUR] ?: 18,
            workEndMinute = prefs[KEY_WORK_END_MINUTE] ?: 0,
            lunchStartHour = prefs[KEY_LUNCH_START_HOUR] ?: 12,
            lunchStartMinute = prefs[KEY_LUNCH_START_MINUTE] ?: 0,
            lunchEndHour = prefs[KEY_LUNCH_END_HOUR] ?: 13,
            lunchEndMinute = prefs[KEY_LUNCH_END_MINUTE] ?: 0,
            restType = RestType.fromName(prefs[KEY_REST_TYPE] ?: RestType.WEEKENDS.name),
            customWorkDays = prefs[KEY_CUSTOM_WORK_DAYS] ?: 22
        )
    }

    /** 保存完整设置 */
    suspend fun save(settings: SalarySettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MONTHLY_SALARY] = settings.monthlySalary
            prefs[KEY_WORK_START_HOUR] = settings.workStartHour
            prefs[KEY_WORK_START_MINUTE] = settings.workStartMinute
            prefs[KEY_WORK_END_HOUR] = settings.workEndHour
            prefs[KEY_WORK_END_MINUTE] = settings.workEndMinute
            prefs[KEY_LUNCH_START_HOUR] = settings.lunchStartHour
            prefs[KEY_LUNCH_START_MINUTE] = settings.lunchStartMinute
            prefs[KEY_LUNCH_END_HOUR] = settings.lunchEndHour
            prefs[KEY_LUNCH_END_MINUTE] = settings.lunchEndMinute
            prefs[KEY_REST_TYPE] = settings.restType.name
            prefs[KEY_CUSTOM_WORK_DAYS] = settings.customWorkDays
        }
    }
}
