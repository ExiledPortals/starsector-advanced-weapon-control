package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.dp.advancedgunnerycontrol.utils.booleanType
import com.dp.advancedgunnerycontrol.utils.gameClassesMentioning
import com.dp.advancedgunnerycontrol.utils.gameLookup
import com.dp.advancedgunnerycontrol.utils.loadGameClass
import java.lang.invoke.MethodHandle

private const val FLAG_NAME = "REFIT_MODE_ADVANCE"

object RefitAdvanceFlag {
    private val setter: MethodHandle? by lazy {
        gameClassesMentioning(FLAG_NAME).firstNotNullOfOrNull { findSetter(it) }
    }

    val isSupported: Boolean get() = setter != null

    fun set(enabled: Boolean) {
        setter?.invoke(enabled)
    }

    private fun findSetter(holderName: String): MethodHandle? {
        return try {
            gameLookup.findStaticSetter(loadGameClass(holderName), FLAG_NAME, booleanType)
        } catch (exception: ReflectiveOperationException) {
            null
        } catch (error: LinkageError) {
            null
        }
    }
}
