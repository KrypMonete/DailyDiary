package com.dailydairy.shopping

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

const val LIST_PLAIN = "list"
const val LIST_SHOP = "shop"
const val LIST_RECIPE = "recipe"

fun ShoppingList.kindLabel(): String = when (kind) {
    LIST_SHOP -> "Alışveriş"
    LIST_RECIPE -> "Tarif"
    else -> "Liste"
}

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val tint: Int = -1,
    val kind: String = LIST_SHOP,
    val createdAt: Long,
    val updatedAt: Long,
    val coverImage: String = "",
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val name: String,
    val count: Int = 1,
    val unit: String = "",
    val price: Int = 0,
    val done: Boolean = false,
    val position: Int = 0,
)

data class ShoppingListCard(
    @Embedded val list: ShoppingList,
    val total: Int,
    val remaining: Int,
)

fun ShoppingItem.moneyText(): String = kurusText(price)

fun kurusText(kurus: Int): String {
    val abs = kotlin.math.abs(kurus)
    val whole = abs / 100
    val fraction = (abs % 100).toString().padStart(2, '0')
    return "$whole,$fraction ₺"
}

fun parseKurus(raw: String): Int? {
    val clean = raw.trim().replace("₺", "").replace(" ", "").replace('.', ',')
    if (clean.isEmpty()) return 0
    val parts = clean.split(',')
    val whole = parts[0].toIntOrNull() ?: return null
    if (whole < 0) return null
    val fraction = when {
        parts.size == 1 -> 0
        else -> parts[1].padEnd(2, '0').take(2).toIntOrNull() ?: return null
    }
    return whole * 100 + fraction
}

fun List<ShoppingItem>.openTotal(): Long =
    filter { !it.done && it.price > 0 }.sumOf { it.price.toLong() }

fun List<ShoppingItem>.fullTotal(): Long =
    filter { it.price > 0 }.sumOf { it.price.toLong() }

fun ShoppingItem.amountText(): String {
    if (isStep()) return ""
    if (unit.isBlank()) return if (count == 1) "1 adet" else "$count adet"
    val number = when (unit) {
        "kg", "L" -> {
            val whole = count / 10
            val fraction = count % 10
            if (fraction == 0) "$whole" else "$whole,$fraction"
        }
        else -> count.toString()
    }
    val label = when (unit) {
        "sn" -> "saniye"
        "dk" -> "dakika"
        "su bardağı", "çay bardağı", "yemek kaşığı", "tatlı kaşığı", "çay kaşığı" -> unit
        else -> unit
    }
    return "$number $label"
}

fun ShoppingItem.isStep(): Boolean = unit == "step"

fun ShoppingItem.measureStep(): Int = when (unit) {
    "g", "ml" -> 50
    "kg", "L" -> 5
    else -> 1
}

fun ShoppingItem.measureMax(): Int = when (unit) {
    "g", "ml", "sn" -> 9_999
    "kg", "L" -> 200
    "saat" -> 48
    "su bardağı", "çay bardağı", "yemek kaşığı", "tatlı kaşığı", "çay kaşığı", "kaşık" -> 99
    else -> 99
}

fun ShoppingItem.cycled(): Pair<Int, String> = when (unit) {
    "" -> (if (count <= 30) 100 else count) to "g"
    "g" -> (count / 100).coerceAtLeast(5) to "kg"
    "kg" -> 250 to "ml"
    "ml" -> (count / 100).coerceAtLeast(5) to "L"
    "L" -> count.coerceIn(1, 99) to "su bardağı"
    "su bardağı" -> count to "çay bardağı"
    "çay bardağı" -> count to "yemek kaşığı"
    "yemek kaşığı" -> count to "tatlı kaşığı"
    "tatlı kaşığı" -> count to "çay kaşığı"
    "çay kaşığı", "kaşık" -> count.coerceIn(1, 9_999) to "sn"
    "sn" -> (count / 60).coerceAtLeast(1) to "dk"
    "dk" -> (count / 60).coerceAtLeast(1) to "saat"
    else -> 1 to ""
}

fun ShoppingItem.shopCycled(): Pair<Int, String> = 1 to ""

fun ShoppingItem.editText(recipe: Boolean, shop: Boolean): String = when {
    recipe && isStep() -> name
    recipe && unit.isNotBlank() -> "${amountText()} $name"
    recipe && count != 1 -> "$count $name"
    shop && price > 0 -> "$name ${kurusText(price)}"
    else -> name
}
