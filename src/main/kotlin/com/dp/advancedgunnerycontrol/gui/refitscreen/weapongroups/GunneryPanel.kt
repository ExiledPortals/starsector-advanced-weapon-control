package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.loadAllTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI

const val PANEL_GAP = 10f

class GunneryPanel(
    private val parent: UIPanelAPI,
    private val place: (PositionAPI) -> Unit,
    private val context: GunneryContext,
    private val width: Float,
    private val height: Float
) {
    private val clickTargets = ClickTargets()
    private val scrollingColumn = ScrollingColumn(clickTargets)
    private var panel: CustomPanelAPI? = null
    private var headerBottom = 0f
    private var shownSignature: List<List<String>?> = emptyList()

    fun show() {
        Settings.isAdvancedMode = true
        rebuild()
    }

    fun advance() {
        if (clickTargets.firePressed()) {
            context.removeUnavailableTags()
            clickTargets.refreshAll()
        }
        if (context.groupSignature() != shownSignature) rebuild()
    }

    fun hoveredSlotIds(): List<String> {
        val area = panel?.position ?: return emptyList()
        val mouseX = interfaceMouseX()
        val mouseY = interfaceMouseY()
        val isInScrollingArea = mouseX >= area.x && mouseX <= area.x + area.width &&
            mouseY >= area.y && mouseY <= area.y + area.height - headerBottom
        if (!isInScrollingArea) return emptyList()
        val groupIndex = clickTargets.hoveredGroup(mouseX, mouseY) ?: return emptyList()
        return context.groupSpec(groupIndex)?.slots.orEmpty()
    }

    fun dispose() {
        detach()
        clickTargets.clear()
    }

    private fun rebuild() {
        val scrollOffset = scrollingColumn.scrollOffset
        detach()
        clickTargets.clear()
        context.removeUnavailableTags()
        Settings.hotAddTags(loadAllTags(context.member, context.shipId))
        shownSignature = context.groupSignature()
        val newPanel = Global.getSettings().createCustom(width, height, BaseCustomUIPanelPlugin())
        headerBottom = GunneryCanvas(newPanel, clickTargets).addHeaderRow(context, 0f, 0f, width)
        scrollingColumn.attach(newPanel, 0f, headerBottom, width, height - headerBottom, scrollOffset, ::layoutSections)
        place(parent.addComponent(newPanel))
        panel = newPanel
    }

    private fun layoutSections(canvas: GunneryCanvas, contentWidth: Float): Float {
        var nextY = canvas.addShipAiStrip(context, 0f, 0f, contentWidth) + SECTION_GAP
        context.filledGroups().forEach { groupIndex ->
            nextY = canvas.addGroupSection(context, groupIndex, 0f, nextY, contentWidth)
        }
        return nextY
    }

    private fun detach() {
        panel?.let { parent.removeComponent(it) }
        panel = null
        scrollingColumn.detach()
    }
}
