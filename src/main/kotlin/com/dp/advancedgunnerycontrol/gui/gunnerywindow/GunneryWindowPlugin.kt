package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11

private const val BACKDROP_ALPHA = 0.96f
private const val DIM_ALPHA = 0.55f

private fun fillRectangle(x: Float, y: Float, width: Float, height: Float, red: Float, green: Float, blue: Float, alpha: Float) {
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_CURRENT_BIT or GL11.GL_COLOR_BUFFER_BIT)
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glColor4f(red, green, blue, alpha)
    GL11.glRectf(x, y, x + width, y + height)
    GL11.glPopAttrib()
}

class GunneryWindowPlugin(private val onCloseRequested: () -> Unit) : BaseCustomUIPanelPlugin() {
    var window: GunneryWindow? = null
    var panel: CustomPanelAPI? = null

    override fun renderBelow(alphaMult: Float) {
        val area = panel?.position ?: return
        fillRectangle(area.x, area.y, area.width, area.height, 0.04f, 0.05f, 0.07f, BACKDROP_ALPHA * alphaMult)
    }

    override fun advance(amount: Float) {
        window?.advance()
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
        val escapePress = events?.firstOrNull { !it.isConsumed && it.isKeyDownEvent && it.eventValue == Keyboard.KEY_ESCAPE } ?: return
        escapePress.consume()
        onCloseRequested()
    }
}

class InputBlockerPlugin : BaseCustomUIPanelPlugin() {
    var panel: CustomPanelAPI? = null

    override fun renderBelow(alphaMult: Float) {
        val area = panel?.position ?: return
        fillRectangle(area.x, area.y, area.width, area.height, 0f, 0f, 0f, DIM_ALPHA * alphaMult)
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
        events?.filter { !it.isConsumed }?.forEach { it.consume() }
    }
}
