package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.combat.CombatViewport
import com.fs.starfarer.combat.entities.Ship
import org.lwjgl.opengl.GL11

private const val FILL_FRACTION = 0.9f
private const val VIEWPORT_EXTENT = 400f

class ShipFrame(val originX: Float, val originY: Float, val zoom: Float) {
    fun screenX(shipX: Float): Float = originX + shipX * zoom

    fun screenY(shipY: Float): Float = originY + shipY * zoom
}

fun frameShipIn(ship: Ship, area: PositionAPI): ShipFrame {
    val sprite = ship.sprite
    val zoom = minOf(area.width, area.height) * FILL_FRACTION / (ship.collisionRadius * 2f)
    val originX = Math.round(area.centerX + (sprite.centerX - sprite.width / 2f) * zoom).toFloat()
    val originY = Math.round(area.centerY + (sprite.centerY - sprite.height / 2f) * zoom).toFloat()
    return ShipFrame(originX, originY, zoom)
}

class ShipDrawing {
    private val viewport = CombatViewport(-VIEWPORT_EXTENT / 2f, -VIEWPORT_EXTENT / 2f, VIEWPORT_EXTENT, VIEWPORT_EXTENT)

    fun draw(ship: Ship, frame: ShipFrame, alpha: Float) {
        viewport.alphaMult = alpha
        ship.setAlphaMult(alpha)
        ship.setRefitMode(true)
        val layers = ship.activeLayers
        GL11.glPushMatrix()
        GL11.glTranslatef(frame.originX, frame.originY, 0f)
        GL11.glScalef(frame.zoom, frame.zoom, 1f)
        layers.forEach { ship.render(it, viewport, false) }
        layers.forEach { ship.renderDecorativeModules(it, viewport) }
        layers.forEach { ship.renderNonDecorativeModules(it, viewport) }
        GL11.glPopMatrix()
    }
}
