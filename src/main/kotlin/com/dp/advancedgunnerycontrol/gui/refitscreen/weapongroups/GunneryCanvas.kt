package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color
import kotlin.math.min

const val CHIP_HEIGHT = 20f
const val CHIP_GAP = 6f
const val ROW_GAP = 4f
const val HEADING_HEIGHT = 26f
private const val LABEL_OFFSET = 3f
private const val TOOLTIP_CHARACTER_WIDTH = 7f
private const val TOOLTIP_MAX_WIDTH = 850f

class ChipState(val text: String, val isChecked: Boolean, val isEnabled: Boolean = true)

class ChipSpec(
    val state: () -> ChipState,
    val tooltipText: (() -> String)? = null,
    val hoverGroup: Int? = null,
    val onPress: () -> Unit
)

class GunneryCanvas(private val panel: CustomPanelAPI, private val clickTargets: ClickTargets) {

    fun addLabel(text: String, x: Float, y: Float, width: Float, color: Color = Misc.getTextColor()) {
        val element = panel.createUIElement(width, CHIP_HEIGHT, false)
        element.addPara(text, color, 0f)
        panel.addUIElement(element).inTL(x, y + LABEL_OFFSET)
    }

    fun addHeading(text: String, x: Float, y: Float, width: Float) {
        val element = panel.createUIElement(width, HEADING_HEIGHT, false)
        element.addSectionHeading(text, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.LMID, 0f)
        panel.addUIElement(element).inTL(x, y)
    }

    fun addChip(spec: ChipSpec, x: Float, y: Float, width: Float) {
        val element = panel.createUIElement(width, CHIP_HEIGHT, false)
        val button = element.addAreaCheckbox(
            spec.state().text, null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
            width, CHIP_HEIGHT, 0f
        )
        spec.tooltipText?.let { element.addTooltipToPrevious(createTooltip(it), TooltipMakerAPI.TooltipLocation.BELOW) }
        panel.addUIElement(element).inTL(x, y)
        clickTargets.add(button, spec)
    }

    private fun createTooltip(textSupplier: () -> String): TooltipMakerAPI.TooltipCreator {
        return object : TooltipMakerAPI.TooltipCreator {
            override fun isTooltipExpandable(tooltipParameter: Any?): Boolean = false

            override fun getTooltipWidth(tooltipParameter: Any?): Float {
                return min(textSupplier().length * TOOLTIP_CHARACTER_WIDTH, TOOLTIP_MAX_WIDTH)
            }

            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParameter: Any?) {
                tooltip?.addPara(textSupplier(), Color.GREEN, 5f)
            }
        }
    }
}
