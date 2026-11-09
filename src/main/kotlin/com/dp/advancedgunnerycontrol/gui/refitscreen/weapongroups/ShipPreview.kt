package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.combat.entities.Ship
import org.lwjgl.opengl.GL11

class PreviewBounds(val left: Float, val bottom: Float, val width: Float, val height: Float)

private class ShipPreviewPlugin(private val hoveredSlotIds: () -> List<String>) : BaseCustomUIPanelPlugin() {
    private val shipDrawing = ShipDrawing()
    var panel: CustomPanelAPI? = null
    var ship: Ship? = null

    override fun render(alphaMult: Float) {
        val area = panel?.position ?: return
        val shownShip = ship ?: return
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        try {
            clipTo(area)
            val frame = frameShipIn(shownShip, area)
            shipDrawing.draw(shownShip, frame, alphaMult)
            drawWeaponArcs(shownShip, hoveredSlots(shownShip), frame, alphaMult)
        } finally {
            GL11.glPopAttrib()
        }
    }

    private fun hoveredSlots(shownShip: Ship) = hoveredSlotIds().mapNotNull { shownShip.hullSpec.getWeaponSlotAPI(it) }

    private fun clipTo(area: PositionAPI) {
        val scale = Global.getSettings().screenScaleMult
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor((area.x * scale).toInt(), (area.y * scale).toInt(), (area.width * scale).toInt(), (area.height * scale).toInt())
    }
}

class ShipPreview(
    private val parent: UIPanelAPI,
    private val bounds: PreviewBounds,
    hoveredSlotIds: () -> List<String>
) {
    private val plugin = ShipPreviewPlugin(hoveredSlotIds)
    private var panel: CustomPanelAPI? = null

    fun show() {
        val newPanel = Global.getSettings().createCustom(bounds.width, bounds.height, plugin)
        plugin.panel = newPanel
        parent.addComponent(newPanel).inBL(bounds.left, bounds.bottom)
        panel = newPanel
    }

    fun advance(ship: ShipAPI?) {
        plugin.ship = ship as? Ship
    }

    fun dispose() {
        panel?.let { parent.removeComponent(it) }
        panel = null
        plugin.panel = null
    }
}
