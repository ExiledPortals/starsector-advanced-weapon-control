package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.utils.invokeMethodByName
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.coreui.refit.wgd2.WeaponGroupDialogV2

class HoveredWeaponGroup(private val dialog: WeaponGroupDialogV2, private val table: UIComponentAPI) {
    fun slotIds(): List<String> {
        val mouseX = interfaceMouseX()
        val mouseY = interfaceMouseY()
        if (!table.position.containsPoint(mouseX, mouseY)) return emptyList()
        val hoveredRow = dialog.table.rows.firstOrNull { (it as UIComponentAPI).position.containsPoint(mouseX, mouseY) } ?: return emptyList()
        val slotId = (invokeMethodByName("getSlot", hoveredRow) as? WeaponSlotAPI)?.id ?: return emptyList()
        return dialog.variant.weaponGroups.firstOrNull { slotId in it.slots }?.slots ?: listOf(slotId)
    }
}

fun interfaceMouseX(): Float = Global.getSettings().mouseX.toFloat()

fun interfaceMouseY(): Float = Global.getSettings().mouseY.toFloat()

fun PositionAPI.containsPoint(pointX: Float, pointY: Float): Boolean {
    return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height
}
