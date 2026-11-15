package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global

private const val FALLBACK_MESSAGE = "Gunnery UI failed to load. Using the old one."

object NewGuiFallback {
    private var failed = false

    val isActive: Boolean get() = Settings.useWeaponGroupsGui() && !failed

    fun <T : Any> guarded(where: String, block: () -> T): T? {
        return try {
            block()
        } catch (error: LinkageError) {
            fail(where, error)
            null
        } catch (exception: RuntimeException) {
            fail(where, exception)
            null
        }
    }

    private fun fail(where: String, cause: Throwable) {
        if (failed) return
        failed = true
        Global.getLogger(NewGuiFallback::class.java).error("The new gunnery UI failed in $where. Using the old UI.", cause)
        Global.getSector()?.campaignUI?.addMessage(FALLBACK_MESSAGE)
    }
}
