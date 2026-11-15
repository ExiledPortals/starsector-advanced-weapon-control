package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.CutStyle
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard

private const val BUTTON_WIDTH = 143f
private const val BUTTON_HEIGHT = 25f
private const val BUTTON_LEFT = 203f
private const val BUTTON_BOTTOM = 10f

private class PressListener : BaseCustomUIPanelPlugin() {
    var wasPressed = false

    override fun buttonPressed(buttonId: Any?) {
        wasPressed = true
    }
}

class GunneryToggleButton(private val innerPanel: UIPanelAPI, private val onPressed: () -> Unit) {
    private var panel: CustomPanelAPI? = null
    private var button: ButtonAPI? = null
    private var pressListener = PressListener()

    fun show(isExpanded: Boolean) {
        remove()
        pressListener = PressListener()
        val newPanel = Global.getSettings().createCustom(BUTTON_WIDTH, BUTTON_HEIGHT, pressListener)
        val element = newPanel.createUIElement(BUTTON_WIDTH, BUTTON_HEIGHT, false)
        element.setButtonFontOrbitron20()
        val newButton = element.addButton(
            if (isExpanded) "Collapse" else "Gunnery",
            "gunnery",
            Misc.getBasePlayerColor(),
            Misc.getDarkPlayerColor(),
            Alignment.MID,
            CutStyle.TL_BR,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            0f
        )
        newButton.setShortcut(Keyboard.KEY_E, true)
        newPanel.addUIElement(element).inTL(0f, 0f)
        innerPanel.addComponent(newPanel).inBL(BUTTON_LEFT, BUTTON_BOTTOM)
        panel = newPanel
        button = newButton
    }

    fun pollPressed() {
        val pressedButton = button ?: return
        if (!pressedButton.isChecked && !pressListener.wasPressed) return
        pressedButton.isChecked = false
        pressListener.wasPressed = false
        onPressed()
    }

    fun remove() {
        panel?.let { innerPanel.removeComponent(it) }
        panel = null
        button = null
    }
}
