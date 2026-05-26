package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Order
import com.example.ui.AppViewModel
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.GreenHealthy

@Composable
fun DeliveryDriverScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.driverOrders.collectAsState()
    val dailyEarnings by viewModel.driverIncome.collectAsState()

    var activeNavigateOrder by remember { mutableStateOf<Order?>(null) }
    var navigationStepIndex by remember { mutableStateOf(0) }

    val navigationInstructions = listOf(
        "Head North on High Street toward the merchant outlet (0.8 mi)",
        "Park outside merchant and collect bag matching order code",
        "Dispatched successfully! Follow Bypass Road toward delivery address (2.5 mi)",
        "Turn right into residential block, pass central garden circle (0.4 mi)",
        "Arriving at front door porch. Leave parcel as requested and take photo"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP HEADER INFO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = "Driver",
                    tint = CoralPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Courier Fleet Workspace",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time dispatch orders dashboard",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Income ledger Badge
            Card(
                colors = CardDefaults.cardColors(containerColor = GreenHealthy.copy(0.15f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = "earnings", tint = GreenHealthy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Earned: $${"%.2f".format(dailyEarnings)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenHealthy
                    )
                }
            }
        }

        if (activeNavigateOrder != null) {
            // DRIVER DIRECT NAVIGATION SIMULATOR
            val currOrder = activeNavigateOrder!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("GPS Tracking Routing 🗺️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { activeNavigateOrder = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close GPS map")
                    }
                }

                // DRAW MAP ROUTE
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw a fake road path grid with dots
                            drawLine(Color.LightGray.copy(0.4f), Offset(w * 0.1f, h * 0.5f), Offset(w * 0.9f, h * 0.5f), strokeWidth = 24f)
                            drawLine(Color.LightGray.copy(0.4f), Offset(w * 0.5f, h * 0.1f), Offset(w * 0.5f, h * 0.9f), strokeWidth = 24f)

                            // Active path (animated or static based on step index)
                            val riderRatio = navigationStepIndex / 4.0f
                            val startX = w * 0.2f
                            val endX = w * 0.8f
                            val currentRiderX = startX + (endX - startX) * riderRatio
                            val startY = h * 0.45f

                            // Draw path connecting Merchant and Customer
                            drawLine(
                                color = CoralPrimary.copy(alpha = 0.6f),
                                start = Offset(startX, startY),
                                end = Offset(currentRiderX, startY),
                                strokeWidth = 12f
                            )

                            // Merchant representation dot
                            drawCircle(color = GoldSecondary, radius = 20f, center = Offset(startX, startY))
                            // Customer representation dot
                            drawCircle(color = CoralPrimary, radius = 24f, center = Offset(endX, startY))
                        }

                        // Labels overlapping
                        Text("Merchant Store", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp, top = 40.dp))
                        Text(currOrder.restaurantName, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp, top = 76.dp))

                        Text("Destination", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp, top = 40.dp))
                        Text("Customer Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp, top = 76.dp))

                        // Cycling scooter marker inside Map
                        val offsetRatio = navigationStepIndex / 4f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .align(Alignment.Center)
                                .offset(x = (-60 + (120 * offsetRatio)).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CoralPrimary)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = "bike", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // INSTRUCTIONS STEPPER SHEET
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Outlined.Navigation, contentDescription = "Nav GPS", tint = CoralPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fast Navigation Instructions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Text(
                            text = navigationInstructions[navigationStepIndex],
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Nav indicators
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Step ${navigationStepIndex + 1} of ${navigationInstructions.size}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            Row {
                                if (navigationStepIndex > 0) {
                                    TextButton(onClick = { navigationStepIndex-- }) {
                                        Text("Previous Offset")
                                    }
                                }
                                if (navigationStepIndex < navigationInstructions.size - 1) {
                                    Button(
                                        onClick = {
                                            navigationStepIndex++
                                            // Handle automatic statuses
                                            if (navigationStepIndex == 2 && currOrder.status == "READY") {
                                                viewModel.driverPickupOrder(currOrder)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                                    ) {
                                        Text("Next Offset")
                                    }
                                }
                            }
                        }
                    }
                }

                // Complete Order Box
                if (navigationStepIndex == navigationInstructions.size - 1) {
                    Button(
                        onClick = {
                            viewModel.driverCompleteDelivery(currOrder)
                            activeNavigateOrder = null
                            navigationStepIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenHealthy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("driver_complete_delivery")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "tick")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify Dropoff & Receive Incentives", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // UNASSIGNED COURIER QUEUE
            Text(
                "Nearby ready deliveries requested 👇",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            val nearbyOrders = orders.filter { it.status == "READY" || (it.status == "PICKED_UP" && it.driverId == viewModel.driverId.value) || (it.status == "PREPARING" && it.driverId == viewModel.driverId.value) }

            if (nearbyOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.CloudQueue, contentDescription = "All clean", modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No active delivery runs detected nearby.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(nearbyOrders) { ord ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("driver_order_card_${ord.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Dispatch Run #${ord.id}", fontWeight = FontWeight.Bold)
                                    Text("Est. Fee + Tip: $${"%.2f".format(5.0 + ord.tipAmount)}", fontWeight = FontWeight.ExtraBold, color = GreenHealthy)
                                }

                                Text(
                                    "From: ${ord.restaurantName}",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    "Ship To: ${ord.deliveryAddress}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Status: ${ord.status}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CoralPrimary
                                    )

                                    if (ord.driverId.isEmpty()) {
                                        Button(
                                            onClick = { viewModel.driverAcceptOrder(ord) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Accept Delivery Task")
                                        }
                                    } else {
                                        Button(
                                            onClick = { activeNavigateOrder = ord },
                                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Navigation, contentDescription = "launch navigation")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Launch GPS Tracking Map")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
