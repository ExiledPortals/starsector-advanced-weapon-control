package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.gui.gunnerywindow.GunneryWindowDialog
import com.dp.advancedgunnerycontrol.gui.refitscreen.RefitScreenHandler
import com.dp.advancedgunnerycontrol.gui.suggesttaggui.SuggestedTagGui
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.state.AppDriver
import org.lwjgl.input.Keyboard

class GUIShower : EveryFrameScript {
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    private val refitHandler = RefitScreenHandler()
    private var wasHotkeyDown = false

    companion object{
        var shouldOpenSuggestedTagGui = false
        var shouldOpenAgcGui = false
    }

    override fun advance(amount: Float) {

        refitHandler.advance(amount)

        if (Global.getSector().isInNewGameAdvance || Global.getCurrentState() == GameState.TITLE) return

        if (NewGuiFallback.isActive) {
            advanceNewGui()
        } else {
            openClassicGui()
        }

        if (Global.getSector().campaignUI.isShowingDialog) return

        if (shouldOpenSuggestedTagGui) {
            Global.getSector()?.campaignUI?.showInteractionDialog(SuggestedTagGui(), null)
            shouldOpenSuggestedTagGui = false
        }
    }

    private fun advanceNewGui() {
        if (!wasHotkeyPressed() && !shouldOpenAgcGui) return
        val opened = NewGuiFallback.guarded("gunnery window") { openGunneryWindow() }
        if (opened == null) {
            openClassicGui()
        } else if (opened) {
            shouldOpenAgcGui = false
        }
    }

    private fun openClassicGui() {
        if (Global.getSector().campaignUI.isShowingDialog) return
        if (!Keyboard.isKeyDown(Settings.guiHotkey()) && !shouldOpenAgcGui) return
        Global.getSector()?.campaignUI?.showInteractionDialog(AGCGUI(), Global.getSector().playerFleet)
        shouldOpenAgcGui = false
    }

    private fun wasHotkeyPressed(): Boolean {
        val isHotkeyDown = Keyboard.isKeyDown(Settings.guiHotkey())
        val wasPressed = isHotkeyDown && !wasHotkeyDown
        wasHotkeyDown = isHotkeyDown
        return wasPressed
    }

    private fun openGunneryWindow(): Boolean {
        if (refitHandler.toggleGunneryWindow()) return true
        if (Global.getSector().campaignUI.isShowingDialog) return false
        Global.getSector().campaignUI.showInteractionDialog(GunneryWindowDialog(), Global.getSector().playerFleet)
        return true
    }
}