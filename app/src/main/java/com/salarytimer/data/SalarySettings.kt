package com.salarytimer.data

/**
 * 薪资设置数据模型
 */
data class SalarySettings(
    /** 月薪（元） */
    val monthlySalary: Double = 10000.0,
    /** 上班时间 - 小时 (0-23) */
    val workStartHour: Int = 9,
    /** 上班时间 - 分钟 (0-59) */
    val workStartMinute: Int = 0,
    /** 下班时间 - 小时 */
    val workEndHour: Int = 18,
    /** 下班时间 - 分钟 */
    val workEndMinute: Int = 0,
    /** 午休开始小时 */
    val lunchStartHour: Int = 12,
    /** 午休开始分钟 */
    val lunchStartMinute: Int = 0,
    /** 午休结束小时 */
    val lunchEndHour: Int = 13,
    /** 午休结束分钟 */
    val lunchEndMinute: Int = 0,
    /** 休息制度 */
    val restType: RestType = RestType.WEEKENDS,
    /** 自定义月工作天数（仅 CUSTOM 类型有效） */
    val customWorkDays: Int = 22
)

/**
 * 休息制度枚举
 */
enum class RestType(val label: String, val daysPerMonth: Int) {
    /** 双休（标准） */
    WEEKENDS("双休（22天/月）", 22),
    /** 大小周 */
    ALTERNATING("大小周（24.5天/月）", 25),
    /** 单休 */
    SINGLE_REST("单休（26天/月）", 26),
    /** 法定工作日（年250天 ÷ 12 ≈ 20.83） */
    LEGAL("法定工作日（21天/月）", 21),
    /** 自定义 */
    CUSTOM("自定义", 22);

    companion object {
        fun fromName(name: String): RestType =
            entries.find { it.name == name } ?: WEEKENDS
    }
}
