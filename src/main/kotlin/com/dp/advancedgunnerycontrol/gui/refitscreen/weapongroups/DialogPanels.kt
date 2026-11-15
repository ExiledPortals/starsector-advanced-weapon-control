package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.utils.invokeMethodByName
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.coreui.refit.wgd2.WeaponGroupDialogV2

class DialogPanels private constructor(
    val dialogComponent: UIComponentAPI,
    val innerPanel: UIPanelAPI,
    val dialogParent: UIPanelAPI,
    val table: UIComponentAPI
) {
    companion object {
        fun create(dialog: WeaponGroupDialogV2): DialogPanels? {
            val innerPanel = invokeMethodByName("getInnerPanel", dialog) as? UIPanelAPI ?: return null
            val dialogParent = invokeMethodByName("getDialogParent", dialog) as? UIPanelAPI ?: return null
            return DialogPanels(dialog, innerPanel, dialogParent, dialog.table)
        }
    }
}
