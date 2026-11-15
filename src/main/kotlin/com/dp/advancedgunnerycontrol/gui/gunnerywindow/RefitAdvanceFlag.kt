package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.dp.advancedgunnerycontrol.utils.booleanType
import com.dp.advancedgunnerycontrol.utils.gameLookup
import com.dp.advancedgunnerycontrol.utils.loadGameClass
import java.lang.invoke.MethodHandle

private const val FLAG_NAME = "REFIT_MODE_ADVANCE"

private val knownFlagHolders = listOf(
    "com.fs.starfarer.combat.entities.ship.A.C",
    "com.fs.starfarer.combat.entities.ship.A.B"
)

object RefitAdvanceFlag {
    private val setter: MethodHandle? by lazy { knownFlagHolders.firstNotNullOfOrNull { findSetter(it) } }

    val isSupported: Boolean get() = setter != null

    fun set(enabled: Boolean) {
        setter?.invoke(enabled)
    }

    private fun findSetter(holderName: String): MethodHandle? {
        return try {
            gameLookup.findStaticSetter(loadGameClass(holderName), FLAG_NAME, booleanType)
        } catch (exception: ReflectiveOperationException) {
            null
        }
    }
}
