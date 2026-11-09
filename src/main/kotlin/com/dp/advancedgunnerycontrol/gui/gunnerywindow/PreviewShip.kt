package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.campaign.fleet.FleetMember
import com.fs.starfarer.combat.entities.Ship
import com.fs.starfarer.combat.entities.ship.A.C

private const val SETTLE_SECONDS = 1.0E-4f
private const val PREVIEW_FACING = 90f

fun createPreviewShip(member: FleetMemberAPI): Ship? {
    val fleetMember = member as? FleetMember ?: return null
    return try {
        val ship = fleetMember.instantiateForCombat(null, 0, null)
        ship.setFleetMember(fleetMember)
        ship.init()
        C.REFIT_MODE_ADVANCE = true
        ship.advance(SETTLE_SECONDS)
        C.REFIT_MODE_ADVANCE = false
        ship.setFacing(PREVIEW_FACING)
        ship.setRefitMode(true)
        ship
    } catch (exception: RuntimeException) {
        Global.getLogger(FleetMember::class.java).warn("Could not create a preview of ${member.shipName}", exception)
        null
    }
}
