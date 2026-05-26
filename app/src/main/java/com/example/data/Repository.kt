package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(private val db: AppDatabase) {

    private val userAccountDao = db.userAccountDao()
    private val restaurantDao = db.restaurantDao()
    private val menuItemDao = db.menuItemDao()
    private val orderDao = db.orderDao()
    private val orderItemDao = db.orderItemDao()
    private val notificationDao = db.notificationDao()
    private val supportMessageDao = db.supportMessageDao()

    // Expose Flows
    val userAccount: Flow<UserAccount?> = userAccountDao.getUserAccount()
    val restaurants: Flow<List<Restaurant>> = restaurantDao.getAllRestaurants()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()
    val allMessages: Flow<List<SupportMessage>> = supportMessageDao.getAllMessages()

    fun getRestaurantById(id: Int): Flow<Restaurant?> = restaurantDao.getRestaurantById(id)
    fun getMenuItemsForRestaurant(restaurantId: Int): Flow<List<MenuItem>> = menuItemDao.getMenuItemsByRestaurant(restaurantId)
    fun getOrderById(id: Int): Flow<Order?> = orderDao.getOrderById(id)
    fun getOrderItems(orderId: Int): Flow<List<OrderItem>> = orderItemDao.getItemsForOrder(orderId)
    fun getOrdersForRestaurant(restaurantId: Int): Flow<List<Order>> = orderDao.getOrdersForRestaurant(restaurantId)
    fun getOrdersForDriver(driverId: String): Flow<List<Order>> = orderDao.getOrdersForDriver(driverId)

    // Suspended modification calls
    suspend fun updateUserAccount(user: UserAccount) = withContext(Dispatchers.IO) {
        userAccountDao.updateUser(user)
    }

    suspend fun insertUserAccount(user: UserAccount) = withContext(Dispatchers.IO) {
        userAccountDao.insertUser(user)
    }

    suspend fun updateRestaurant(restaurant: Restaurant) = withContext(Dispatchers.IO) {
        restaurantDao.updateRestaurant(restaurant)
    }

    suspend fun insertMenuItem(menuItem: MenuItem) = withContext(Dispatchers.IO) {
        menuItemDao.insertMenuItem(menuItem)
    }

    suspend fun updateMenuItem(menuItem: MenuItem) = withContext(Dispatchers.IO) {
        menuItemDao.updateMenuItem(menuItem)
    }

    suspend fun deleteMenuItem(menuItem: MenuItem) = withContext(Dispatchers.IO) {
        menuItemDao.deleteMenuItem(menuItem)
    }

    suspend fun insertOrder(order: Order, items: List<OrderItem>): Long = withContext(Dispatchers.IO) {
        val orderId = orderDao.insertOrder(order)
        items.forEach {
            orderItemDao.insertOrderItem(it.copy(orderId = orderId.toInt()))
        }
        orderId
    }

    suspend fun updateOrder(order: Order) = withContext(Dispatchers.IO) {
        orderDao.updateOrder(order)
    }

    suspend fun insertNotification(notification: Notification) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markNotificationsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun insertSupportMessage(message: SupportMessage) = withContext(Dispatchers.IO) {
        supportMessageDao.insertMessage(message)
    }

    // Seed checking & implementation
    fun seedIfEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            val existingUser = userAccountDao.getUserAccount().firstOrNull()
            if (existingUser == null) {
                // Pre-seed owner user
                userAccountDao.insertUser(
                    UserAccount(
                        id = 1,
                        name = "Sarah Rahane",
                        email = "tiandsouzarahane@gmail.com",
                        role = "CUSTOMER",
                        walletBalance = 250.0,
                        activeAddress = "Apartment 4B, Hillcrest Manor, Tech District",
                        savedAddresses = "Home: Apartment 4B, Hillcrest Manor, Tech District;Office: Floor 12, Innovation Tower, Cyber Valley;Cafe: Blue Bottle Corner, Park Lane",
                        paymentCards = "Visa (*4242);Apple Pay;UPI (sarah@okaxis)",
                        selectedRestaurantId = 1
                    )
                )
            }

            val existingRestaurants = restaurantDao.getAllRestaurants().firstOrNull()
            if (existingRestaurants.isNullOrEmpty()) {
                val rId1 = restaurantDao.insertRestaurant(
                    Restaurant(name = "Mamma's Italian Pizzeria", category = "Restaurant", rating = 4.8, reviewCount = 284, prepTimeMinutes = 25, coverIconName = "pizza", address = "742 Evergreen Terrace, Central Plaza", lat = 12.9715, lng = 77.5945, isCloudKitchen = false)
                )
                val rId2 = restaurantDao.insertRestaurant(
                    Restaurant(name = "The Daily Grind Cafe", category = "Cafe", rating = 4.6, reviewCount = 142, prepTimeMinutes = 12, coverIconName = "cafe", address = "101 Espresso Blvd, Midtown", lat = 12.9725, lng = 77.5935, isCloudKitchen = false)
                )
                val rId3 = restaurantDao.insertRestaurant(
                    Restaurant(name = "Golden Crust Bakery", category = "Bakery", rating = 4.9, reviewCount = 189, prepTimeMinutes = 15, coverIconName = "bakery", address = "55 Old Baker's Lane, Old Town", lat = 12.9705, lng = 77.5955, isCloudKitchen = false)
                )
                val rId4 = restaurantDao.insertRestaurant(
                    Restaurant(name = "FreshCart Grocers", category = "Grocery", rating = 4.5, reviewCount = 67, prepTimeMinutes = 20, coverIconName = "grocery", address = "Market Circle No 4, Downtown", lat = 12.9735, lng = 77.5915, isCloudKitchen = false)
                )
                val rId5 = restaurantDao.insertRestaurant(
                    Restaurant(name = "Sweet Treats Gelato", category = "Shop", rating = 4.7, reviewCount = 94, prepTimeMinutes = 10, coverIconName = "shop", address = "Gelato Boulevard 12", lat = 12.9695, lng = 77.5965, isCloudKitchen = false)
                )
                val rId6 = restaurantDao.insertRestaurant(
                    Restaurant(name = "Green Garden Bowls", category = "Restaurant", rating = 4.7, reviewCount = 52, prepTimeMinutes = 18, coverIconName = "organic", address = "Kitchen Loft Space 17A, Industrial Park", lat = 12.9745, lng = 77.5925, isCloudKitchen = true)
                )

                // Populate menus
                // Pizzeria Menus
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId1.toInt(), name = "Margherita Woodfired Pizza", description = "Fresh buffalo mozzarella, San Marzano tomato sauce, sweet garden basil, extra virgin olive oil drizzle.", price = 12.99, rating = 4.8, category = "Popular"))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId1.toInt(), name = "Truffle Porcini Pasta", description = "Creamy house-made fettuccine with wild porcini mushrooms, parmigiano reggiano, and rich black truffle essence.", price = 15.50, rating = 4.9, category = "Main"))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId1.toInt(), name = "Garlic Herbed Focaccia", description = "Warm wood-fired flatbread seasoned with rosemary, sea salt, served with cold marinara and garlic oil.", price = 6.25, rating = 4.6, category = "Snack"))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId1.toInt(), name = "House Tiramisu", description = "Premium espresso-soaked sponge ladyfingers, whipped farm-fresh mascarpone cream, dark cocoa powder.", price = 7.50, rating = 4.9, category = "Dessert"))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId1.toInt(), name = "Italian San Pellegrino Soda", description = "Vibrant, sparkling blood orange soda imported directly from Lombardy.", price = 3.50, rating = 4.5, category = "Beverage"))

                // Cafe Menus
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId2.toInt(), name = "Smashed Avocado Toast", description = "Crushed organic Hass avocado, heirloom tomatoes, crumbled barrel-aged feta, microgreens on seed loaf.", price = 10.50, rating = 4.7, category = "Popular", isHealthy = true))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId2.toInt(), name = "Cold Brew Nitro Coffee", description = "Ethiopian single-origin beans, cold steeped for 20 hours and served nitrogen-infused on tap.", price = 4.95, rating = 4.8, category = "Beverage", isHealthy = true))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId2.toInt(), name = "Flat White", description = "Double shot of house espresso under a thin, silky layer of steamed velvety milk.", price = 4.25, rating = 4.7, category = "Beverage"))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId2.toInt(), name = "Almond Butter Croissant", description = "Twice-baked butter croissant loaded with house almond paste and toasted sliced almonds.", price = 4.75, rating = 4.6, category = "Dessert"))

                // Bakery Menus
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId3.toInt(), name = "Wild Yeast Sourdough Boule", description = "Naturally leavened wild yeast loaf with a crackly, blistered crust and chewy, open-crumb texture.", price = 6.95, rating = 4.9, category = "Main", isHealthy = true))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId3.toInt(), name = "Double Chocolate Fudge Brownie", description = "Extremely decadent rich dark chocolate fudge brownie slab finished with sea salt flakes.", price = 3.99, rating = 4.8, category = "Dessert"))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId3.toInt(), name = "Spinach & Feta Pastry Swirl", description = "Warm, flaky puff pastry nest loaded with sautéed baby spinach and creamy Greek feta cheese.", price = 4.50, rating = 4.7, category = "Snack", isHealthy = true))

                // Grocery Menus
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId4.toInt(), name = "Organic Mixed Berry Medley (250g)", description = "Fresh California hand-selected sweet strawberries, plump blueberries, and succulent raspberries.", price = 5.95, rating = 4.8, category = "Popular", isHealthy = true))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId4.toInt(), name = "Hass Avocados (Net 3 Pack)", description = "Ripe, creamy avocados ideal for guacamole, salads, or spreading over toast.", price = 4.99, rating = 4.6, category = "Main", isHealthy = true))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId4.toInt(), name = "Organic Whole Almond Milk (1L)", description = "Raw, unsweetened almond milk cold-processed without additives or thickeners.", price = 3.80, rating = 4.5, category = "Beverage", isHealthy = true))

                // Shop Menus
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId5.toInt(), name = "Dark Belgian Chocolate Gelato", description = "Rich artisan gelato churned with 72% dark Belgian cocoa and absolute dark chocolate chunks.", price = 4.50, rating = 4.9, category = "Popular"))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId5.toInt(), name = "Wild Strawberry Sorbete", description = "Dairy-free, luscious, pure fruit sorbete made with ripe mountain strawberries.", price = 4.25, rating = 4.7, category = "Dessert", isHealthy = true))

                // Green Garden Bowls (Cloud Kitchen)
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId6.toInt(), name = "Harvest Quinoa Buddha Bowl", description = "Tri-color warm quinoa, roasted butternut squash, crispy organic chickpeas, sliced avocados, and sesame-tahini.", price = 11.50, rating = 4.8, category = "Popular", isHealthy = true))
                menuItemDao.insertMenuItem(MenuItem(restaurantId = rId6.toInt(), name = "Detox Cold-Pressed Green Juice", description = "Cold-pulverized raw cucumber, crisp green apple, spinach, ginger root, lemon, and organic mint.", price = 5.95, rating = 4.7, category = "Beverage", isHealthy = true))

                // Add starter promotions in system notifications
                notificationDao.insertNotification(
                    Notification(timestamp = System.currentTimeMillis() - 3600000, title = "Welcome to QuickBite! 🎉", message = "Receive 30% off your first 3 orders with coupon code 'QUICK30'. Taste fine dining locally!", isRead = false, isPromo = true)
                )
                notificationDao.insertNotification(
                    Notification(timestamp = System.currentTimeMillis() - 600000, title = "20% Cashback on Cafe Orders ☕", message = "Order from The Daily Grind Cafe this weekend and enjoy 20% instant wallet cashback!", isRead = false, isPromo = true)
                )

                // Add nice welcome chat messages from Customer Support
                supportMessageDao.insertMessage(
                    SupportMessage(timestamp = System.currentTimeMillis() - 60000, sender = "Agent", messageText = "Hi! Welcome to QuickBite customer assistance. Feel free to ask about your orders, request cancellations, driver status, or process refunds interactively here!", isRefundRequest = false, orderId = 0)
                )
            }
        }
    }
}
