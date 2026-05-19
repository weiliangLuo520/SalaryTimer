package com.salarytimer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.salarytimer.data.SalaryCalculator
import com.salarytimer.data.SalaryDataStore
import com.salarytimer.data.WorkStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 薪资实时小组件
 */
class SalaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = SalaryDataStore(context)
        val settings = dataStore.settings.first()
        val result = SalaryCalculator.calculate(settings)

        provideContent {
            SalaryWidgetContent(result)
        }
    }

    @Composable
    private fun SalaryWidgetContent(result: com.salarytimer.data.SalaryResult) {
        val statusColor = when (result.status) {
            WorkStatus.WORKING -> Color(0xFF42A5F5)
            WorkStatus.LUNCH_BREAK -> Color(0xFFFF9800)
            WorkStatus.BEFORE_WORK -> Color(0xFFFFEE58)
            WorkStatus.AFTER_WORK -> Color(0xFF4CAF50)
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(Color(0xFF161B22))
                .padding(16.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                // 标题行
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰 薪资计时器",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = ColorProvider(Color(0xFF8B949E)),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "${result.status.emoji} ${result.status.label}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(statusColor),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                // 核心金额
                Text(
                    text = "¥ ${result.formatMoney(result.earnedToday)}",
                    style = TextStyle(
                        fontSize = 32.sp,
                        color = ColorProvider(Color(0xFF4CAF50)),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = "今日已赚 · 目标 ¥${result.formatMoney(result.dailyTarget)}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = ColorProvider(Color(0xFF6E7681))
                    )
                )

                Spacer(modifier = GlanceModifier.height(12.dp))

                // 进度条
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .cornerRadius(4.dp)
                        .background(Color(0xFF2D333B))
                ) {
                    val progressWidth = (result.dailyProgressPercent / 100.0).coerceIn(0.0, 1.0)
                    Box(
                        modifier = GlanceModifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = progressWidth.toFloat())
                            .cornerRadius(4.dp)
                            .background(statusColor)
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // 底部信息行
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = result.formatProgress(result.dailyProgressPercent),
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(statusColor),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    if (result.status == WorkStatus.WORKING) {
                        Text(
                            text = "⏱ ${result.formatCountdown()}",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFF8B949E))
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = result.currentTime,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(Color(0xFF6E7681)),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

/**
 * Widget Receiver
 */
class SalaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SalaryWidget()
}
