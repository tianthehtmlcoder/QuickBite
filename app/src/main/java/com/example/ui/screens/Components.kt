package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FoodCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val color = when (category.lowercase()) {
        "restaurant" -> CoralPrimary
        "cafe" -> YellowBakery
        "bakery" -> GoldSecondary
        "grocery" -> BlueGrocery
        "shop" -> PurpleDesserts
        "organic" -> GreenHealthy
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StarRatingBar(
    rating: Double,
    reviews: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Rating Star",
            tint = GoldSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "%.1f".format(rating),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (reviews > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($reviews+ reviews)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SimulatedFoodIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when (iconName.lowercase()) {
                    "pizza" -> Color(0xFFFDF2E9)
                    "cafe" -> Color(0xFFEFEBE9)
                    "bakery" -> Color(0xFFFEF9E7)
                    "grocery" -> Color(0xFFE8F8F5)
                    "shop" -> Color(0xFFF5EEF8)
                    else -> Color(0xFFEAECEE)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.15f)) {
            val width = this.size.width
            val height = this.size.height

            when (iconName.lowercase()) {
                "pizza" -> {
                    // Pizza Crust slice
                    drawArc(
                        color = Color(0xFFF39C12),
                        startAngle = -10f,
                        sweepAngle = 110f,
                        useCenter = true,
                        size = Size(width * 0.9f, height * 0.9f)
                    )
                    // Cheese filling
                    drawArc(
                        color = Color(0xFFF4D03F),
                        startAngle = 5f,
                        sweepAngle = 80f,
                        useCenter = true,
                        size = Size(width * 0.75f, height * 0.75f)
                    )
                    // Pepperonis
                    drawCircle(Color(0xFFC0392B), radius = width * 0.08f, center = Offset(width * 0.45f, height * 0.5f))
                    drawCircle(Color(0xFFC0392B), radius = width * 0.08f, center = Offset(width * 0.6f, height * 0.65f))
                }
                "cafe" -> {
                    // Mug Cup
                    drawRoundRect(
                        color = Color(0xFF6D4C41),
                        topLeft = Offset(width * 0.2f, height * 0.35f),
                        size = Size(width * 0.55f, height * 0.55f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.1f, height * 0.1f)
                    )
                    // Handle
                    drawArc(
                        color = Color(0xFF6D4C41),
                        startAngle = -90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(width * 0.65f, height * 0.45f),
                        size = Size(width * 0.2f, height * 0.35f),
                        style = Stroke(width * 0.08f)
                    )
                    // Coffee aroma steam lines
                    drawLine(
                        color = Color(0xFF8D6E63),
                        start = Offset(width * 0.3f, height * 0.25f),
                        end = Offset(width * 0.32f, height * 0.1f),
                        strokeWidth = width * 0.06f
                    )
                    drawLine(
                        color = Color(0xFF8D6E63),
                        start = Offset(width * 0.48f, height * 0.25f),
                        end = Offset(width * 0.50f, height * 0.1f),
                        strokeWidth = width * 0.06f
                    )
                }
                "bakery" -> {
                    // Sourdough bread loaf curves
                    drawArc(
                        color = Color(0xFFD35400),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        size = Size(width, height * 1.2f),
                        topLeft = Offset(0f, -height * 0.2f)
                    )
                    // Score lines
                    drawLine(
                        color = Color(0xFFFEF9E7),
                        start = Offset(width * 0.25f, height * 0.35f),
                        end = Offset(width * 0.45f, height * 0.1f),
                        strokeWidth = width * 0.05f
                    )
                    drawLine(
                        color = Color(0xFFFEF9E7),
                        start = Offset(width * 0.5f, height * 0.35f),
                        end = Offset(width * 0.7f, height * 0.1f),
                        strokeWidth = width * 0.05f
                    )
                }
                "grocery" -> {
                    // Apple/Cherry
                    drawCircle(
                        color = Color(0xFFE74C3C),
                        radius = width * 0.35f,
                        center = Offset(width * 0.5f, height * 0.55f)
                    )
                    // Leaf stem
                    drawArc(
                        color = Color(0xFF27AE60),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(width * 0.38f, height * 0.05f),
                        size = Size(width * 0.3f, height * 0.3f),
                        style = Stroke(width * 0.07f)
                    )
                }
                "shop" -> {
                    // Gelato Cup
                    drawRoundRect(
                        color = Color(0xFF9B59B6),
                        topLeft = Offset(width * 0.2f, height * 0.5f),
                        size = Size(width * 0.6f, height * 0.45f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.03f, height * 0.03f)
                    )
                    // Cream Scoop 1
                    drawCircle(
                        color = Color(0xFFFFC0CB),
                        radius = width * 0.28f,
                        center = Offset(width * 0.38f, height * 0.4f)
                    )
                    // Cream Scoop 2
                    drawCircle(
                        color = Color(0xFFF4D03F),
                        radius = width * 0.25f,
                        center = Offset(width * 0.62f, height * 0.42f)
                    )
                }
                else -> {
                    // Default salad bowl shape
                    drawArc(
                        color = Color(0xFF2ECC71),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        size = Size(width * 0.85f, height * 0.85f),
                        topLeft = Offset(width * 0.075f, height * 0.1f)
                    )
                    // healthy garnishes
                    drawCircle(Color(0xFFE74C3C), radius = width * 0.08f, center = Offset(width * 0.3f, height * 0.4f))
                    drawCircle(Color(0xFFF39C12), radius = width * 0.06f, center = Offset(width * 0.65f, height * 0.45f))
                }
            }
        }
    }
}
