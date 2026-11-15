package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.utils.getChildren
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.campaign.ui.UITable
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable

private const val TABLE_GAP = 10f
private const val STATS_GAP = 20f
private const val STATS_OFFSET = 20f
private const val TITLE_MARGIN = 34f

class VanillaDialogParts(private val panels: DialogPanels) {
    private val children: List<UIComponentAPI> = panels.innerPanel.getChildren()
    private val groupTable: UITable? = children.filterIsInstance<UITable>().firstOrNull { it !== panels.table }
    private val statsGrid: UIComponentAPI? = children.filterIsInstance<StandardTooltipV2Expandable>().firstOrNull()
    private val titleLabel: LabelAPI? = children.filterIsInstance<LabelAPI>().firstOrNull()
    private val titleHeight: Float = titleLabel?.position?.height ?: 0f

    fun statsBottom(): Float? = statsGrid?.position?.y

    fun hideGroupTable() {
        val table = groupTable ?: return
        statsGrid?.position?.belowLeft(panels.table, STATS_GAP)?.setXAlignOffset(STATS_OFFSET)
        panels.innerPanel.removeComponent(table)
    }

    fun showGroupTable() {
        val table = groupTable ?: return
        panels.innerPanel.addComponent(table).rightOfTop(panels.table, TABLE_GAP)
        statsGrid?.position?.belowLeft(table, STATS_GAP)?.setXAlignOffset(STATS_OFFSET)
    }

    fun fitTitleToDialogWidth(dialogWidth: Float) {
        titleLabel?.position?.setSize(dialogWidth - TITLE_MARGIN, titleHeight)
    }
}
