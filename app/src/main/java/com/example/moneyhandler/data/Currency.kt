package com.example.moneyhandler.data

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "US Dollar ($)"),
    EUR("EUR", "€", "Euro (€)"),
    GBP("GBP", "£", "British Pound (£)"),
    INR("INR", "₹", "Indian Rupee (₹)"),
    JPY("JPY", "¥", "Japanese Yen (¥)"),
    CAD("CAD", "C$", "Canadian Dollar (C$)"),
    AUD("AUD", "A$", "Australian Dollar (A$)");

    companion object {
        fun fromCode(code: String): Currency {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: USD
        }
    }
}
