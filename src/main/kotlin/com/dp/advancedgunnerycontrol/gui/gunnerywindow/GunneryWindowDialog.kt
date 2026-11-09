package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

private class GunneryWindowDelegate(private val dialog: InteractionDialogAPI) : CustomVisualDialogDelegate {
    private var callbacks: CustomVisualDialogDelegate.DialogCallbacks? = null
    private val plugin = GunneryWindowPlugin { callbacks?.dismissDialog() }

    override fun init(panel: CustomPanelAPI, callbacks: CustomVisualDialogDelegate.DialogCallbacks) {
        this.callbacks = callbacks
        plugin.panel = panel
        plugin.window = GunneryWindow(panel, fullWindowWidth(), fullWindowHeight(), Global.getSector().playerFleet.flagship)
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin = plugin

    override fun getNoiseAlpha(): Float = 0f

    override fun advance(amount: Float) = Unit

    override fun reportDismissed(option: Int) {
        plugin.window?.dispose()
        plugin.window = null
        dialog.dismiss()
    }
}

class GunneryWindowDialog : InteractionDialogPlugin {
    override fun init(dialog: InteractionDialogAPI?) {
        dialog?.showCustomVisualDialog(fullWindowWidth(), fullWindowHeight(), GunneryWindowDelegate(dialog))
    }

    override fun optionSelected(optionText: String?, optionData: Any?) = Unit

    override fun optionMousedOver(optionText: String?, optionData: Any?) = Unit

    override fun advance(amount: Float) = Unit

    override fun backFromEngagement(battleResult: EngagementResultAPI?) = Unit

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}
