package com.salarytimer.data

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

/**
 * 薪资计算器 — 核心计算引擎
 */
object SalaryCalculator {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    /**
     * 计算每日工作时长（小时），扣除午休
     */
    fun dailyWorkHours(settings: SalarySettings): Double {
        val workStart = settings.workStartHour * 60.0 + settings.workStartMinute
        val workEnd = settings.workEndHour * 60.0 + settings.workEndMinute
        val lunchStart = settings.lunchStartHour * 60.0 + settings.lunchStartMinute
        val lunchEnd = settings.lunchEndHour * 60.0 + settings.lunchEndMinute
        val totalMinutes = (workEnd - workStart) - (lunchEnd - lunchStart)
        return max(totalMinutes / 60.0, 0.0)
    }

    /**
     * 计算有效月工作天数
     */
    fun monthlyWorkDays(settings: SalarySettings): Int {
        return when (settings.restType) {
            RestType.CUSTOM -> settings.customWorkDays
            else -> settings.restType.daysPerMonth
        }
    }

    /**
     * 计算每秒薪资（元/秒）
     */
    fun perSecondSalary(settings: SalarySettings): Double {
        val dailyHours = dailyWorkHours(settings)
        val days = monthlyWorkDays(settings)
        if (dailyHours <= 0 || days <= 0) return 0.0
        return settings.monthlySalary / (days * dailyHours * 3600.0)
    }

    /**
     * 计算每日目标薪资
     */
    fun dailyTargetSalary(settings: SalarySettings): Double {
        val days = monthlyWorkDays(settings)
        if (days <= 0) return 0.0
        return settings.monthlySalary / days
    }

    /**
     * 计算实时薪资数据
     * @return SalaryResult 包含所有计算结果
     */
    fun calculate(settings: SalarySettings, now: LocalDateTime = LocalDateTime.now()): SalaryResult {
        val today = now.toLocalDate()
        val currentTime = now.toLocalTime()

        // 工作时间边界
        val workStart = LocalTime.of(settings.workStartHour, settings.workStartMinute)
        val workEnd = LocalTime.of(settings.workEndHour, settings.workEndMinute)
        val lunchStart = LocalTime.of(settings.lunchStartHour, settings.lunchStartMinute)
        val lunchEnd = LocalTime.of(settings.lunchEndHour, settings.lunchEndMinute)

        val dailyTarget = dailyTargetSalary(settings)
        val perSec = perSecondSalary(settings)
        val dailyHours = dailyWorkHours(settings)

        // 判断当前状态
        val status: WorkStatus
        val earnedToday: Double
        val progressPercent: Double

        when {
            // 还没上班
            currentTime.isBefore(workStart) -> {
                status = WorkStatus.BEFORE_WORK
                earnedToday = 0.0
                progressPercent = 0.0
            }
            // 已经下班
            currentTime.isAfter(workEnd) || currentTime == workEnd -> {
                status = WorkStatus.AFTER_WORK
                earnedToday = dailyTarget
                progressPercent = 100.0
            }
            // 午休中
            currentTime.isAfter(lunchStart) && currentTime.isBefore(lunchEnd) -> {
                status = WorkStatus.LUNCH_BREAK
                // 午休前已赚取的部分
                earnedToday = calculateEarnedBefore(workStart, lunchStart, perSec)
                progressPercent = if (dailyTarget > 0) (earnedToday / dailyTarget) * 100.0 else 0.0
            }
            // 工作中
            else -> {
                status = WorkStatus.WORKING
                earnedToday = calculateWorkingEarned(workStart, workEnd, lunchStart, lunchEnd, currentTime, perSec)
                progressPercent = if (dailyTarget > 0) min((earnedToday / dailyTarget) * 100.0, 100.0) else 0.0
            }
        }

        // 本月已工作天数（不含今天）
        val daysWorkedThisMonth = calculateDaysWorkedThisMonth(today, settings)
        // 本月累计 = 已完成天数 * 每日目标 + 今日已赚
        val monthlyEarned = daysWorkedThisMonth * dailyTarget + earnedToday
        val monthlyProgress = if (settings.monthlySalary > 0)
            min((monthlyEarned / settings.monthlySalary) * 100.0, 100.0) else 0.0

        // 今日剩余
        val todayRemaining = max(dailyTarget - earnedToday, 0.0)

        // 距下班时间
        val secondsUntilEnd = if (status == WorkStatus.WORKING) {
            ChronoUnit.SECONDS.between(currentTime, workEnd)
        } else 0L

        // 秒薪显示格式
        val earnedPerSecond = if (status == WorkStatus.WORKING) perSec else 0.0

        return SalaryResult(
            status = status,
            earnedToday = earnedToday,
            dailyTarget = dailyTarget,
            dailyProgressPercent = progressPercent,
            monthlyEarned = monthlyEarned,
            monthlyTarget = settings.monthlySalary,
            monthlyProgressPercent = monthlyProgress,
            todayRemaining = todayRemaining,
            secondsUntilEnd = secondsUntilEnd,
            earnedPerSecond = earnedPerSecond,
            perSecondSalary = perSec,
            dailyWorkHours = dailyHours,
            monthlyWorkDays = monthlyWorkDays(settings),
            currentTime = now.format(timeFormatter),
            todayDate = today.toString()
        )
    }

