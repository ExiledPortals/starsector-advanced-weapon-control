package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

private const val SCROLLBAR_WIDTH = 24f
private const val MAX_CONTENT_HEIGHT = 8000f

class ScrollingColumn(private val clickTargets: ClickTargets) {
    private var element: TooltipMakerAPI? = null

    val scrollOffset: Float
        get() = element?.externalScroller?.yOffset ?: 0f

    fun attach(
        parent: CustomPanelAPI,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        restoredOffset: Float,
        layout: (GunneryCanvas, Float) -> Float
    ) {
        val contentWidth = width - SCROLLBAR_WIDTH
        val contentPanel = Global.getSettings().createCustom(contentWidth, MAX_CONTENT_HEIGHT, BaseCustomUIPanelPlugin())
        val contentHeight = layout(GunneryCanvas(contentPanel, clickTargets), contentWidth)
        contentPanel.position.setSize(contentWidth, contentHeight)
        val newElement = parent.createUIElement(width, height, true)
        newElement.addCustom(contentPanel, 0f)
        parent.addUIElement(newElement).inTL(left, top)
        newElement.externalScroller?.yOffset = restoredOffset
        element = newElement
    }

    fun detach() {
        element = null
    }
}
