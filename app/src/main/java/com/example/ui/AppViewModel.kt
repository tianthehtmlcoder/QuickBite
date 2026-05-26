package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(private val repository: Repository) : ViewModel() {

    init {
        // Trigger seed check
        repository.seedIfEmpty()
    }

    // Role state
    private val _currentUser = repository.userAccount
    val currentUser = _currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // All available restaurants & filters
    val restaurants = repository.restaurants.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _categoryFilter = MutableStateFlow("All")
    val categoryFilter = _categoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Filtered restaurants for Customer Feed
    val filteredRestaurants = combine(restaurants, categoryFilter, searchQuery) { list, cat, query ->
        list.filter { item ->
            val matchCat = cat == "All" || item.category.lowercase() == cat.lowercase() || (cat == "Cloud Kitchen" && item.isCloudKitchen)
            val matchQuery = query.isEmpty() || item.name.contains(query, ignoreCase = true) || item.category.contains(query, ignoreCase = true)
            matchCat && matchQuery
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // Currently viewed restaurant and menu items
    private val _viewingRestaurantId = MutableStateFlow<Int?>(null)
    val viewingRestaurantId = _viewingRestaurantId.asStateFlow()

    val viewingRestaurant = _viewingRestaurantId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getRestaurantById(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val viewingMenuItems = _viewingRestaurantId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getMenuItemsForRestaurant(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // Cart system (Item to Quantity)
    private val _cart = MutableStateFlow<Map<MenuItem, Int>>(emptyMap())
    val cart = _cart.asStateFlow()

    // Checkout configurations
    private val _couponCode = MutableStateFlow("")
    val couponCode = _couponCode.asStateFlow()

    private val _appliedDiscount = MutableStateFlow(0.0)
    val appliedDiscount = _appliedDiscount.asStateFlow()

    private val _driverTip = MutableStateFlow(0.0)
    val driverTip = _driverTip.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Credit/Debit Card")
    val paymentMethod = _paymentMethod.asStateFlow()

    private val _splitUsersCount = MutableStateFlow(1)
    val splitUsersCount = _splitUsersCount.asStateFlow()

    private val _scheduledTime = MutableStateFlow("") // empty = instantly
    val scheduledTime = _scheduledTime.asStateFlow()

    private val _groupOrderName = MutableStateFlow("") // empty = single order
    val groupOrderName = _groupOrderName.asStateFlow()

    private val _tableReservationDetails = MutableStateFlow("") // empty = delivery, otherwise details of party
    val tableReservationDetails = _tableReservationDetails.asStateFlow()

    // Calculated totals
    val subtotal = cart.map { map ->
        map.entries.sumOf { it.key.price * it.value }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val deliveryFee = cart.map { map ->
        if (map.isEmpty()) 0.0 else 2.99
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val orderTotal = combine(subtotal, deliveryFee, driverTip, appliedDiscount) { sub, dev, tip, disc ->
        val total = sub + dev + tip - disc
        if (total < 0) 0.0 else total
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    // Current active tracking order for simulation
    private val _trackingOrderId = MutableStateFlow<Int?>(null)
    val trackingOrderId = _trackingOrderId.asStateFlow()

    val trackingOrder = _trackingOrderId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getOrderById(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val trackingOrderItems = _trackingOrderId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getOrderItems(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // All global Orders (for statistics & history)
    val allOrders = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allNotifications = repository.allNotifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allMessages = repository.allMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Owner Managed statistics & menu
    val ownerRestaurantId = currentUser.map { user ->
        user?.selectedRestaurantId ?: 1
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 1)

    val ownerRestaurant = ownerRestaurantId.flatMapLatest { id ->
        repository.getRestaurantById(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val ownerMenuItems = ownerRestaurantId.flatMapLatest { id ->
        repository.getMenuItemsForRestaurant(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val ownerOrders = ownerRestaurantId.flatMapLatest { id ->
        repository.getOrdersForRestaurant(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // Driver statistics & orders
    val driverId = currentUser.map { user ->
        user?.id?.toString() ?: "1"
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = "1")

    val driverOrders = driverId.flatMapLatest { id ->
        repository.getOrdersForDriver(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    // Daily Incentive calculations for driver
    val driverIncome = driverOrders.map { list ->
        list.filter { it.status == "DELIVERED" }.sumOf { 5.0 + it.tipAmount }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    // Interactions
    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun viewRestaurant(id: Int?) {
        _viewingRestaurantId.value = id
    }

    // Cart actions
    fun addToCart(item: MenuItem) {
        val currentMap = _cart.value.toMutableMap()
        currentMap[item] = (currentMap[item] ?: 0) + 1
        _cart.value = currentMap
    }

    fun removeFromCart(item: MenuItem) {
        val currentMap = _cart.value.toMutableMap()
        val count = currentMap[item] ?: 0
        if (count <= 1) {
            currentMap.remove(item)
        } else {
            currentMap[item] = count - 1
        }
        _cart.value = currentMap
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _couponCode.value = ""
        _appliedDiscount.value = 0.0
        _driverTip.value = 0.0
        _splitUsersCount.value = 1
        _scheduledTime.value = ""
        _groupOrderName.value = ""
        _tableReservationDetails.value = ""
    }

    fun setCoupon(code: String): Boolean {
        _couponCode.value = code
        val sub = subtotal.value
        return if (code.trim().uppercase() == "QUICK30") {
            _appliedDiscount.value = sub * 0.3
            true
        } else if (code.trim().uppercase() == "FREESHIP") {
            _appliedDiscount.value = deliveryFee.value
            true
        } else {
            _appliedDiscount.value = 0.0
            false
        }
    }

    fun setDriverTip(amount: Double) {
        _driverTip.value = amount
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun setSplitUsers(count: Int) {
        _splitUsersCount.value = count
    }

    fun setScheduledTime(time: String) {
        _scheduledTime.value = time
    }

    fun setGroupOrder(name: String) {
        _groupOrderName.value = name
    }

    fun setTableReservation(details: String) {
        _tableReservationDetails.value = details
    }

    // Role switcher
    fun setRole(role: String) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.updateUserAccount(user.copy(role = role))
                postSystemNotification("System Link Active 🔗", "Switched current application view to $role workspace successfully.")
            }
        }
    }

    fun updateAddress(newAddress: String) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.updateUserAccount(user.copy(activeAddress = newAddress))
                postSystemNotification("Delivery Pin Dropped 📍", "Your delivery default address has been updated to: $newAddress")
            }
        }
    }

    // Place Order Flow
    fun placeOrder(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val currentRestaurant = viewingRestaurant.value ?: return@launch
            val sub = subtotal.value
            val fee = deliveryFee.value
            val tip = driverTip.value
            val disc = appliedDiscount.value
            val tot = orderTotal.value

            val newOrder = Order(
                userId = user.id,
                restaurantId = currentRestaurant.id,
                restaurantName = currentRestaurant.name,
                status = "PENDING",
                deliveryFee = fee,
                tipAmount = tip,
                discountAmount = disc,
                subtotal = sub,
                total = tot,
                deliveryAddress = user.activeAddress,
                dateString = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date()),
                etaMinutes = currentRestaurant.prepTimeMinutes + 10,
                couponUsed = _couponCode.value,
                scheduledTime = _scheduledTime.value,
                groupOrderDetails = if (_groupOrderName.value.isNotEmpty()) "Group: ${_groupOrderName.value}; split with ${_splitUsersCount.value} users" else "",
                tableReservationDetails = _tableReservationDetails.value,
                splitCount = _splitUsersCount.value,
                paymentMethod = _paymentMethod.value
            )

            val orderItems = _cart.value.map { entry ->
                OrderItem(
                    orderId = 0,
                    menuItemId = entry.key.id,
                    name = entry.key.name,
                    quantity = entry.value,
                    price = entry.key.price
                )
            }

            val newlyCreatedId = repository.insertOrder(newOrder, orderItems)
            _trackingOrderId.value = newlyCreatedId.toInt()

            // Update user wallet balance if COD is not selected and payment can be completed from balance or simulation
            if (_paymentMethod.value == "Wallet") {
                val rem = user.walletBalance - tot
                repository.updateUserAccount(user.copy(walletBalance = if (rem < 0) 0.0 else rem))
            }

            postSystemNotification(
                "Order Placed successfully! 🍕",
                "Your basket from ${currentRestaurant.name} is received and pending merchant confirmation."
            )

            // Clear Cart and Call complete
            clearCart()
            onComplete(newlyCreatedId.toInt())
        }
    }

    // Customer support and Chat flow
    private val _supportInput = MutableStateFlow("")
    val supportInput = _supportInput.asStateFlow()

    fun setSupportInput(text: String) {
        _supportInput.value = text
    }

    fun sendSupportMessage() {
        val text = _supportInput.value.trim()
        if (text.isEmpty()) return
        _supportInput.value = ""

        viewModelScope.launch {
            repository.insertSupportMessage(
                SupportMessage(timestamp = System.currentTimeMillis(), sender = "Customer", messageText = text)
            )

            // Auto simulated AI chatbot reply
            val lowercaseText = text.lowercase()
            val replyText = when {
                lowercaseText.contains("refund") -> {
                    // Check if there is a delivered/cancelled order to refund
                    val ordersList = allOrders.value
                    if (ordersList.isNotEmpty()) {
                        val lastOrder = ordersList.first()
                        "I see you are requesting a refund for Order #${lastOrder.id} at ${lastOrder.restaurantName}. I have processed a credit bump of $${"%.2f".format(lastOrder.total)} directly back to your secure QuickBite Wallet. Feel free to re-order anytime!"
                    } else {
                        "Certainly, I can assist with refund requests. I don't see any active orders on this account yet. Could you verify the details?"
                    }
                }
                lowercaseText.contains("cancel") -> {
                    val ordersList = allOrders.value
                    val pendingOrder = ordersList.find { it.status == "PENDING" || it.status == "ACCEPTED" }
                    if (pendingOrder != null) {
                        repository.updateOrder(pendingOrder.copy(status = "CANCELLED"))
                        postSystemNotification("Order Cancelled 🚫", "Order #${pendingOrder.id} has been cancelled upon user request.")
                        "I've successfully cancelled Order #${pendingOrder.id} from ${pendingOrder.restaurantName} for you since it didn't start cooking yet. The entire authorization amount has been put back into your wallet."
                    } else {
                        "I can cancel orders before the kitchen starts preparation. However, I didn't find any pending/accepted orders in your history right now. Let me know if you need help with anything else!"
                    }
                }
                lowercaseText.contains("coupon") || lowercaseText.contains("discount") || lowercaseText.contains("offer") -> {
                    "Absolutely! You can use coupon 'QUICK30' to get 30% off your food delivery subtotal, or 'FREESHIP' to waive the standard delivery fee. Simply add items to your cart and apply them at checkout!"
                }
                lowercaseText.contains("driver") || lowercaseText.contains("arrive") || lowercaseText.contains("delay") || lowercaseText.contains("track") -> {
                    val ordersList = allOrders.value
                    val activeTrack = ordersList.find { it.status != "DELIVERED" && it.status != "CANCELLED" }
                    if (activeTrack != null) {
                        "Your current active Order #${activeTrack.id} status is and currently under '${activeTrack.status}'. Estimated delivery in 12-15 minutes!"
                    } else {
                        "There are currently no active deliveries in transit for your coordinates. You can place a fresh order from the main restaurant feeds!"
                    }
                }
                else -> {
                    "Thanks for reaching out! Let me know if you want to cancel an order, request a refund, inquire about active delivery drivers, or suggest custom cuisines."
                }
            }

            // Delayed response to feel realistic
            kotlinx.coroutines.delay(1000)
            repository.insertSupportMessage(
                SupportMessage(
                    timestamp = System.currentTimeMillis(),
                    sender = "Agent",
                    messageText = replyText,
                    isRefundRequest = lowercaseText.contains("refund")
                )
            )

            // If a refund was processed, simulate wallet credit update
            if (lowercaseText.contains("refund")) {
                val user = currentUser.value
                val ordersList = allOrders.value
                if (user != null && ordersList.isNotEmpty()) {
                    val lastOrder = ordersList.first()
                    repository.updateUserAccount(user.copy(walletBalance = user.walletBalance + lastOrder.total))
                }
            }
        }
    }

    // Customer Side: Favorite restaurant toggles (Simulated using comma-separated IDs in activeAddress for ease, or a list)
    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites = _favorites.asStateFlow()

    fun toggleFavorite(restaurantId: Int) {
        val currentFavs = _favorites.value.toMutableSet()
        if (currentFavs.contains(restaurantId)) {
            currentFavs.remove(restaurantId)
            viewModelScope.launch {
                postSystemNotification("Favorite Removed 💔", "Restaurant removed from your home feed favorites.")
            }
        } else {
            currentFavs.add(restaurantId)
            viewModelScope.launch {
                postSystemNotification("Added to Favorites ❤️", "This merchant is now pinned to your QuickBite shortcuts.")
            }
        }
        _favorites.value = currentFavs
    }

    // Notifications post helpers
    suspend fun postSystemNotification(title: String, message: String) {
        repository.insertNotification(
            Notification(
                timestamp = System.currentTimeMillis(),
                title = title,
                message = message,
                isRead = false,
                isPromo = false
            )
        )
    }

    // Owner Side operations
    fun acceptOrder(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = "ACCEPTED"))
            postSystemNotification("Kitchen Fired Up 🍳", "${order.restaurantName} accepted your order #${order.id} and has begun preparation.")
        }
    }

    fun prepareOrder(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = "PREPARING"))
            postSystemNotification("Cooking Commenced 🍜", "Your order #${order.id} is cooking at ${order.restaurantName}'s prep station.")
        }
    }

    fun markReadyForPickup(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = "READY"))
            postSystemNotification("Order Swiped Ready! 📦", "Kitchen complete! Order #${order.id} is bagged and waiting for a nearby driver.")
        }
    }

    fun rejectOrder(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = "CANCELLED"))
            postSystemNotification("Order Cancelled 🚫", "${order.restaurantName} is unable to fullfil order #${order.id} at this time. Refunding wallet.")
            val user = currentUser.value
            if (user != null) {
                repository.updateUserAccount(user.copy(walletBalance = user.walletBalance + order.total))
            }
        }
    }

    fun modifyMenuItemStock(item: MenuItem, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateMenuItem(item.copy(isAvailable = isAvailable))
            postSystemNotification("Menu Stock Updated 🏷️", "'${item.name}' availability changed to ${if (isAvailable) "In Stock" else "Sold Out"}")
        }
    }

    fun addCustomMenuItem(name: String, desc: String, price: Double, category: String, isHealthy: Boolean) {
        viewModelScope.launch {
            val rId = ownerRestaurantId.value
            val newItem = MenuItem(
                restaurantId = rId,
                name = name,
                description = desc,
                price = price,
                rating = 5.0,
                category = category,
                isHealthy = isHealthy
            )
            repository.insertMenuItem(newItem)
            postSystemNotification("Dish Loaded 🍕", "Added new menu entry '$name' to your active restaurant listings catalog.")
        }
    }

    // Driver Side operations
    fun driverAcceptOrder(order: Order) {
        viewModelScope.launch {
            val driver = currentUser.value ?: return@launch
            repository.updateOrder(
                order.copy(
                    status = "PREPARING", // if driver accepts early, align it
                    driverId = driver.id.toString(),
                    driverName = driver.name
                )
            )
            // if order is ready, prepare for pick up
            if (order.status == "READY") {
                repository.updateOrder(
                    order.copy(
                        status = "PICKED_UP",
                        driverId = driver.id.toString(),
                        driverName = driver.name
                    )
                )
            }
            postSystemNotification("Driver Dispatched 🏎️", "${driver.name} is heading over to gather or carry your parcel.")
        }
    }

    fun driverPickupOrder(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = "PICKED_UP"))
            postSystemNotification("Parcel Collected 🎒", "Driver has picked up food from dispatch point. Navigating to coordinates.")
        }
    }

    fun driverCompleteDelivery(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = "DELIVERED"))
            // Update Restaurant Revenue too
            val rest = repository.getRestaurantById(order.restaurantId).firstOrNull()
            if (rest != null) {
                repository.updateRestaurant(rest.copy(revenue = rest.revenue + order.subtotal))
            }

            postSystemNotification("Delivery Dropped Off! 🏡", "Order #${order.id} delivered by ${order.driverName}. Thank you for using QuickBite!")
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markNotificationsRead()
        }
    }
}
