package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import kotlin.math.max

class ChipGrid(private val originX: Float, private val originY: Float, availableWidth: Float, private val chipWidth: Float) {
    private val columns = max(1, ((availableWidth + CHIP_GAP) / (chipWidth + CHIP_GAP)).toInt())

    fun xOf(index: Int): Float = originX + (index % columns) * (chipWidth + CHIP_GAP)

    fun yOf(index: Int): Float = originY + (index / columns) * (CHIP_HEIGHT + ROW_GAP)

    fun bottom(count: Int): Float {
        val rows = (count + columns - 1) / columns
        return originY + rows * (CHIP_HEIGHT + ROW_GAP)
    }
}
