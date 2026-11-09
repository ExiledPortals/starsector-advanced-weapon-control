package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.fs.starfarer.api.ui.ButtonAPI

class ClickTargets {
    private class Target(val button: ButtonAPI, val spec: ChipSpec) {
        private var expectedChecked = false

        fun isPressed(): Boolean = button.isChecked != expectedChecked

        fun refresh() {
            val state = spec.state()
            expectedChecked = state.isChecked
            button.text = state.text
            button.isChecked = state.isChecked
            button.isEnabled = state.isEnabled
        }
    }

    private val targets = mutableListOf<Target>()

    fun add(button: ButtonAPI, spec: ChipSpec) {
        val target = Target(button, spec)
        target.refresh()
        targets.add(target)
    }

    fun clear() {
        targets.clear()
    }

    fun hoveredGroup(pointX: Float, pointY: Float): Int? {
        return targets.firstOrNull { it.spec.hoverGroup != null && it.button.position.containsPoint(pointX, pointY) }?.spec?.hoverGroup
    }

    fun refreshAll() {
        targets.forEach { it.refresh() }
    }

    fun firePressed(): Boolean {
        val pressed = targets.firstOrNull { it.isPressed() } ?: return false
        pressed.spec.onPress()
        return true
    }
}
