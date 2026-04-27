package com.github.irmin.digsig.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            colors.background,
                            colors.surface,
                            colors.background
                        )
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(colors.primary.copy(alpha = 0.24f), Color.Transparent)
                    ),
                    radius = size.minDimension * 0.42f,
                    center = Offset(x = size.width * 0.14f, y = size.height * 0.08f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(colors.tertiary.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    radius = size.minDimension * 0.55f,
                    center = Offset(x = size.width * 0.88f, y = size.height * 0.22f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun HeroHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    badges: List<String> = emptyList()
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            accentColor.copy(alpha = 0.22f),
                            colors.secondary.copy(alpha = 0.18f),
                            colors.surface.copy(alpha = 0.96f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        InfoPill(
                            text = "Encrypted workspace",
                            icon = Icons.Rounded.AutoAwesome,
                            accentColor = accentColor
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant
                    )
                }

                if (badges.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        badges.take(3).forEach { badge ->
                            InfoPill(
                                text = badge,
                                accentColor = colors.tertiary,
                                containerColor = colors.surfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    label: String = "Open module"
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = accentColor.copy(alpha = 0.14f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    helper: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    badge: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor.copy(alpha = 0.14f)
                ) {
                    Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (badge != null) {
                        Text(
                            text = badge.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
                    )
                }
            }

            Text(
                text = helper,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
        }
    }
}

@Composable
fun InfoPill(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = accentColor.copy(alpha = 0.14f)
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusBanner(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    if (message.isEmpty()) return

    val colors = MaterialTheme.colorScheme
    val accent = if (isError) colors.error else colors.tertiary
    val container = if (isError) colors.errorContainer else colors.secondaryContainer
    val icon = if (isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle
    val title = if (isError) "Attention required" else "Operation complete"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.14f)
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isError) colors.onErrorContainer else colors.onSecondaryContainer
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) colors.onErrorContainer else colors.onSecondaryContainer
                )
            }
        }
    }
}
