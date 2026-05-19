package com.salarytimer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.salarytimer.data.RestType
import com.salarytimer.data.SalarySettings
import com.salarytimer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SalarySettings,
    onSave: (SalarySettings) -> Unit,
    onBack: () -> Unit
) {
    var salaryText by remember(settings) { mutableStateOf(settings.monthlySalary.toLong().toString()) }
    var startHour by remember(settings) { mutableStateOf(settings.workStartHour) }
    var startMinute by remember(settings) { mutableStateOf(settings.workStartMinute) }
    var endHour by remember(settings) { mutableStateOf(settings.workEndHour) }
    var endMinute by remember(settings) { mutableStateOf(settings.workEndMinute) }
    var lunchStartH by remember(settings) { mutableStateOf(settings.lunchStartHour) }
    var lunchStartM by remember(settings) { mutableStateOf(settings.lunchStartMinute) }
    var lunchEndH by remember(settings) { mutableStateOf(settings.lunchEndHour) }
    var lunchEndM by remember(settings) { mutableStateOf(settings.lunchEndMinute) }
    var restType by remember(settings) { mutableStateOf(settings.restType) }
    var customDays by remember(settings) { mutableStateOf(settings.customWorkDays.toString()) }
    var restTypeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ 设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 月薪输入
            SettingsSection(title = "💰 月薪设置") {
                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { salaryText = it.filter { c -> c.isDigit() } },
                    label = { Text("月薪金额（元）") },
                    prefix = { Text("¥ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 工作时间
            SettingsSection(title = "🕐 工作时间") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimePickerField(
                        modifier = Modifier.weight(1f),
                        label = "上班时间",
                        hour = startHour,
                        minute = startMinute,
                        onTimeChange = { h, m -> startHour = h; startMinute = m }
                    )
                    TimePickerField(
                        modifier = Modifier.weight(1f),
                        label = "下班时间",
                        hour = endHour,
                        minute = endMinute,
                        onTimeChange = { h, m -> endHour = h; endMinute = m }
                    )
                }
            }

            // 午休时间
            SettingsSection(title = "🍜 午休时间") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimePickerField(
                        modifier = Modifier.weight(1f),
                        label = "午休开始",
                        hour = lunchStartH,
                        minute = lunchStartM,
                        onTimeChange = { h, m -> lunchStartH = h; lunchStartM = m }
                    )
                    TimePickerField(
                        modifier = Modifier.weight(1f),
                        label = "午休结束",
                        hour = lunchEndH,
                        minute = lunchEndM,
                        onTimeChange = { h, m -> lunchEndH = h; lunchEndM = m }
                    )
                }
            }

            // 休息制度
            SettingsSection(title = "📅 休息制度") {
                ExposedDropdownMenuBox(
                    expanded = restTypeExpanded,
                    onExpandedChange = { restTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = restType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("休息类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = restTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = restTypeExpanded,
                        onDismissRequest = { restTypeExpanded = false }
                    ) {
                        RestType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    restType = type
                                    restTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // 自定义天数
                if (restType == RestType.CUSTOM) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDays,
                        onValueChange = { customDays = it.filter { c -> c.isDigit() } },
                        label = { Text("月工作天数") },
                        suffix = { Text("天/月") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // 预览
            val previewSettings = SalarySettings(
                monthlySalary = salaryText.toDoubleOrNull() ?: 0.0,
                workStartHour = startHour, workStartMinute = startMinute,
                workEndHour = endHour, workEndMinute = endMinute,
                lunchStartHour = lunchStartH, lunchStartMinute = lunchStartM,
                lunchEndHour = lunchEndH, lunchEndMinute = lunchEndM,
                restType = restType,
                customWorkDays = customDays.toIntOrNull() ?: 22
            )
            PreviewCard(previewSettings)

            // 保存按钮
            Button(
                onClick = {
                    onSave(previewSettings)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreenLight)
            ) {
                Text("保存设置", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun TimePickerField(
    modifier: Modifier = Modifier,
    label: String,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    var hourText by remember(hour) { mutableStateOf(hour.toString().padStart(2, '0')) }
    var minuteText by remember(minute) { mutableStateOf(minute.toString().padStart(2, '0')) }

    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = hourText,
                onValueChange = {
                    hourText = it.filter { c -> c.isDigit() }.take(2)
                    hourText.toIntOrNull()?.coerceIn(0, 23)?.let { h -> onTimeChange(h, minute) }
                },
                modifier = Modifier.width(72.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
            Text(":", fontWeight = FontWeight.Bold, color = TextSecondary)
            OutlinedTextField(
                value = minuteText,
                onValueChange = {
                    minuteText = it.filter { c -> c.isDigit() }.take(2)
                    minuteText.toIntOrNull()?.coerceIn(0, 59)?.let { m -> onTimeChange(hour, m) }
                },
                modifier = Modifier.width(72.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }
    }
}

@Composable
private fun PreviewCard(settings: SalarySettings) {
    val dailyHours = run {
        val totalMin = ((settings.workEndHour * 60 + settings.workEndMinute) -
                (settings.workStartHour * 60 + settings.workStartMinute)) -
                ((settings.lunchEndHour * 60 + settings.lunchEndMinute) -
                        (settings.lunchStartHour * 60 + settings.lunchStartMinute))
        (totalMin.coerceAtLeast(0)) / 60.0
    }
    val days = when (settings.restType) {
        RestType.CUSTOM -> settings.customWorkDays
        else -> settings.restType.daysPerMonth
    }
    val dailyTarget = if (days > 0) settings.monthlySalary / days else 0.0
    val perSec = if (dailyHours > 0 && days > 0) settings.monthlySalary / (days * dailyHours * 3600) else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MoneyGreenDark.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📊 计算预览", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            PreviewRow("每日工时", String.format("%.1f 小时", dailyHours))
            PreviewRow("月工作天数", "$days 天")
            PreviewRow("每日薪资", "¥${String.format("%.2f", dailyTarget)}")
            PreviewRow("每秒薪资", "¥${String.format("%.6f", perSec)}")
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MoneyGreenLight)
    }
}
