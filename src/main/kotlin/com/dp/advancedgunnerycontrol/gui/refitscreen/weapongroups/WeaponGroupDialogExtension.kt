package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.coreui.refit.wgd2.WeaponGroupDialogV2

private const val ANIMATION_SECONDS = 0.25f
private const val SCREEN_MARGIN = 20f
private const val BUTTON_AREA_HEIGHT = 45f
private const val PANEL_RIGHT_MARGIN = 10f
private const val MIN_PREVIEW_HEIGHT = 120f

private enum class DialogPhase { COLLAPSED, EXPANDING, EXPANDED, COLLAPSING }

class WeaponGroupDialogExtension(private val dialog: WeaponGroupDialogV2, private val panels: DialogPanels) {
    private val vanillaParts = VanillaDialogParts(panels)
    private val originalWidth = panels.dialogComponent.position.width
    private val originalHeight = panels.dialogComponent.position.height
    private val toggleButton = GunneryToggleButton(panels.innerPanel) { toggle() }
    private var phase = DialogPhase.COLLAPSED
    private var progress = 0f
    private val hoveredWeaponGroup = HoveredWeaponGroup(dialog, panels.table)
    private var gunneryPanel: GunneryPanel? = null
    private var shipPreview: ShipPreview? = null

    init {
        toggleButton.show(false)
    }

    fun advance(amount: Float, ship: ShipAPI?) {
        keepAllWeaponGroups()
        toggleButton.pollPressed()
        when (phase) {
            DialogPhase.EXPANDING -> grow(amount)
            DialogPhase.COLLAPSING -> shrink(amount)
            DialogPhase.EXPANDED -> advanceExpanded(ship)
            DialogPhase.COLLAPSED -> Unit
        }
    }

    fun restoreVanilla() {
        gunneryPanel?.dispose()
        gunneryPanel = null
        shipPreview?.dispose()
        shipPreview = null
        toggleButton.remove()
        if (phase == DialogPhase.COLLAPSED) return
        vanillaParts.showGroupTable()
        panels.resize(originalWidth, originalHeight)
        vanillaParts.fitTitleToDialogWidth(originalWidth)
        phase = DialogPhase.COLLAPSED
    }

    private fun keepAllWeaponGroups() {
        repeat(WEAPON_GROUP_COUNT) { dialog.variant.getGroup(it) }
    }

    private fun toggle() {
        when (phase) {
            DialogPhase.COLLAPSED -> startExpanding()
            DialogPhase.EXPANDED -> startCollapsing()
            else -> Unit
        }
    }

    private fun startExpanding() {
        vanillaParts.hideGroupTable()
        phase = DialogPhase.EXPANDING
        toggleButton.show(true)
    }

    private fun startCollapsing() {
        gunneryPanel?.dispose()
        gunneryPanel = null
        shipPreview?.dispose()
        shipPreview = null
        phase = DialogPhase.COLLAPSING
        toggleButton.show(false)
    }

    private fun grow(amount: Float) {
        progress = minOf(1f, progress + amount / ANIMATION_SECONDS)
        applyProgress()
        if (progress >= 1f) phase = DialogPhase.EXPANDED
    }

    private fun shrink(amount: Float) {
        progress = maxOf(0f, progress - amount / ANIMATION_SECONDS)
        applyProgress()
        if (progress > 0f) return
        vanillaParts.showGroupTable()
        phase = DialogPhase.COLLAPSED
    }

    private fun applyProgress() {
        val eased = progress * progress * (3f - 2f * progress)
        val width = originalWidth + (targetWidth() - originalWidth) * eased
        val height = originalHeight + (targetHeight() - originalHeight) * eased
        panels.resize(width, height)
        vanillaParts.fitTitleToDialogWidth(width)
    }

    private fun targetWidth(): Float = maxOf(originalWidth, panels.dialogParent.position.width - SCREEN_MARGIN * 2)

    private fun targetHeight(): Float = maxOf(originalHeight, panels.dialogParent.position.height - SCREEN_MARGIN * 2)

    private fun advanceExpanded(ship: ShipAPI?) {
        advanceGunneryPanel(ship)
        advanceShipPreview(ship)
    }

    private fun advanceGunneryPanel(ship: ShipAPI?) {
        val existingPanel = gunneryPanel
        if (existingPanel != null) {
            existingPanel.advance()
            return
        }
        gunneryPanel = createGunneryPanel(ship)?.also { it.show() }
    }

    private fun hoveredSlotIds(): List<String> {
        return hoveredWeaponGroup.slotIds().ifEmpty { gunneryPanel?.hoveredSlotIds().orEmpty() }
    }

    private fun advanceShipPreview(ship: ShipAPI?) {
        val existingPreview = shipPreview
        if (existingPreview != null) {
            existingPreview.advance(ship)
            return
        }
        shipPreview = createShipPreview()?.also {
            it.show()
            it.advance(ship)
        }
    }

    private fun createShipPreview(): ShipPreview? {
        val inner = panels.innerPanel.position
        val table = panels.table.position
        val top = (vanillaParts.statsBottom() ?: table.y) - PANEL_GAP
        val height = top - inner.y - BUTTON_AREA_HEIGHT
        if (height < MIN_PREVIEW_HEIGHT) return null
        val bounds = PreviewBounds(table.x - inner.x, BUTTON_AREA_HEIGHT, table.width, height)
        return ShipPreview(panels.innerPanel, bounds, ::hoveredSlotIds)
    }

    private fun createGunneryPanel(ship: ShipAPI?): GunneryPanel? {
        val member = ship?.fleetMember ?: return null
        val context = GunneryContext(ship, member, dialog.variant)
        return GunneryPanel(panels.innerPanel, { it.rightOfTop(panels.table, PANEL_GAP) }, context, panelWidth(), panelHeight())
    }

    private fun panelWidth(): Float {
        val inner = panels.innerPanel.position
        val table = panels.table.position
        return inner.x + inner.width - (table.x + table.width) - PANEL_GAP - PANEL_RIGHT_MARGIN
    }

    private fun panelHeight(): Float {
        val inner = panels.innerPanel.position
        val tableTop = panels.table.position.y + panels.table.position.height
        return tableTop - inner.y - BUTTON_AREA_HEIGHT
    }
}
