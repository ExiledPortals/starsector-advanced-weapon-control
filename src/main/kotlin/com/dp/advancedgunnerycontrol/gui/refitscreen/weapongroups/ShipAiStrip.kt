package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.defaultShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.detailedShipModeDescriptions
import com.dp.advancedgunnerycontrol.typesandvalues.shipModeFromString
import com.dp.advancedgunnerycontrol.typesandvalues.shipModeToString

private const val SHIP_MODE_CHIP_WIDTH = 130f

fun GunneryCanvas.addShipAiStrip(context: GunneryContext, x: Float, y: Float, width: Float): Float {
    addHeading("Ship AI", x, y, width)
    val modeNames = Settings.getCurrentShipModes().mapNotNull { shipModeToString[it] }
    val grid = ChipGrid(x, y + HEADING_HEIGHT + ROW_GAP, width, SHIP_MODE_CHIP_WIDTH)
    modeNames.forEachIndexed { index, modeName ->
        addChip(shipModeChip(context, modeName), grid.xOf(index), grid.yOf(index), SHIP_MODE_CHIP_WIDTH)
    }
    return grid.bottom(modeNames.size)
}

private fun shipModeChip(context: GunneryContext, modeName: String): ChipSpec {
    val isDefault = modeName == defaultShipMode
    val description = detailedShipModeDescriptions[shipModeFromString[modeName]] ?: ""
    val state = {
        ChipState(modeName, if (isDefault) context.isDefaultShipMode() else modeName in context.shipModes())
    }
    return ChipSpec(state, { description }) {
        if (isDefault) context.resetShipModes() else context.toggleShipMode(modeName)
    }
}
