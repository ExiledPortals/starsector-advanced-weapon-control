package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.gui.gunnerywindow.RefitAdvanceFlag
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.GameRenderers
import com.dp.advancedgunnerycontrol.utils.loadGameClass
import com.fs.starfarer.api.Global

private val requiredGameClasses = listOf(
    "com.fs.state.AppDriver",
    "com.fs.graphics.Sprite",
    "com.fs.starfarer.campaign.CampaignState",
    "com.fs.starfarer.campaign.fleet.FleetMember",
    "com.fs.starfarer.campaign.ui.UITable",
    "com.fs.starfarer.combat.CombatViewport",
    "com.fs.starfarer.combat.ai.BasicShipAI",
    "com.fs.starfarer.combat.entities.Ship",
    "com.fs.starfarer.coreui.refit.wgd2.WeaponGroupDialogV2",
    "com.fs.starfarer.ui.impl.StandardTooltipV2Expandable"
)

private class CompatibilityCheck(val feature: String, val consequence: String, val findProblem: () -> String?)

private val compatibilityChecks = listOf(
    CompatibilityCheck("New gunnery UI", "The old Gunnery UI is used.", ::findMissingGameClasses),
    CompatibilityCheck("Weapon arcs", "Arcs are not drawn in the preview.") {
        "renderers not found".takeIf { GameRenderers.instance == null }
    },
    CompatibilityCheck("Gunnery window", "The window falls back to the old UI.") {
        "refit advance flag not found".takeIf { !RefitAdvanceFlag.isSupported }
    }
)

private fun findMissingGameClasses(): String? {
    val missing = requiredGameClasses.filterNot { isLoadable(it) }
    return if (missing.isEmpty()) null else "missing ${missing.joinToString()}"
}

private fun isLoadable(className: String): Boolean {
    return try {
        loadGameClass(className)
        true
    } catch (exception: ReflectiveOperationException) {
        false
    } catch (error: LinkageError) {
        false
    }
}

object GameCompatibilityCheck {
    fun logProblems() {
        val logger = Global.getLogger(GameCompatibilityCheck::class.java)
        compatibilityChecks.forEach { check ->
            findProblem(check)?.let { logger.warn("${check.feature} unavailable: $it. ${check.consequence}") }
        }
    }

    private fun findProblem(check: CompatibilityCheck): String? {
        return try {
            check.findProblem()
        } catch (exception: RuntimeException) {
            exception.toString()
        } catch (error: LinkageError) {
            error.toString()
        }
    }
}
