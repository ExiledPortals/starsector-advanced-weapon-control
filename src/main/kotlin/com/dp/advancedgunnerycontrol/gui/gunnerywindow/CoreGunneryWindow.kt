package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.UIPanelAPI

class CoreGunneryWindow(private val core: UIPanelAPI, member: FleetMemberAPI?, onCloseRequested: () -> Unit) {
    private val blockerPlugin = InputBlockerPlugin()
    private val windowPlugin = GunneryWindowPlugin(onCloseRequested)
    private val blocker = Global.getSettings().createCustom(screenWidth(), screenHeight(), blockerPlugin)
    private val content = Global.getSettings().createCustom(fullWindowWidth(), fullWindowHeight(), windowPlugin)

    init {
        blockerPlugin.panel = blocker
        windowPlugin.panel = content
        windowPlugin.window = GunneryWindow(content, fullWindowWidth(), fullWindowHeight(), member)
        core.addComponent(blocker).inTL(0f, 0f)
        core.addComponent(content).inTL(SCREEN_MARGIN, SCREEN_MARGIN)
    }

    fun close() {
        core.removeComponent(content)
        core.removeComponent(blocker)
        windowPlugin.window?.dispose()
        windowPlugin.window = null
    }

    private fun screenWidth(): Float = Global.getSettings().screenWidth

    private fun screenHeight(): Float = Global.getSettings().screenHeight
}
