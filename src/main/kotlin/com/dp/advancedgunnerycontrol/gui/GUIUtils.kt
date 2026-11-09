package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.WeaponGroupAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import kotlin.math.roundToInt

fun groupAsString(group: WeaponGroupSpec, sh: FleetMemberAPI, includeCount: Boolean = true): String {
    return weaponNamesWithCount(group, sh.variant, includeCount).toString()
}

fun groupWeaponSummary(group: WeaponGroupSpec, variant: ShipVariantAPI): String {
    return weaponNamesWithCount(group, variant, true).joinToString(", ")
}

private fun weaponNamesWithCount(group: WeaponGroupSpec, variant: ShipVariantAPI, includeCount: Boolean): List<String> {
    val names = group.slots.mapNotNull { variant.getWeaponId(it) }.map {
        Global.getSettings().getWeaponSpec(it).weaponName
    }
    val occurrences = names.groupingBy { it }.eachCount()
    return names.toSet().sorted().map { if (includeCount) "${occurrences[it] ?: 0} x $it" else it }
}

fun groupAsString(group: WeaponGroupAPI, sh: FleetMemberAPI, includeCount: Boolean = true): String {
    val weaponStrings = group.weaponsCopy.map {
        it.displayName
    }
    val occ = mutableMapOf<String, Int>()
    weaponStrings.forEach {
        occ[it] = occ[it]?.plus(1) ?: 1
    }
    return weaponStrings.toSet().sorted().map {
        if(includeCount) {("${occ[it] ?: "0"} x $it")} else {it}
    }.toString()
}

fun groupFluxCost(group: WeaponGroupSpec, sh: FleetMemberAPI): Int {
    return try {
        group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
            Global.getSettings().getWeaponSpec(it).derivedStats.fluxPerSecond
        }.sum().roundToInt()
    }catch (e: Exception){
        0
    }

}

fun groupWeaponSpriteNames(group: WeaponGroupSpec, sh: FleetMemberAPI): List<String> {
    return group.slots.mapNotNull { sh.variant.getWeaponId(it) }.map {
        Global.getSettings().getWeaponSpec(it).turretSpriteName
    }.toSet().toList()
}

fun isElligibleForPD(groupIndex: Int, sh: FleetMemberAPI): Boolean = isElligibleForPD(groupIndex, sh.variant)

fun isElligibleForPD(groupIndex: Int, variant: ShipVariantAPI): Boolean {
    val group = variant.weaponGroups[groupIndex]
    val hasIPDA = variant.hasHullMod("pointdefenseai")
    return group.slots.mapNotNull { variant.getWeaponId(it) }.map {
        val weapon = Global.getSettings().getWeaponSpec(it)
        val aiHints = weapon.aiHints
        (hasIPDA && weapon.size == WeaponAPI.WeaponSize.SMALL && weapon.type != WeaponAPI.WeaponType.MISSILE) ||
                aiHints.contains(WeaponAPI.AIHints.PD_ALSO) ||
                aiHints.contains(WeaponAPI.AIHints.PD) ||
                aiHints.contains(WeaponAPI.AIHints.PD_ONLY)
    }.contains(true)
}

fun isEverythingBlacklisted(groupIndex: Int, sh: FleetMemberAPI): Boolean = isEverythingBlacklisted(groupIndex, sh.variant)

fun isEverythingBlacklisted(groupIndex: Int, variant: ShipVariantAPI): Boolean {
    val group = variant.weaponGroups[groupIndex]
    return (group.slots.mapNotNull { variant.getWeaponId(it) }.all { Settings.weaponBlacklist.contains(it) })
}

fun usesAmmo(groupIndex: Int, sh: FleetMemberAPI): Boolean = usesAmmo(groupIndex, sh.variant)

fun usesAmmo(groupIndex: Int, variant: ShipVariantAPI): Boolean {
    val group = variant.weaponGroups[groupIndex]
    return group.slots.mapNotNull { Global.getSettings().getWeaponSpec(variant.getWeaponId(it)) }.any {
        it.usesAmmo()
    }
}
