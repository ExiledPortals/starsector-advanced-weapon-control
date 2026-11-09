package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.gui.AGCGUI
import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.gui.actions.CopyLoadoutAction
import com.dp.advancedgunnerycontrol.gui.actions.CopyToSameVariantAction
import com.dp.advancedgunnerycontrol.gui.actions.GUIAction
import com.dp.advancedgunnerycontrol.gui.actions.ResetAction
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.applySuggestedModes

private const val ACTION_BUTTON_WIDTH = 150f

fun GunneryCanvas.addActionRow(context: GunneryContext, x: Float, y: Float, width: Float): Float {
    val actions = listOf(
        ChipSpec({ ChipState("Load suggested", false) }, { "Fill every group with its suggested tags" }) {
            loadSuggestedTags(context)
        },
        guiActionChip(context, { copyLoadoutText(context) }, ::CopyLoadoutAction),
        guiActionChip(context, { "Copy to variant" }, ::CopyToSameVariantAction),
        guiActionChip(context, { "Reset" }, ::ResetAction)
    )
    val grid = ChipGrid(x, y, width, ACTION_BUTTON_WIDTH)
    actions.forEachIndexed { index, action -> addChip(action, grid.xOf(index), grid.yOf(index), ACTION_BUTTON_WIDTH) }
    return grid.bottom(actions.size)
}

private fun guiActionChip(context: GunneryContext, text: () -> String, createAction: (GUIAttributes) -> GUIAction): ChipSpec {
    val attributes = GUIAttributes()
    attributes.ship = context.member
    val action = createAction(attributes)
    return ChipSpec({ ChipState(text(), false) }, { actionTooltip(context, action) }) {
        AGCGUI.storageIndex = context.loadoutIndex
        action.execute()
    }
}

private fun actionTooltip(context: GunneryContext, action: GUIAction): String {
    AGCGUI.storageIndex = context.loadoutIndex
    return action.getTooltip()
}

private fun copyLoadoutText(context: GunneryContext): String {
    val loadoutCount = Settings.maxLoadouts()
    val previousIndex = (context.loadoutIndex + loadoutCount - 1) % loadoutCount
    return "Copy loadout ${previousIndex + 1}"
}

private fun loadSuggestedTags(context: GunneryContext) {
    applySuggestedModes(context.member, context.loadoutIndex, true, context.shipId, context.variant)
}
