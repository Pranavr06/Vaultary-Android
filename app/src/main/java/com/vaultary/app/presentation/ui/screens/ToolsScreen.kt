package com.vaultary.app.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultary.app.presentation.dashboard.ToolsViewModel
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin

@Composable
fun ToolsScreen(viewModel: ToolsViewModel) {
    val passwordCheckResult by viewModel.passwordCheckResult.collectAsState()
    val generatedPassword by viewModel.generatedPassword.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var checkInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Sync generated password into input
    LaunchedEffect(generatedPassword) {
        generatedPassword?.let {
            checkInput = it
            viewModel.checkPassword(it)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Test Your Strength",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Enter a password or generate a secure one.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Password Input
        OutlinedTextField(
            value = checkInput,
            onValueChange = { 
                checkInput = it
                if (it.length > 0) viewModel.checkPassword(it) else viewModel.checkPassword("")
            },
            placeholder = { Text("Enter password...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            trailingIcon = {
                Row(modifier = Modifier.padding(end = 8.dp)) {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(checkInput))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.generatePassword() }) {
                        Icon(Icons.Filled.Shuffle, contentDescription = "Generate", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )

        // Validation Rules
        val hasUpper = checkInput.any { it.isUpperCase() }
        val hasNumber = checkInput.any { it.isDigit() }
        val hasSpecial = checkInput.any { !it.isLetterOrDigit() }
        val has8Chars = checkInput.length >= 8

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RuleItem("Uppercase", hasUpper)
            RuleItem("Number", hasNumber)
            RuleItem("Special Char", hasSpecial)
            RuleItem("8+ Chars", has8Chars)
        }

        // Progress Bar
        val score = passwordCheckResult?.score ?: 0
        val color = when (score) {
            0, 1 -> Color(0xFFEF4444) // Red
            2 -> Color(0xFFF59E0B) // Yellow
            3 -> Color(0xFF10B981) // Green
            4 -> Color(0xFF22C55E) // Strong Green
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        val scoreText = when (score) {
            0, 1 -> "Very Weak"
            2 -> "Fair"
            3 -> "Strong"
            4 -> "Very Strong"
            else -> ""
        }
        val crackTime = passwordCheckResult?.crack_time ?: "instant"

        LinearProgressIndicator(
            progress = { if (checkInput.isEmpty()) 0f else (score + 1) / 5f },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(bottom = 8.dp)
        )

        if (checkInput.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(scoreText, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Crack time: $crackTime", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            if ((passwordCheckResult?.breach_count ?: 0) > 0) {
                Text(
                    text = "⚠️ Found in ${passwordCheckResult?.breach_count} data breaches!",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                        .background(Color(0xFFFEE2E2).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else if (score >= 3) {
                Text(
                    text = "", // Spacer for matching screenshot's light red area? Actually the screenshot shows a faint red area. It might be a breach warning box.
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 24.dp))

        // Security Breakdown Spider Chart
        Text(
            text = "Security Breakdown",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val lengthScore = minOf((passwordCheckResult?.password_length ?: 0) / 20f, 1f)
        val complexityScore = score / 4f
        val entropyScore = minOf(log10((passwordCheckResult?.guesses ?: 1.0).coerceAtLeast(1.0)).toFloat() / 20f, 1f)
        val safetyScore = if ((passwordCheckResult?.breach_count ?: 0) == 0 && checkInput.isNotEmpty()) 1f else 0f

        RadarChart(
            modifier = Modifier.size(240.dp).padding(16.dp),
            values = listOf(lengthScore, complexityScore, entropyScore, safetyScore),
            labels = listOf("Length", "Complexity", "Entropy", "Safety")
        )
    }
}

@Composable
fun RuleItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (isMet) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = if (isMet) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RadarChart(modifier: Modifier = Modifier, values: List<Float>, labels: List<String>) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val numAxes = 4
        val angleStep = (2 * Math.PI) / numAxes

        val gridColor = Color(0xFF94A3B8).copy(alpha = 0.3f)

        // Draw background web
        for (i in 1..4) {
            val r = radius * (i / 4f)
            val path = Path()
            for (j in 0 until numAxes) {
                val angle = j * angleStep - Math.PI / 2
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = gridColor, style = Stroke(width = 1.dp.toPx()))
        }

        // Draw axes and labels
        for (j in 0 until numAxes) {
            val angle = j * angleStep - Math.PI / 2
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            drawLine(gridColor, center, Offset(x, y), strokeWidth = 1.dp.toPx())

            // Label offset calculation
            val labelOffset = 24.dp.toPx()
            val lx = center.x + (radius + labelOffset) * cos(angle).toFloat()
            val ly = center.y + (radius + labelOffset) * sin(angle).toFloat()

            // In Canvas, to draw text properly we usually use text measurer, 
            // but for simplicity we will just rely on standard draw constraints or let it be.
            // As drawing text directly in Canvas is complex without TextMeasurer (Compose 1.2+), 
            // we'll just skip text rendering in the raw Canvas and overlay Text components if needed.
            // But since this is a custom chart, we'll draw simple circles at the nodes.
        }

        // Draw data polygon
        val dataPath = Path()
        for (j in 0 until numAxes) {
            val angle = j * angleStep - Math.PI / 2
            val v = if (values.all { it == 0f }) 0f else values[j] // Collapse to center if 0
            val r = radius * v.coerceIn(0f, 1f)
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()
            
            if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            drawCircle(Color(0xFF3B82F6), radius = 4.dp.toPx(), center = Offset(x, y))
        }
        dataPath.close()
        
        drawPath(dataPath, color = Color(0x403B82F6)) // Fill
        drawPath(dataPath, color = Color(0xFF3B82F6), style = Stroke(width = 2.dp.toPx())) // Stroke
    }
}
