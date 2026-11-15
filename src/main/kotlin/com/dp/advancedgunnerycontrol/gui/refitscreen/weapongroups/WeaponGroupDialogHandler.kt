package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.getChildren
import com.dp.advancedgunnerycontrol.utils.invokeMethodByName
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.coreui.refit.wgd2.WeaponGroupDialogV2

private const val MAX_LOOKUP_FAILURES = 3

class WeaponGroupDialogHandler {
    private var attachedDialog: WeaponGroupDialogV2? = null
    private var extension: WeaponGroupDialogExtension? = null
    private var lookupFailures = 0

    fun advance(amount: Float, core: UIPanelAPI?, ship: ShipAPI?) {
        val dialog = core?.let { findDialog(it) }
        if (dialog == null) {
            release()
            return
        }
        if (dialog !== attachedDialog) attach(dialog)
        extension?.advance(amount, ship)
    }

    fun release() {
        attachedDialog = null
        extension = null
    }

    fun abandon() {
        extension?.restoreVanilla()
        release()
    }

    private fun findDialog(core: UIPanelAPI): WeaponGroupDialogV2? {
        if (lookupFailures >= MAX_LOOKUP_FAILURES) return null
        val dialogParent = invokeMethodByName(
            "getDialogParentForSubDialog", core, narrativeContext = "WeaponGroupDialogHandler, finding the dialog parent"
        ) as? UIPanelAPI
        if (dialogParent == null) {
            lookupFailures++
            return null
        }
        return dialogParent.getChildren().filterIsInstance<WeaponGroupDialogV2>().firstOrNull()
    }

    private fun attach(dialog: WeaponGroupDialogV2) {
        attachedDialog = dialog
        extension = if (Settings.enablePersistentModes()) DialogPanels.create(dialog)?.let { WeaponGroupDialogExtension(dialog, it) } else null
    }
}
