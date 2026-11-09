package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.utils.getChildren
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.campaign.ui.UITable
import com.fs.starfarer.coreui.refit.wgd2.WeaponGroupDialogV2
import com.fs.starfarer.ui.d
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable

private const val TABLE_GAP = 10f
private const val STATS_GAP = 20f
private const val STATS_OFFSET = 20f
private const val TITLE_MARGIN = 34f

class VanillaDialogParts(private val dialog: WeaponGroupDialogV2) {
    private val children: List<UIComponentAPI> = dialog.innerPanel.getChildren()
    private val groupTable: UITable? = children.filterIsInstance<UITable>().firstOrNull { it !== dialog.table }
    private val statsGrid: StandardTooltipV2Expandable? = children.filterIsInstance<StandardTooltipV2Expandable>().firstOrNull()
    private val titleLabel: d? = children.filterIsInstance<d>().firstOrNull()
    private val titleHeight: Float = titleLabel?.position?.height ?: 0f

    fun statsBottom(): Float? = statsGrid?.position?.y

    fun hideGroupTable() {
        val table = groupTable ?: return
        statsGrid?.position?.belowLeft(dialog.table, STATS_GAP)?.setXAlignOffset(STATS_OFFSET)
        dialog.innerPanel.removeComponent(table)
    }

    fun showGroupTable() {
        val table = groupTable ?: return
        dialog.innerPanel.addComponent(table).rightOfTop(dialog.table, TABLE_GAP)
        statsGrid?.position?.belowLeft(table, STATS_GAP)?.setXAlignOffset(STATS_OFFSET)
    }

    fun fitTitleToDialogWidth(dialogWidth: Float) {
        titleLabel?.position?.setSize(dialogWidth - TITLE_MARGIN, titleHeight)
    }
}
