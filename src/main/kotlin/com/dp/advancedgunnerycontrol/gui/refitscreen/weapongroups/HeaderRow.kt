package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.Values

private const val LOADOUT_LABEL_WIDTH = 60f
private const val LOADOUT_TAB_WIDTH = 130f

fun GunneryCanvas.addHeaderRow(context: GunneryContext, x: Float, y: Float, width: Float): Float {
    addLoadoutTabs(context, x, y)
    return addActionRow(context, x, y + CHIP_HEIGHT + ROW_GAP * 2, width) + ROW_GAP
}

private fun GunneryCanvas.addLoadoutTabs(context: GunneryContext, x: Float, y: Float) {
    addLabel("Loadout", x, y, LOADOUT_LABEL_WIDTH)
    (0 until Settings.maxLoadouts()).forEach { index ->
        val tab = ChipSpec({ ChipState(loadoutTabText(index), index == context.loadoutIndex) }) {
            Values.storageIndex = index
        }
        addChip(tab, x + LOADOUT_LABEL_WIDTH + index * (LOADOUT_TAB_WIDTH + CHIP_GAP), y, LOADOUT_TAB_WIDTH)
    }
}

private fun loadoutTabText(index: Int): String {
    val name = Settings.loadoutNames().getOrNull(index) ?: return "${index + 1}"
    return "${index + 1}: $name"
}
