package com.example.moneyhandler.data

import java.util.Locale

enum class Category(
    val displayName: String,
    val emoji: String,
    val colorHex: Long,
    val reductionTip: String
) {
    FOOD(
        displayName = "Food & Dining",
        emoji = "🍔",
        colorHex = 0xFFFF7043, // Deep Orange
        reductionTip = "Plan meals weekly, cook at home more often, and limit restaurant dining or food delivery apps."
    ),
    GROCERIES(
        displayName = "Groceries",
        emoji = "🛒",
        colorHex = 0xFF66BB6A, // Green
        reductionTip = "Create a grocery list before shopping, buy store brands, buy non-perishable staples in bulk, and avoid impulse buying on an empty stomach."
    ),
    TRANSPORT(
        displayName = "Transportation",
        emoji = "🚗",
        colorHex = 0xFF42A5F5, // Blue
        reductionTip = "Consider public transit, carpooling, or walking/cycling for shorter commutes to cut fuel and parking costs."
    ),
    SHOPPING(
        displayName = "Shopping",
        emoji = "🛍️",
        colorHex = 0xFFAB47BC, // Purple
        reductionTip = "Use a 48-hour rule before non-essential purchases to prevent impulse buying and check for discounts/sales."
    ),
    BILLS(
        displayName = "Bills & Utilities",
        emoji = "💡",
        colorHex = 0xFFFFCA28, // Amber
        reductionTip = "Audit recurring subscriptions, cancel unused services, and conserve electricity and water at home."
    ),
    ENTERTAINMENT(
        displayName = "Entertainment",
        emoji = "🎬",
        colorHex = 0xFFEC407A, // Pink
        reductionTip = "Look for free local community events, host game nights at home, and share digital subscription plans."
    ),
    HEALTH(
        displayName = "Health & Care",
        emoji = "💊",
        colorHex = 0xFF26A69A, // Teal
        reductionTip = "Opt for generic medicine alternatives when available and prioritize preventive healthcare routines."
    ),
    EDUCATION(
        displayName = "Education & Self-Dev",
        emoji = "📚",
        colorHex = 0xFF7E57C2, // Deep Purple
        reductionTip = "Utilize public library resources, free open courses, and online documentation before buying expensive books."
    ),
    OTHER(
        displayName = "Others",
        emoji = "📦",
        colorHex = 0xFF78909C, // Blue Grey
        reductionTip = "Keep detailed notes for miscellaneous expenses to identify untracked spending leaks."
    );

    companion object {
        fun fromString(value: String): Category {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }

        fun autoDetect(input: String): Category? {
            val clean = input.lowercase(Locale.getDefault()).trim()
            if (clean.isBlank()) return null

            val groceriesKeywords = listOf(
                "grocery", "groceries", "supermarket", "walmart", "target", "trader joe",
                "whole foods", "vegetable", "veggie", "fruit", "milk", "bread", "egg",
                "meat", "rice", "food market", "provision", "cereal", "pantry"
            )
            val foodKeywords = listOf(
                "food", "dining", "restaurant", "pizza", "burger", "coffee", "cafe",
                "starbucks", "lunch", "dinner", "breakfast", "snack", "doordash", "uber eats",
                "ubereats", "takeout", "zomato", "swiggy", "kfc", "mcdonald", "diner"
            )
            val transportKeywords = listOf(
                "transport", "transit", "uber", "lyft", "taxi", "cab", "bus", "train",
                "metro", "subway", "fuel", "gas", "petrol", "diesel", "parking", "toll", "flight"
            )
            val billsKeywords = listOf(
                "bill", "bills", "electricity", "water", "gas bill", "utility", "wifi",
                "internet", "rent", "phone", "recharge", "mobile bill", "insurance"
            )
            val shoppingKeywords = listOf(
                "shopping", "clothes", "clothing", "shoes", "amazon", "ebay", "fashion",
                "mall", "store", "electronics", "apparel", "dress"
            )
            val entertainmentKeywords = listOf(
                "entertainment", "movie", "cinema", "netflix", "spotify", "hulu", "disney",
                "game", "gaming", "steam", "playstation", "xbox", "concert", "ticket"
            )
            val healthKeywords = listOf(
                "health", "doctor", "pharmacy", "medicine", "pill", "hospital", "clinic",
                "dental", "dentist", "gym", "fitness", "workout"
            )
            val educationKeywords = listOf(
                "education", "book", "books", "course", "udemy", "coursera", "tuition",
                "school", "college", "university", "stationery", "pen"
            )

            return when {
                groceriesKeywords.any { clean.contains(it) } -> GROCERIES
                foodKeywords.any { clean.contains(it) } -> FOOD
                transportKeywords.any { clean.contains(it) } -> TRANSPORT
                billsKeywords.any { clean.contains(it) } -> BILLS
                shoppingKeywords.any { clean.contains(it) } -> SHOPPING
                entertainmentKeywords.any { clean.contains(it) } -> ENTERTAINMENT
                healthKeywords.any { clean.contains(it) } -> HEALTH
                educationKeywords.any { clean.contains(it) } -> EDUCATION
                else -> null
            }
        }
    }
}
