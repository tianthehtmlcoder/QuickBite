package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val role: String, // "CUSTOMER", "RESTAURANT_OWNER", "DRIVER"
    val walletBalance: Double = 100.0,
    val activeAddress: String = "123 Main St, Tech City",
    val savedAddresses: String = "Home: 123 Main St, Tech City;Office: 456 Tech Park, Innovate Valley",
    val paymentCards: String = "Visa ending in 4242;Mastercard ending in 9876",
    val selectedRestaurantId: Int = 1 // The restaurant ID managed by this owner if the role is RESTAURANT_OWNER
)

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Restaurant", "Cafe", "Bakery", "Grocery", "Shop"
    val rating: Double,
    val reviewCount: Int,
    val prepTimeMinutes: Int,
    val coverIconName: String, // identifier for drawables or icon representations
    val address: String,
    val revenue: Double = 0.0,
    val performanceRating: Double = 5.0,
    val lat: Double,
    val lng: Double,
    val isCloudKitchen: Boolean = false
)

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val isAvailable: Boolean = true,
    val rating: Double,
    val category: String, // "Popular", "Main", "Snack", "Dessert", "Beverage"
    val isHealthy: Boolean = false
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val restaurantId: Int,
    val restaurantName: String,
    val status: String, // "PENDING", "ACCEPTED", "PREPARING", "READY", "PICKED_UP", "DELIVERED", "CANCELLED"
    val deliveryFee: Double,
    val tipAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val subtotal: Double,
    val total: Double,
    val deliveryAddress: String,
    val dateString: String,
    val etaMinutes: Int,
    val driverId: String = "", // "" means unassigned
    val driverName: String = "",
    val couponUsed: String = "",
    val scheduledTime: String = "", // empty means now
    val groupOrderDetails: String = "", // empty means single order, otherwise: "Office Party; Split between User1, User2"
    val tableReservationDetails: String = "", // empty means delivery, otherwise details of party size, date, time
    val splitCount: Int = 1,
    val paymentMethod: String = "Card"
)

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val menuItemId: Int,
    val name: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val isPromo: Boolean = false
)

@Entity(tableName = "support_messages")
data class SupportMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val sender: String, // "Customer", "Agent"
    val messageText: String,
    val isRefundRequest: Boolean = false,
    val orderId: Int = 0
)
