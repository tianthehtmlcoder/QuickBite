package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MenuItem
import com.example.data.Order
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantOwnerScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val rState by viewModel.ownerRestaurant.collectAsState()
    val orders by viewModel.ownerOrders.collectAsState()
    val menuItems by viewModel.ownerMenuItems.collectAsState()

    var activeTab by remember { mutableStateOf("Orders") } // "Orders" | "Menu" | "Analytics"

    // Custom menu form states
    var showAddForm by remember { mutableStateOf(false) }
    var customDisName by remember { mutableStateOf("") }
    var customDisDesc by remember { mutableStateOf("") }
    var customDisPrice by remember { mutableStateOf("") }
    var customDisCategory by remember { mutableStateOf("Main") }
    var customDisHealthy by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP OWNER PROFILE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = "Merchant Loft",
                tint = CoralPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = rState?.name ?: "Restaurant Control Center",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Owner Workspace  •  ${rState?.category ?: "Food Joint"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // TAB SEGMENT SELECTOR
        TabRow(
            selectedTabIndex = when (activeTab) {
                "Orders" -> 0
                "Menu" -> 1
                else -> 2
            }
        ) {
            Tab(selected = activeTab == "Orders", onClick = { activeTab = "Orders" }) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = "orders list")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Orders (${orders.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }.size})", fontSize = 13.sp)
                }
            }
            Tab(selected = activeTab == "Menu", onClick = { activeTab = "Menu" }) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = "menu list")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Menu", fontSize = 13.sp)
                }
            }
            Tab(selected = activeTab == "Analytics", onClick = { activeTab = "Analytics" }) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Analytics, contentDescription = "analytics")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Finances", fontSize = 13.sp)
                }
            }
        }

        // BODY INTERFACES
        when (activeTab) {
            "Orders" -> {
                val activeMerchantOrders = orders.filter { it.status != "DELIVERED" && it.status != "CANCELLED" }
                if (activeMerchantOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Inbox, contentDescription = "Zero orders", modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("All quiet! No active basket requests.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeMerchantOrders) { order ->
                            MerchantOrderCard(
                                order = order,
                                onAccept = { viewModel.acceptOrder(order) },
                                onPrepare = { viewModel.prepareOrder(order) },
                                onReady = { viewModel.markReadyForPickup(order) },
                                onReject = { viewModel.rejectOrder(order) }
                            )
                        }
                    }
                }
            }

            "Menu" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Dishes Catalog", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Button(
                            onClick = { showAddForm = !showAddForm },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Insert dishes")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Dish")
                        }
                    }

                    if (showAddForm) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("add_item_form"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("New Product Details", fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = customDisName,
                                    onValueChange = { customDisName = it },
                                    label = { Text("Dish Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = customDisDesc,
                                    onValueChange = { customDisDesc = it },
                                    label = { Text("Recipe Description") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = customDisPrice,
                                    onValueChange = { customDisPrice = it },
                                    label = { Text("Unit Price ($)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = customDisHealthy,
                                        onCheckedChange = { customDisHealthy = it }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark as Organic / Healthy choice")
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showAddForm = false }) { Text("Cancel") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val pr = customDisPrice.toDoubleOrNull() ?: 0.0
                                            if (customDisName.isNotEmpty() && pr > 0.0) {
                                                viewModel.addCustomMenuItem(
                                                    name = customDisName,
                                                    desc = customDisDesc,
                                                    price = pr,
                                                    category = customDisCategory,
                                                    isHealthy = customDisHealthy
                                                )
                                                customDisName = ""
                                                customDisDesc = ""
                                                customDisPrice = ""
                                                showAddForm = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                                    ) {
                                        Text("Save Dish")
                                    }
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(menuItems) { d ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(d.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("$${"%.2f".format(d.price)}  •  ${d.category}", color = CoralPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (d.isAvailable) "Instock" else "SoldOut", fontSize = 11.sp, color = if (d.isAvailable) GreenHealthy else Color.Red)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = d.isAvailable,
                                        onCheckedChange = { viewModel.modifyMenuItemStock(d, it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // ANALYTICS SCREEN
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Store Performance Analytics 📊", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    // Grid metric cards
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CoralPrimaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "revenue", tint = CoralPrimary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Total Revenue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${"%.2f".format(rState?.revenue ?: 0.0)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = CoralPrimary)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "salesCount", tint = CoralPrimary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Orders Serviced", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${orders.size} parcels", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // Store Rating card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Reviews rating", tint = GoldSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Reputation Rating", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Outstanding star rank loaded from customers.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Text("4.9 / 5.0", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Card(colors = CardDefaults.cardColors(containerColor = GreenHealthy.copy(0.15f))) {
                                        Text("EXCELLENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreenHealthy, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Simple Visual sales performance chart (Bar graphics using compose shapes)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Weekly Revenue Trend 📈", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val stats = listOf(40, 65, 30, 85, 45, 95, 12)
                                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                                stats.zip(days).forEach { (score, day) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .fillMaxHeight(score / 100f)
                                                .background(CoralPrimary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(day, fontSize = 10.sp, color = Color.Gray)
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

@Composable
fun MerchantOrderCard(
    order: Order,
    onAccept: () -> Unit,
    onPrepare: () -> Unit,
    onReady: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("merchant_order_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: ID and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = order.dateString,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Address and total
            Text(
                "Deliver to: ${order.deliveryAddress}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (order.groupOrderDetails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Split Details: ${order.groupOrderDetails}",
                    fontSize = 11.sp,
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (order.tableReservationDetails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Table Booked: ${order.tableReservationDetails}",
                    fontSize = 11.sp,
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Value: $${"%.2f".format(order.total)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = CoralPrimary
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (order.status) {
                            "PENDING" -> Color(0xFFFCF3CF)
                            "ACCEPTED" -> Color(0xFFD5F5E3)
                            "PREPARING" -> Color(0xFFE8F8F5)
                            "READY" -> Color(0xFFEBF5FB)
                            else -> Color(0xFFF2F3F4)
                        }
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = order.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = when (order.status) {
                            "PENDING" -> Color(0xFFB7950B)
                            "ACCEPTED" -> Color(0xFF1E8449)
                            "PREPARING" -> Color(0xFF117864)
                            "READY" -> Color(0xFF2471A3)
                            else -> Color.DarkGray
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Action triggers based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (order.status) {
                    "PENDING" -> {
                        Button(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onAccept,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("Accept Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "ACCEPTED" -> {
                        Button(
                            onClick = onPrepare,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.OutdoorGrill, contentDescription = "cooking")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Begin Prep (Cooking)", fontSize = 12.sp)
                        }
                    }

                    "PREPARING" -> {
                        Button(
                            onClick = onReady,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenHealthy),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.LocalMall, contentDescription = "ready")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Order Ready for Pick up", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "READY" -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Waiting for Courier dispatch...",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
