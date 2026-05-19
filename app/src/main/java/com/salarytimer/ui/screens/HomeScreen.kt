package com.salarytimer.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salarytimer.data.SalaryResult
import com.salarytimer.data.WorkStatus
import com.salarytimer.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    result: SalaryResult,
    onSettingsClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("💰 薪资计时器", fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // 状态指示器
            StatusChip(result.status)

            Spacer(Modifier.height(20.dp))

            // 核心：实时薪资大数字
            EarningsDisplay(result)

            Spacer(Modifier.height(24.dp))

            // 今日进度环
            DailyProgressRing(result)

            Spacer(Modifier.height(20.dp))

            // 信息卡片行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "今日目标",
                    value = "¥${result.formatMoney(result.dailyTarget)}",
                    subtitle = "${result.dailyWorkHours}h × ${result.monthlyWorkDays}天",
                    color = MoneyGreenLight
                )
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "今日剩余",
                    value = "¥${result.formatMoney(result.todayRemaining)}",
                    subtitle = if (result.status == WorkStatus.WORKING) "下班倒计时 ${result.formatCountdown()}" else result.status.label,
                    color = GoldAccent
                )
            }

            Spacer(Modifier.height(12.dp))

            // 月度进度
            MonthlyProgressCard(result)

            Spacer(Modifier.height(12.dp))

            // 秒薪卡片
            PerSecondCard(result)

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusChip(status: WorkStatus) {
    val bgColor = when (status) {
        WorkStatus.WORKING -> WorkingBlue
        WorkStatus.LUNCH_BREAK -> LunchOrange
        WorkStatus.BEFORE_WORK -> BeforeWorkYellow
        WorkStatus.AFTER_WORK -> AfterWorkPurple
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor.copy(alpha = 0.15f),
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 脉动动画点
            if (status == WorkStatus.WORKING) {
                PulsingDot(bgColor)
            } else {
                Text(status.emoji, fontSize = 14.sp)
            }
            Text(
                status.label,
                color = bgColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PulsingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun EarningsDisplay(result: SalaryResult) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "今日已赚",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "¥",
                style = MaterialTheme.typography.headlineMedium,
                color = MoneyGreenLight,
                fontWeight = FontWeight.Bold
            )
            Text(
                result.formatMoney(result.earnedToday),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MoneyGreenLight
            )
        }
        // 每秒收入动画
        if (result.status == WorkStatus.WORKING) {
            Spacer(Modifier.height(4.dp))
            Text(
                "+¥${result.formatPerSecond()} /秒",
                style = MaterialTheme.typography.bodyMedium,
                color = MoneyGreenLight.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DailyProgressRing(result: SalaryResult) {
    val animatedProgress by animateFloatAsState(
        targetValue = (result.dailyProgressPercent / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val ringColor = when (result.status) {
        WorkStatus.WORKING -> WorkingBlue
        WorkStatus.LUNCH_BREAK -> LunchOrange
        WorkStatus.BEFORE_WORK -> BeforeWorkYellow
        WorkStatus.AFTER_WORK -> MoneyGreenLight
    }

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // 背景环
            drawArc(
                color = Color.Gray.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // 进度环
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(ringColor.copy(alpha = 0.5f), ringColor, ringColor.copy(alpha = 0.5f))
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                result.formatProgress(result.dailyProgressPercent),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = ringColor
            )
            Text(
                "今日完成度",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
    }
}

@Composable
private fun MonthlyProgressCard(result: SalaryResult) {
    val animatedProgress by animateFloatAsState(
        targetValue = (result.monthlyProgressPercent / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "monthlyProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("本月收入", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(
                    result.formatProgress(result.monthlyProgressPercent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = GoldAccent,
                trackColor = GoldAccent.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "¥${result.formatMoney(result.monthlyEarned)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Text(
                    "¥${result.formatMoney(result.monthlyTarget)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun PerSecondCard(result: SalaryResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("每秒薪资", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "¥${result.formatPerSecond()}",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = MoneyGreenLight
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("每日工作", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${result.dailyWorkHours} 小时",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${result.monthlyWorkDays} 天/月",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}
