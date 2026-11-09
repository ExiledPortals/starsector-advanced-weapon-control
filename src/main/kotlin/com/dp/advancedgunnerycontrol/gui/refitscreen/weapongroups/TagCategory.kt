package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.typesandvalues.avoidArmorRegex
import com.dp.advancedgunnerycontrol.typesandvalues.forceFireRegex
import com.dp.advancedgunnerycontrol.typesandvalues.holdRegex
import com.dp.advancedgunnerycontrol.typesandvalues.panicFireRegex
import com.dp.advancedgunnerycontrol.typesandvalues.pdFluxRegex
import com.dp.advancedgunnerycontrol.typesandvalues.rangeRegex
import com.dp.advancedgunnerycontrol.typesandvalues.rofRegex

enum class TagCategory(val label: String, private val tagNames: Set<String>) {
    TARGET(
        "Target",
        setOf("PD", "NoPD", "Fighter", "NoFighters", "NoMissiles", "BigShips", "SmallShips", "PrioritisePD", "ShipTarget", "Overloaded")
    ),
    SHIELDS(
        "Shields",
        setOf("AvoidShields", "TargetShields", "TgtShields+", "AvdShields+", "TgtShieldsFT", "AvdShieldsFT", "ShieldsOff")
    ),
    FIRE(
        "Fire",
        setOf("ConserveAmmo", "CnsrvPDAmmo", "Opportunist", "AvoidDebris", "ForceAF", "DoNotShoot", "Merge")
    ),
    PHASE("Phase", setOf("AvoidPhased", "TargetPhase")),
    PRIORITY("Priority", setOf("PrioFighter", "PrioMissile", "PrioShips", "PrioWounded", "PrioHealthy", "PrioDense")),
    LIMITS("Limits", emptySet()),
    OTHER("Other", emptySet());

    fun contains(tag: String): Boolean = tag in tagNames
}

private val limitPatterns = listOf(holdRegex, pdFluxRegex, avoidArmorRegex, panicFireRegex, rangeRegex, forceFireRegex, rofRegex)

fun categoryOf(tag: String): TagCategory {
    if (limitPatterns.any { it.matches(tag) }) return TagCategory.LIMITS
    return TagCategory.values().firstOrNull { it.contains(tag) } ?: TagCategory.OTHER
}

fun groupTagsByCategory(tags: List<String>): Map<TagCategory, List<String>> {
    return tags.groupBy { categoryOf(it) }.toSortedMap(compareBy { it.ordinal })
}
