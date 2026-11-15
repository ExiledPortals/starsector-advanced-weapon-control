package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSlotAPI

private const val ARC_RADIUS = 150f

fun drawWeaponArcs(ship: ShipAPI, slots: List<WeaponSlotAPI>, frame: ShipFrame, alpha: Float) {
    val renderers = GameRenderers.instance ?: return
    slots.forEach { drawArc(renderers, ship, it, frame, alpha) }
}

private fun drawArc(renderers: GameRenderers, ship: ShipAPI, slot: WeaponSlotAPI, frame: ShipFrame, alpha: Float) {
    val position = slot.computePosition(ship)
    val centerX = frame.screenX(position.x)
    val centerY = frame.screenY(position.y)
    val midAngle = slot.computeMidArcAngle(ship)
    val startAngle = midAngle - slot.arc / 2f
    val endAngle = midAngle + slot.arc / 2f
    val radius = ARC_RADIUS * frame.zoom
    val style = StrokeStyle(renderers.weaponColor(slot.weaponType), lineWidth(slot), alpha)
    renderers.drawMount(slot, centerX, centerY, frame.zoom, alpha)
    drawEdge(renderers, centerX, centerY, startAngle, radius, style)
    drawEdge(renderers, centerX, centerY, endAngle, radius, style)
    renderers.drawArc(ArcShape(centerX, centerY, startAngle, endAngle, radius), style)
}

private fun drawEdge(renderers: GameRenderers, centerX: Float, centerY: Float, angle: Float, radius: Float, style: StrokeStyle) {
    val radians = Math.toRadians(angle.toDouble())
    val endX = centerX + Math.cos(radians).toFloat() * radius
    val endY = centerY + Math.sin(radians).toFloat() * radius
    renderers.drawEdge(centerX, centerY, endX, endY, style)
}

private fun lineWidth(slot: WeaponSlotAPI): Float {
    return when (slot.slotSize) {
        WeaponAPI.WeaponSize.LARGE -> 4f
        WeaponAPI.WeaponSize.MEDIUM -> 3f
        else -> 2f
    }
}
