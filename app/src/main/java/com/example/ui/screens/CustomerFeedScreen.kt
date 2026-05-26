package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MenuItem
import com.example.data.Restaurant
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFeedScreen(
    viewModel: AppViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val restaurants by viewModel.filteredRestaurants.collectAsState()
    val activeFilter by viewModel.categoryFilter.collectAsState()
    val searchVal by viewModel.searchQuery.collectAsState()
    val activeResId by viewModel.viewingRestaurantId.collectAsState()
    val viewingRes by viewModel.viewingRestaurant.collectAsState()
    val menuItems by viewModel.viewingMenuItems.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val userAccount by viewModel.currentUser.collectAsState()

    var showAddressDialog by remember { mutableStateOf(false) }
    var showCartDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (activeResId == null) {
            // MAIN FEED VIEW
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header: Address & Wallet & Notifications
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showAddressDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location Pin",
                                tint = CoralPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Delivering to",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown Location",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = userAccount?.activeAddress ?: "Set delivery location",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Balance Badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CoralPrimaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = CoralPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$${"%.2f".format(userAccount?.walletBalance ?: 0.0)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralOnPrimaryContainer
                            )
                        }
                    }

                    // Direct chat support
                    IconButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier.testTag("chat_support_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Chat Bot Assistance",
                            tint = CoralPrimary
                        )
                    }
                }

                // Global Food Category Search Bar
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchVal,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search restaurants, coffee, grocery, pies...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                        },
                        trailingIcon = {
                            if (searchVal.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear text")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_search_bar"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // Quick Horizontal Categories Filter Slider
                val categories = listOf("All", "Restaurant", "Cafe", "Bakery", "Grocery", "Shop", "Cloud Kitchen")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories) { cat ->
                        val selected = activeFilter == cat
                        Button(
                            onClick = { viewModel.setCategoryFilter(cat) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) CoralPrimary else MaterialTheme.colorScheme.surface,
                                contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("filter_tab_$cat"),
                            border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Feed Headers
                Text(
                    text = if (searchVal.isNotEmpty()) "Search Results" else "Fast local dispatch choices 📦",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                // List of merchants
                if (restaurants.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CoralPrimary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        if (searchVal.isEmpty() && activeFilter == "All") {
                            item {
                                // Beautiful Editorial style promo banner
                                val brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF6750A4), Color(0xFF21005D))
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(172.dp)
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(brush)
                                            .padding(16.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Ad",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.align(Alignment.BottomStart),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Fresh Sushi Feast",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                )
                                            )
                                            Text(
                                                text = "Free delivery on orders over $25 from Kumo Kitchen",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    // Take them to Kumo Kitchen (ID 3 or search)
                                                    viewModel.viewRestaurant(3)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFD0BCFF),
                                                    contentColor = Color(0xFF21005D)
                                                ),
                                                shape = RoundedCornerShape(20.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("Order Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        items(restaurants) { r ->
                            RestaurantFeedItemCard(
                                restaurant = r,
                                isFavorite = false,
                                onClick = { viewModel.viewRestaurant(r.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // MERCHANDISE MENU VIEW
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Panel
                viewingRes?.let { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.viewRestaurant(null) }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back back")
                        }
                        SimulatedFoodIcon(
                            iconName = r.coverIconName,
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = r.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FoodCategoryBadge(r.category)
                                if (r.isCloudKitchen) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = GreenHealthy.copy(0.15f)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "Cloud Kitchen",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenHealthy,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Restaurant quick info summary
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StarRatingBar(rating = r.rating, reviews = r.reviewCount)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "Prep time",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${r.prepTimeMinutes} mins",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Scrollable Dish Selection
                if (menuItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CoralPrimary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Text(
                                "Menu Items 🍽️",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        items(menuItems) { dish ->
                            MenuItemRowCard(
                                item = dish,
                                countInCart = cart[dish] ?: 0,
                                onAdd = { viewModel.addToCart(dish) },
                                onRemove = { viewModel.removeFromCart(dish) }
                            )
                        }
                    }
                }

                // BOTTOM FLOATING BASKET ACTION BAR
                if (cart.isNotEmpty()) {
                    val itemCount = cart.values.sum()
                    val totalSum = cart.entries.sumOf { it.key.price * it.value }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "$itemCount item${if (itemCount > 1) "s" else ""} added",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Total: $${"%.2f".format(totalSum + 2.99)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralPrimary
                                )
                            }

                            Button(
                                onClick = { showCartDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .testTag("view_cart_button")
                                    .height(48.dp)
                            ) {
                                Text("View Basket & Checkout", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.Default.ShoppingBasket, contentDescription = "Shopping Bag")
                            }
                        }
                    }
                }
            }
        }

        // ADDRESS SELECTION DIALOG
        if (showAddressDialog) {
            AlertDialog(
                onDismissRequest = { showAddressDialog = false },
                title = { Text("Select Delivery Location 📍", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val addresses = listOf(
                            "Home: Apartment 4B, Hillcrest Manor, Tech District",
                            "Office: Floor 12, Innovation Tower, Cyber Valley",
                            "Cafe: Blue Bottle Corner, Park Lane"
                        )
                        addresses.forEach { adr ->
                            val parsedName = adr.substringBefore(":")
                            val parsedVal = adr.substringAfter(": ")
                            Button(
                                onClick = {
                                    viewModel.updateAddress(parsedVal)
                                    showAddressDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (parsedName == "Home") Icons.Default.Home else if (parsedName == "Office") Icons.Default.Work else Icons.Default.Coffee,
                                        contentDescription = "Address icon",
                                        tint = CoralPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(parsedName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(parsedVal, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddressDialog = false }) { Text("Dismiss") }
                }
            )
        }

        // CHECKOUT BASKET CONSOLE (CUSTOM SHEET OVERLAY)
        if (showCartDialog) {
            CheckoutCartSheetOverlay(
                viewModel = viewModel,
                onDismiss = { showCartDialog = false },
                onOrderPlaced = {
                    showCartDialog = false
                    // Stay to track that order
                }
            )
        }
    }
}

@Composable
fun RestaurantFeedItemCard(
    restaurant: Restaurant,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
            .testTag("restaurant_card_${restaurant.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimulatedFoodIcon(
                iconName = restaurant.coverIconName,
                size = 68.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = restaurant.address,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    FoodCategoryBadge(restaurant.category)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating star",
                            tint = GoldSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "%.1f".format(restaurant.rating),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "•  ${restaurant.prepTimeMinutes} mins",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemRowCard(
    item: MenuItem,
    countInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("menu_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.isHealthy) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GreenHealthy.copy(alpha = 0.15f)),
                            shape = CircleShape
                        ) {
                            Text(
                                "Healthy",
                                fontSize = 8.sp,
                                color = GreenHealthy,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${"%.2f".format(item.price)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CoralPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Cart Adjuster Box
            if (!item.isAvailable) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Sold Out",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else if (countInCart == 0) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("add_item_${item.id}")
                ) {
                    Text("ADD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, CoralPrimary, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .height(34.dp)
                ) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(34.dp).testTag("decrease_item_${item.id}")) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrement", tint = CoralPrimary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = countInCart.toString(),
                        fontWeight = FontWeight.Bold,
                        color = CoralPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onAdd, modifier = Modifier.size(34.dp).testTag("increase_item_${item.id}")) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increment", tint = CoralPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckoutCartSheetOverlay(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val itemsMap by viewModel.cart.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val deliveryFee by viewModel.deliveryFee.collectAsState()
    val tipAmount by viewModel.driverTip.collectAsState()
    val couponCode by viewModel.couponCode.collectAsState()
    val discount by viewModel.appliedDiscount.collectAsState()
    val total by viewModel.orderTotal.collectAsState()
    val payMethod by viewModel.paymentMethod.collectAsState()

    // Multi-User checkouts
    val splitCount by viewModel.splitUsersCount.collectAsState()
    val groupOrder by viewModel.groupOrderName.collectAsState()
    val scheduledTime by viewModel.scheduledTime.collectAsState()
    val tableRes by viewModel.tableReservationDetails.collectAsState()

    var customCouponCode by remember { mutableStateOf("") }
    var couponStatusMsg by remember { mutableStateOf("") }

    // Table reservation text inputs
    var isTableResEnabled by remember { mutableStateOf(false) }
    var inputTableDetails by remember { mutableStateOf("") }

    // Schedule text input
    var isScheduleEnabled by remember { mutableStateOf(false) }
    var inputScheduleDetails by remember { mutableStateOf("") }

    // Group order text
    var isGroupEnabled by remember { mutableStateOf(false) }
    var inputGroupDetails by remember { mutableStateOf("") }

    // Split payment input count
    var isSplitEnabled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
                .clickable(enabled = false, onClick = {})
                .testTag("checkout_sheet"),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Place Your Order 🛒", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close checkout")
                    }
                }

                // Scrollable details list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Items Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    itemsMap.forEach { (dish, qty) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${qty}x  ${dish.name}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("$${"%.2f".format(dish.price * qty)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // ADVANCED SEAMLESS SETTINGS
                    // Promo codes box
                    Text("Coupons", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customCouponCode,
                            onValueChange = { customCouponCode = it },
                            placeholder = { Text("e.g. QUICK30 or FREESHIP", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("coupon_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val success = viewModel.setCoupon(customCouponCode)
                                couponStatusMsg = if (success) "Promo applied successfully! 🎉" else "Invalid code."
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                        ) {
                            Text("Apply")
                        }
                    }
                    if (couponCode.isNotEmpty()) {
                        Text(
                            "Applied: '$couponCode' (-$${"%.2f".format(discount)})",
                            color = GreenHealthy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (couponStatusMsg.isNotEmpty()) {
                        Text(couponStatusMsg, fontSize = 11.sp, color = Color.Red)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tipping segment
                    Text("Tip Your Delivery Hero 🧑‍🦼", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tips = listOf(0.0, 2.0, 5.0, 10.0)
                        tips.forEach { tip ->
                            val isSelected = tipAmount == tip
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setDriverTip(tip) },
                                label = { Text(if (tip == 0.0) "No Tip" else "$$tip") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // TABLE RESERVATIONS / SCHEDULES / GROUP OR ACTIONS
                    Text("Delivery Options", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    // Scheduled Delivery field trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isScheduleEnabled = !isScheduleEnabled }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Calendar", tint = CoralPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Scheduled Delivery (Later time)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (scheduledTime.isEmpty()) "As soon as possible (ASAP)" else "Scheduled for: $scheduledTime",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isScheduleEnabled) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Arrow"
                        )
                    }
                    if (isScheduleEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputScheduleDetails,
                                onValueChange = {
                                    inputScheduleDetails = it
                                    viewModel.setScheduledTime(it)
                                },
                                placeholder = { Text("e.g. May 27th, 6:30 PM", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(48.dp)
                            )
                        }
                    }

                    // Table reservation trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTableResEnabled = !isTableResEnabled }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.TableBar, contentDescription = "Table reservations", tint = CoralPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dine-In Table Reservation", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (tableRes.isEmpty()) "Standard Delivery" else "Dine-In reserved: $tableRes",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isTableResEnabled) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Arrow"
                        )
                    }
                    if (isTableResEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputTableDetails,
                                onValueChange = {
                                    inputTableDetails = it
                                    viewModel.setTableReservation(it)
                                },
                                placeholder = { Text("e.g. Table for 4, tonight at 8:00 PM", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(48.dp)
                            )
                        }
                    }

                    // Group order trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGroupEnabled = !isGroupEnabled }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Groups, contentDescription = "Group", tint = CoralPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Group Office / Party Ordering", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (groupOrder.isEmpty()) "Single customer checkout" else "Group order tag: $groupOrder",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isGroupEnabled) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Arrow"
                        )
                    }
                    if (isGroupEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputGroupDetails,
                                onValueChange = {
                                    inputGroupDetails = it
                                    viewModel.setGroupOrder(it)
                                },
                                placeholder = { Text("e.g. Office Lunch / Sarah's Birthday", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(48.dp)
                            )
                        }
                    }

                    // SPLIT BILL PAYMENTS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSplitEnabled = !isSplitEnabled }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CallSplit, contentDescription = "Split", tint = CoralPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Split Bill with Friends 👥", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (splitCount <= 1) "Pays full bill" else "Divided between $splitCount users ($${"%.2f".format(total / splitCount)} each)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isSplitEnabled) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Arrow"
                        )
                    }
                    if (isSplitEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Number of Split Users (including you)", fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (splitCount > 1) viewModel.setSplitUsers(splitCount - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Sub Split")
                                }
                                Text("$splitCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(
                                    onClick = { viewModel.setSplitUsers(splitCount + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Add Split")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // PAYMENT PORTALS SUPPORT
                    Text("Secure Secure Payment System 🔒", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val paymentOptions = listOf(
                            "Credit/Debit Card", "UPI (Instant PhonePe/GPay)", "Digital Wallet", "Net Banking", "Cash on Delivery (COD)"
                        )
                        paymentOptions.forEach { opt ->
                            val selected = payMethod == opt
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setPaymentMethod(opt) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { viewModel.setPaymentMethod(opt) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(opt, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Total Summary Block at bottom
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontSize = 12.sp)
                            Text("$${"%.2f".format(subtotal)}", fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Fee", fontSize = 12.sp)
                            Text("$${"%.2f".format(deliveryFee)}", fontSize = 12.sp)
                        }
                        if (discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Promotion Applied", fontSize = 12.sp, color = GreenHealthy)
                                Text("-$${"%.2f".format(discount)}", fontSize = 12.sp, color = GreenHealthy)
                            }
                        }
                        if (tipAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Driver Tip", fontSize = 12.sp)
                                Text("$${"%.2f".format(tipAmount)}", fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Outlay", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "$${"%.2f".format(total)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = CoralPrimary
                            )
                        }

                        if (splitCount > 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text(
                                    "Split Bill Cost: $splitCount users pays $${"%.2f".format(total / splitCount)} each.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralPrimary,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Submit button
                Button(
                    onClick = {
                        viewModel.placeOrder {
                            onOrderPlaced()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_order_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Secure Secure Pay & Confirm", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
