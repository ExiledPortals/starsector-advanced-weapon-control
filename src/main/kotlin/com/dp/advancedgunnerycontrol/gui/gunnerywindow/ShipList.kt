package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.CHIP_HEIGHT
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.ChipSpec
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.ChipState
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.GunneryCanvas
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.ROW_GAP
import com.fs.starfarer.api.fleet.FleetMemberAPI

fun GunneryCanvas.addShipList(
    members: List<FleetMemberAPI>,
    selectedMember: () -> FleetMemberAPI?,
    onSelect: (FleetMemberAPI) -> Unit,
    width: Float
): Float {
    members.forEachIndexed { index, member ->
        val chip = ChipSpec({ ChipState(shipLabel(member), member === selectedMember()) }) { onSelect(member) }
        addChip(chip, 0f, index * (CHIP_HEIGHT + ROW_GAP), width)
    }
    return members.size * (CHIP_HEIGHT + ROW_GAP)
}

private fun shipLabel(member: FleetMemberAPI): String = "${member.shipName} (${member.hullSpec.hullNameWithDashClass})"
