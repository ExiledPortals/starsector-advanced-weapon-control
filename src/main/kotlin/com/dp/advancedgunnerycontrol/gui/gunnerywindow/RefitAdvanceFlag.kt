package com.dp.advancedgunnerycontrol.gui.gunnerywindow

import com.dp.advancedgunnerycontrol.utils.booleanType
import com.dp.advancedgunnerycontrol.utils.gameLookup
import com.dp.advancedgunnerycontrol.utils.loadGameClass
import com.fs.starfarer.combat.entities.Ship
import java.io.File
import java.io.IOException
import java.lang.invoke.MethodHandle
import java.net.URISyntaxException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private const val FLAG_NAME = "REFIT_MODE_ADVANCE"
private const val CLASS_EXTENSION = ".class"

private val knownFlagHolders = listOf(
    "com.fs.starfarer.combat.entities.ship.A.C",
    "com.fs.starfarer.combat.entities.ship.A.B"
)

object RefitAdvanceFlag {
    private val setter: MethodHandle? by lazy {
        knownFlagHolders.firstNotNullOfOrNull { findSetter(it) } ?: searchGameJar()
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

    private fun searchGameJar(): MethodHandle? {
        val jarFile = gameJarFile() ?: return null
        return try {
            ZipFile(jarFile).use { jar ->
                jar.entries().asSequence()
                    .filter { it.name.endsWith(CLASS_EXTENSION) && mentionsFlag(jar, it) }
                    .map { it.name.removeSuffix(CLASS_EXTENSION).replace('/', '.') }
                    .firstNotNullOfOrNull { findSetter(it) }
            }
        } catch (exception: IOException) {
            null
        }
    }

    private fun gameJarFile(): File? {
        val location = Ship::class.java.protectionDomain?.codeSource?.location ?: return null
        return try {
            File(location.toURI())
        } catch (exception: URISyntaxException) {
            null
        }
    }

    private fun mentionsFlag(jar: ZipFile, entry: ZipEntry): Boolean {
        val content = jar.getInputStream(entry).use { String(it.readBytes(), Charsets.ISO_8859_1) }
        return content.contains(FLAG_NAME)
    }
}
