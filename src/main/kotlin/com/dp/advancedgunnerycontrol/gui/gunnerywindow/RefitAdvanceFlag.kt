package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.dp.advancedgunnerycontrol.utils.booleanType
import com.dp.advancedgunnerycontrol.utils.declaredMethods
import com.dp.advancedgunnerycontrol.utils.gameLookup
import com.fs.starfarer.combat.entities.Ship
import java.lang.invoke.MethodHandle

private const val FLAG_NAME = "REFIT_MODE_ADVANCE"
private const val ANCHOR_METHOD = "addLaunchBay"

object RefitAdvanceFlag {
    private val setter: MethodHandle? by lazy { findSetter() }

    val isSupported: Boolean get() = setter != null

    fun set(enabled: Boolean) {
        setter?.invoke(enabled)
    }

    private fun findSetter(): MethodHandle? {
        val holder = declaredMethods(Ship::class.java)
            .singleOrNull { it.name == ANCHOR_METHOD && it.parameterTypes.size == 1 }
            ?.parameterTypes?.single() ?: return null
        return try {
            gameLookup.findStaticSetter(holder, FLAG_NAME, booleanType)
        } catch (exception: ReflectiveOperationException) {
            null
        }
    }
}
