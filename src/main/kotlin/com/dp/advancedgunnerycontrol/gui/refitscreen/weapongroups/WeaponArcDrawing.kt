package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.graphics.oOoO
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.fs.starfarer.renderers.E
import com.fs.starfarer.renderers.M
import java.awt.Color

private const val ARC_RADIUS = 150f
private const val LINE_TEXTURE = "graphics/hud/line4x4.png"
private const val MAX_ALPHA = 255f
private const val MOUNT_RADIUS = 40f
private const val MOUNT_LINE_WIDTH = 2.5f
private const val BUILT_IN_MOUNT_ALPHA = 0.3f
private const val BUILT_IN_MOUNT_SCALE = 0.66f

private val lineTexture by lazy { oOoO.Ò00000(LINE_TEXTURE) }

fun drawWeaponArcs(ship: ShipAPI, slots: List<WeaponSlotAPI>, frame: ShipFrame, alpha: Float) {
    slots.forEach { drawArc(ship, it, frame, alpha) }
}

private fun drawArc(ship: ShipAPI, slot: WeaponSlotAPI, frame: ShipFrame, alpha: Float) {
    val position = slot.computePosition(ship)
    val centerX = frame.screenX(position.x)
    val centerY = frame.screenY(position.y)
    val midAngle = slot.computeMidArcAngle(ship)
    val startAngle = midAngle - slot.arc / 2f
    val endAngle = midAngle + slot.arc / 2f
    val radius = ARC_RADIUS * frame.zoom
    val color = M.o00000(slot.weaponType)
    val width = lineWidth(slot)
    drawMount(slot, centerX, centerY, frame.zoom, alpha)
    drawEdge(centerX, centerY, startAngle, radius, width, color, alpha)
    drawEdge(centerX, centerY, endAngle, radius, width, color, alpha)
    E.o00000(lineTexture, centerX, centerY, startAngle, endAngle, radius, width, color, (MAX_ALPHA * alpha).toInt(), 0, 1f)
}

private fun drawMount(slot: WeaponSlotAPI, centerX: Float, centerY: Float, zoom: Float, alpha: Float) {
    val mount = M(slot.weaponType, slot.slotSize, MOUNT_RADIUS, MOUNT_LINE_WIDTH, slot.isBuiltIn)
    if (slot.isBuiltIn) {
        mount.Ô00000(BUILT_IN_MOUNT_ALPHA)
        mount.o00000(zoom * BUILT_IN_MOUNT_SCALE)
    } else {
        mount.o00000(zoom)
    }
    mount.o00000(centerX, centerY, alpha)
}

private fun drawEdge(centerX: Float, centerY: Float, angle: Float, radius: Float, width: Float, color: Color, alpha: Float) {
    val radians = Math.toRadians(angle.toDouble())
    val endX = centerX + Math.cos(radians).toFloat() * radius
    val endY = centerY + Math.sin(radians).toFloat() * radius
    E.o00000(lineTexture, centerX, centerY, endX, endY, width, color, 0f, alpha, 0f, false)
}

private fun lineWidth(slot: WeaponSlotAPI): Float {
    return when (slot.slotSize) {
        WeaponAPI.WeaponSize.LARGE -> 4f
        WeaponAPI.WeaponSize.MEDIUM -> 3f
        else -> 2f
    }
}
