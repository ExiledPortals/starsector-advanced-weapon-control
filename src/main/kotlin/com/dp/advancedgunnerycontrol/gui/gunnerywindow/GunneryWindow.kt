package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.ClickTargets
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.GunneryCanvas
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.GunneryContext
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.GunneryPanel
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.HEADING_HEIGHT
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.PANEL_GAP
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.PreviewBounds
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.ROW_GAP
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.ScrollingColumn
import com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups.ShipPreview
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.combat.entities.Ship

const val SCREEN_MARGIN = 20f
private const val WINDOW_MARGIN = 10f
private const val LIST_WIDTH = 360f
private const val LIST_HEIGHT_FRACTION = 0.38f

fun fullWindowWidth(): Float = Global.getSettings().screenWidth - SCREEN_MARGIN * 2

fun fullWindowHeight(): Float = Global.getSettings().screenHeight - SCREEN_MARGIN * 2

class GunneryWindow(
    private val root: CustomPanelAPI,
    private val width: Float,
    private val height: Float,
    initialMember: FleetMemberAPI?
) {
    private val members = Global.getSector().playerFleet.membersWithFightersCopy.filter { !it.isFighterWing }
    private val listTop = WINDOW_MARGIN + HEADING_HEIGHT + ROW_GAP
    private val listHeight = height * LIST_HEIGHT_FRACTION
    private val panelLeft = WINDOW_MARGIN + LIST_WIDTH + PANEL_GAP
    private val listTargets = ClickTargets()
    private val preview = ShipPreview(root, previewBounds()) { gunneryPanel?.hoveredSlotIds().orEmpty() }
    private var selectedMember: FleetMemberAPI? = null
    private var previewShip: Ship? = null
    private var gunneryPanel: GunneryPanel? = null

    init {
        buildShipList()
        preview.show()
        (initialMember?.takeIf { it in members } ?: members.firstOrNull())?.let { select(it) }
    }

    fun advance() {
        if (listTargets.firePressed()) listTargets.refreshAll()
        gunneryPanel?.advance()
    }

    fun dispose() {
        gunneryPanel?.dispose()
        gunneryPanel = null
        preview.dispose()
        previewShip?.cleanup()
        previewShip = null
    }

    private fun previewBounds(): PreviewBounds {
        val top = listTop + listHeight + PANEL_GAP
        return PreviewBounds(WINDOW_MARGIN, WINDOW_MARGIN, LIST_WIDTH, height - top - WINDOW_MARGIN)
    }

    private fun buildShipList() {
        GunneryCanvas(root, listTargets).addHeading("Ships", WINDOW_MARGIN, WINDOW_MARGIN, LIST_WIDTH)
        ScrollingColumn(listTargets).attach(root, WINDOW_MARGIN, listTop, LIST_WIDTH, listHeight, 0f) { canvas, contentWidth ->
            canvas.addShipList(members, { selectedMember }, ::select, contentWidth)
        }
    }

    private fun select(member: FleetMemberAPI) {
        gunneryPanel?.dispose()
        previewShip?.cleanup()
        selectedMember = member
        val ship = createPreviewShip(member)
        previewShip = ship
        preview.advance(ship)
        gunneryPanel = ship?.let { createGunneryPanel(it, member) }
    }

    private fun createGunneryPanel(ship: Ship, member: FleetMemberAPI): GunneryPanel {
        val context = GunneryContext(ship, member, member.variant)
        val panelWidth = width - panelLeft - WINDOW_MARGIN
        val panelHeight = height - WINDOW_MARGIN * 2
        return GunneryPanel(root, { it.inTL(panelLeft, WINDOW_MARGIN) }, context, panelWidth, panelHeight).also { it.show() }
    }
}