    /**
     * 计算从 workStart 到 targetTime（不含午休）的已赚金额
     */
    private fun calculateWorkingEarned(
        workStart: LocalTime, workEnd: LocalTime,
        lunchStart: LocalTime, lunchEnd: LocalTime,
        currentTime: LocalTime, perSec: Double
    ): Double {
        val effectiveEnd = minOf(currentTime, workEnd)

        // 上午工作段：workStart → min(currentTime, lunchStart)
        val morningEnd = minOf(effectiveEnd, lunchStart)
        val morningSeconds = if (morningEnd.isAfter(workStart))
            ChronoUnit.SECONDS.between(workStart, morningEnd) else 0L

        // 下午工作段：lunchEnd → currentTime（如果过了午休）
        val afternoonSeconds = if (effectiveEnd.isAfter(lunchEnd))
            ChronoUnit.SECONDS.between(lunchEnd, effectiveEnd) else 0L

        return (morningSeconds + afternoonSeconds) * perSec
    }

    private fun calculateEarnedBefore(workStart: LocalTime, until: LocalTime, perSec: Double): Double {
        val seconds = if (until.isAfter(workStart))
            ChronoUnit.SECONDS.between(workStart, until) else 0L
        return seconds * perSec
    }

    /**
     * 计算本月已过去的完整工作天数
     */
    private fun calculateDaysWorkedThisMonth(today: LocalDate, settings: SalarySettings): Int {
        val firstOfMonth = today.withDayOfMonth(1)
        var count = 0
        var day = firstOfMonth
        while (day.isBefore(today)) {
            if (isWorkDay(day, settings)) count++
            day = day.plusDays(1)
        }
        return count
    }

    /**
     * 判断某天是否为工作日（基于休息制度的简化判断）
     */
    private fun isWorkDay(date: LocalDate, settings: SalarySettings): Boolean {
        return when (settings.restType) {
            RestType.LEGAL, RestType.WEEKENDS -> {
                val dow = date.dayOfWeek
                dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
            }
            RestType.SINGLE_REST -> {
                date.dayOfWeek != DayOfWeek.SUNDAY
            }
            RestType.ALTERNATING -> {
                // 大小周：单周周日休息，双周周六日都休息
                val weekOfYear = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
                val dow = date.dayOfWeek
                if (weekOfYear % 2 == 1) {
                    // 单周：只休周日
                    dow != DayOfWeek.SUNDAY
                } else {
                    // 双周：休周六和周日
                    dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
                }
            }
            RestType.CUSTOM -> {
                // 自定义：默认按双休处理，用户通过 customWorkDays 控制月总天数
                val dow = date.dayOfWeek
                dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
            }
        }
    }
}

/**
 * 工作状态枚举
 */
enum class WorkStatus(val label: String, val emoji: String) {
    BEFORE_WORK("未上班", "🌅"),
    WORKING("工作中", "💻"),
    LUNCH_BREAK("午休中", "🍜"),
    AFTER_WORK("已下班", "🌙")
}

/**
 * 薪资计算结果
 */
data class SalaryResult(
    val status: WorkStatus,
    val earnedToday: Double,
    val dailyTarget: Double,
    val dailyProgressPercent: Double,
    val monthlyEarned: Double,
    val monthlyTarget: Double,
    val monthlyProgressPercent: Double,
    val todayRemaining: Double,
    val secondsUntilEnd: Long,
    val earnedPerSecond: Double,
    val perSecondSalary: Double,
    val dailyWorkHours: Double,
    val monthlyWorkDays: Int,
    val currentTime: String,
    val todayDate: String
) {
    /** 格式化金额显示 */
    fun formatMoney(amount: Double): String = String.format("%.2f", amount)

    /** 格式化秒薪显示 */
    fun formatPerSecond(): String = String.format("%.6f", perSecondSalary)

    /** 格式化倒计时 */
    fun formatCountdown(): String {
        if (secondsUntilEnd <= 0) return "00:00:00"
        val h = secondsUntilEnd / 3600
        val m = (secondsUntilEnd % 3600) / 60
        val s = secondsUntilEnd % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    /** 进度百分比文字 */
    fun formatProgress(percent: Double): String = String.format("%.1f%%", percent)
}
