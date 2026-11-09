package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.getTagTooltip
import com.fs.starfarer.api.util.Misc

private const val CAPTION_WIDTH = 80f
private const val TAG_CHIP_WIDTH = 130f
private const val FIRE_MODE_CHIP_WIDTH = 130f
private const val FIRE_MODE_TOOLTIP = "Linked: weapons fire together. Alternating: weapons take turns."
const val SECTION_GAP = 14f

fun GunneryCanvas.addGroupSection(context: GunneryContext, groupIndex: Int, x: Float, y: Float, width: Float): Float {
    addHeading("Group ${groupIndex + 1}: ${context.weaponSummary(groupIndex)}", x, y, width)
    var nextY = y + HEADING_HEIGHT + ROW_GAP
    groupTagsByCategory(Settings.getCurrentWeaponTagList()).forEach { (category, tags) ->
        nextY = addCategoryRow(context, groupIndex, category, tags, x, nextY, width)
    }
    return addFireModeRow(context, groupIndex, x, nextY) + SECTION_GAP
}

private fun GunneryCanvas.addCategoryRow(
    context: GunneryContext,
    groupIndex: Int,
    category: TagCategory,
    tags: List<String>,
    x: Float,
    y: Float,
    width: Float
): Float {
    addLabel(category.label, x, y, CAPTION_WIDTH, Misc.getGrayColor())
    val grid = ChipGrid(x + CAPTION_WIDTH, y, width - CAPTION_WIDTH, TAG_CHIP_WIDTH)
    tags.forEachIndexed { index, tag ->
        addChip(tagChip(context, groupIndex, tag), grid.xOf(index), grid.yOf(index), TAG_CHIP_WIDTH)
    }
    return grid.bottom(tags.size)
}

private fun tagChip(context: GunneryContext, groupIndex: Int, tag: String): ChipSpec {
    val state = {
        val isActive = tag in context.tagsOf(groupIndex)
        ChipState(tag, isActive, isActive || context.unavailableReason(groupIndex, tag) == null)
    }
    val tooltipText = {
        val reason = if (tag in context.tagsOf(groupIndex)) null else context.unavailableReason(groupIndex, tag)
        getTagTooltip(tag) + (reason?.let { "\n>>$it<<" } ?: "")
    }
    return ChipSpec(state, tooltipText, groupIndex) { context.toggleTag(groupIndex, tag) }
}

private fun GunneryCanvas.addFireModeRow(context: GunneryContext, groupIndex: Int, x: Float, y: Float): Float {
    addLabel("Fire mode", x, y, CAPTION_WIDTH, Misc.getGrayColor())
    val fireMode = ChipSpec({ ChipState(context.fireModeText(groupIndex), false) }, { FIRE_MODE_TOOLTIP }, groupIndex) {
        context.toggleFireMode(groupIndex)
    }
    addChip(fireMode, x + CAPTION_WIDTH, y, FIRE_MODE_CHIP_WIDTH)
    return y + CHIP_HEIGHT + ROW_GAP
}
