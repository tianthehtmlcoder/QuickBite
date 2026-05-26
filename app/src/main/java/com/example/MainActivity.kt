package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core manual DI setup
        val database = AppDatabase.getDatabase(this)
        val repository = Repository(database)

        setContent {
            MyApplicationTheme {
                val appViewModel: AppViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return AppViewModel(repository) as T
                        }
                    }
                )

                QuickBiteAppShell(viewModel = appViewModel)
            }
        }
    }
}

@Composable
fun QuickBiteAppShell(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()

    var activeScreen by remember { mutableStateOf("home") } // "home" | "chat"
    var customerSubView by remember { mutableStateOf("discover") } // "discover" | "track" | "alerts"

    val currentRole = currentUser?.role ?: "CUSTOMER"

    Scaffold(
        topBar = {
            // MULTI-ROLE SEGMENT SWITCH BAR
            Surface(
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QuickBite Ecosystem 🚀",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color.White
                        )

                        // Role selection capsule
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(0.15f))
                                .padding(2.dp)
                        ) {
                            listOf("CUSTOMER", "RESTAURANT_OWNER", "DRIVER").forEach { role ->
                                val selected = currentRole == role
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (selected) Color.White else Color.Transparent)
                                        .clickable {
                                            viewModel.setRole(role)
                                            // reset home tracking screens
                                            activeScreen = "home"
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("role_pill_$role"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (role) {
                                            "CUSTOMER" -> Icons.Default.Person
                                            "RESTAURANT_OWNER" -> Icons.Default.Storefront
                                            else -> Icons.Default.DirectionsBike
                                        },
                                        contentDescription = role,
                                        tint = if (selected) CoralPrimary else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // CUSTOMER VIEW SPECIAL BOTTOM NAV BAR
            if (currentRole == "CUSTOMER" && activeScreen == "home") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val countUnread = allNotifications.filter { !it.isRead }.size

                    NavigationBarItem(
                        selected = customerSubView == "discover",
                        onClick = { customerSubView = "discover" },
                        icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Discover Feed") },
                        label = { Text("Discover", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("bottom_discover")
                    )

                    NavigationBarItem(
                        selected = customerSubView == "track",
                        onClick = { customerSubView = "track" },
                        icon = { Icon(imageVector = Icons.Default.Map, contentDescription = "Tracking live order") },
                        label = { Text("Live Track", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("bottom_track")
                    )

                    NavigationBarItem(
                        selected = customerSubView == "alerts",
                        onClick = {
                            customerSubView = "alerts"
                            viewModel.markNotificationsRead()
                        },
                        icon = {
                            BadgedBox(badge = {
                                if (countUnread > 0) {
                                    Badge(containerColor = CoralPrimary) {
                                        Text(countUnread.toString(), color = Color.White)
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts")
                            }
                        },
                        label = { Text("Alerts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("bottom_alerts")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeScreen == "chat") {
                SupportChatScreen(
                    viewModel = viewModel,
                    onNavigateBack = { activeScreen = "home" }
                )
            } else {
                when (currentRole) {
                    "CUSTOMER" -> {
                        when (customerSubView) {
                            "discover" -> {
                                CustomerFeedScreen(
                                    viewModel = viewModel,
                                    onNavigateToChat = { activeScreen = "chat" }
                                )
                            }
                            "track" -> {
                                CustomerTrackingSubView(viewModel = viewModel)
                            }
                            "alerts" -> {
                                CustomerAlertsSubView(viewModel = viewModel)
                            }
                        }
                    }

                    "RESTAURANT_OWNER" -> {
                        RestaurantOwnerScreen(viewModel = viewModel)
                    }

                    "DRIVER" -> {
                        DeliveryDriverScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerTrackingSubView(viewModel: AppViewModel) {
    val trackerId by viewModel.trackingOrderId.collectAsState()
    val activeTrack by viewModel.trackingOrder.collectAsState()
    val items by viewModel.trackingOrderItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (trackerId == null || activeTrack == null) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = "Zero delivery tracker",
                    tint = Color.LightGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "You've no active orders 🍔",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Place an order at the discover catalog to check live tracking.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 4.dp)
                )
            }
        } else {
            val order = activeTrack!!
            Text("Order Live Tracking 🛵", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Start))

            // Map Simulation card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Status map", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(order.restaurantName, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text("Fast cooking node", fontSize = 10.sp, color = Color.Gray)
                        }

                        // Courier avatar
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "ETA: ${order.etaMinutes} mins",
                                fontWeight = FontWeight.Bold,
                                color = CoralPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                if (order.driverName.isEmpty()) "Finding courier..." else "Courier: ${order.driverName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Progress indicators
                    Text("Ecosystem Routing Progress:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    val statesList = listOf("PENDING", "ACCEPTED", "PREPARING", "READY", "PICKED_UP", "DELIVERED")
                    val currentIndex = statesList.indexOf(order.status)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (currentIndex < 0) 0.05f else (currentIndex + 1) / 6.0f)
                                .fillMaxHeight()
                                .background(CoralPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sent", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (currentIndex >= 0) CoralPrimary else Color.LightGray)
                        Text("Prep", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (currentIndex >= 2) CoralPrimary else Color.LightGray)
                        Text("Picked Up", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (currentIndex >= 4) CoralPrimary else Color.LightGray)
                        Text("Delivered", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (currentIndex >= 5) CoralPrimary else Color.LightGray)
                    }
                }
            }

            // Order details summary list
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Logistics details (Order #${order.id})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery address", fontSize = 12.sp, color = Color.Gray)
                        Text(order.deliveryAddress, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment channel", fontSize = 12.sp, color = Color.Gray)
                        Text(order.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (order.scheduledTime.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Scheduled delivery", fontSize = 12.sp, color = Color.Gray)
                            Text(order.scheduledTime, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
                        }
                    }

                    if (order.tableReservationDetails.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Table reservation", fontSize = 12.sp, color = Color.Gray)
                            Text(order.tableReservationDetails, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))

                    items.forEach { dish ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${dish.quantity}x  ${dish.name}", fontSize = 12.sp)
                            Text("$${"%.2f".format(dish.price * dish.quantity)}", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total debited", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("$${"%.2f".format(order.total)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = CoralPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerAlertsSubView(viewModel: AppViewModel) {
    val alerts by viewModel.allNotifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Notifications Inbox 🔔", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))

        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your alert center is clear.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alerts) { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.isPromo) CoralPrimaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (!alert.isPromo) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (alert.isPromo) CoralPrimary else MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (alert.isPromo) Icons.Default.LocalActivity else Icons.Default.CircleNotifications,
                                    contentDescription = "bell",
                                    tint = if (alert.isPromo) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(alert.message, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
