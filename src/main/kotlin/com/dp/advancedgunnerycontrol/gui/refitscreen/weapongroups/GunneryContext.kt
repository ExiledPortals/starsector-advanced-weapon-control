package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.gui.groupWeaponSummary
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.defaultShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.isIncompatibleWithExistingTags
import com.dp.advancedgunnerycontrol.typesandvalues.loadPersistedShipModes
import com.dp.advancedgunnerycontrol.typesandvalues.persistShipModes
import com.dp.advancedgunnerycontrol.typesandvalues.shouldTagBeDisabled
import com.dp.advancedgunnerycontrol.utils.generateUniversalFleetMemberId
import com.dp.advancedgunnerycontrol.utils.loadPersistentTags
import com.dp.advancedgunnerycontrol.utils.persistTags
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.WeaponGroupSpec
import com.fs.starfarer.api.loading.WeaponGroupType

const val WEAPON_GROUP_COUNT = 7

class GunneryContext(val ship: ShipAPI, val member: FleetMemberAPI, val variant: ShipVariantAPI) {
    val shipId: String = generateUniversalFleetMemberId(ship)

    val loadoutIndex: Int
        get() = Values.storageIndex

    fun groupSpec(groupIndex: Int): WeaponGroupSpec? = variant.weaponGroups.getOrNull(groupIndex)

    fun isGroupFilled(groupIndex: Int): Boolean = groupSpec(groupIndex)?.slots?.isNotEmpty() == true

    fun filledGroups(): List<Int> = (0 until WEAPON_GROUP_COUNT).filter { isGroupFilled(it) }

    fun groupSignature(): List<List<String>?> = (0 until WEAPON_GROUP_COUNT).map { groupSpec(it)?.slots?.toList() }

    fun weaponSummary(groupIndex: Int): String {
        val group = groupSpec(groupIndex) ?: return ""
        return groupWeaponSummary(group, variant)
    }

    fun tagsOf(groupIndex: Int): List<String> = loadPersistentTags(shipId, member, groupIndex, loadoutIndex)

    fun saveTags(groupIndex: Int, tags: List<String>) {
        persistTags(shipId, member, groupIndex, loadoutIndex, tags)
    }

    fun toggleTag(groupIndex: Int, tag: String) {
        val tags = tagsOf(groupIndex)
        saveTags(groupIndex, if (tag in tags) tags - tag else tags + tag)
    }

    fun unavailableReason(groupIndex: Int, tag: String): String? {
        if (shouldTagBeDisabled(groupIndex, variant, tag)) return "Can't be used with this group's weapons"
        return isIncompatibleWithExistingTags(tag, tagsOf(groupIndex)).reason
    }

    fun removeUnavailableTags() {
        filledGroups().forEach { groupIndex ->
            val tags = tagsOf(groupIndex)
            val usableTags = tags.filterNot { shouldTagBeDisabled(groupIndex, variant, it) }
            if (usableTags != tags) saveTags(groupIndex, usableTags)
        }
    }

    fun fireModeText(groupIndex: Int): String {
        return if (groupSpec(groupIndex)?.type == WeaponGroupType.ALTERNATING) "Alternating" else "Linked"
    }

    fun toggleFireMode(groupIndex: Int) {
        val group = groupSpec(groupIndex) ?: return
        group.type = if (group.type == WeaponGroupType.ALTERNATING) WeaponGroupType.LINKED else WeaponGroupType.ALTERNATING
    }

    fun shipModes(): List<String> = loadPersistedShipModes(shipId, loadoutIndex)

    fun isDefaultShipMode(): Boolean = shipModes().let { it.isEmpty() || defaultShipMode in it }

    fun toggleShipMode(mode: String) {
        val modes = shipModes().filter { it != defaultShipMode }
        persistShipModes(shipId, loadoutIndex, if (mode in modes) modes - mode else modes + mode)
    }

    fun resetShipModes() {
        persistShipModes(shipId, loadoutIndex, listOf(defaultShipMode))
    }
}
