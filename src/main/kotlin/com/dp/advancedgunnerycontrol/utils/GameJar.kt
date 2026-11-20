package com.dp.advancedgunnerycontrol.utils

import com.fs.starfarer.combat.entities.Ship
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private const val CLASS_EXTENSION = ".class"

fun gameClassesInPackage(packagePath: String): List<String> {
    return findGameClassNames { _, entry -> entry.name.startsWith(packagePath) }
}

fun gameClassesMentioning(text: String): List<String> {
    return findGameClassNames { jar, entry -> mentions(jar, entry, text) }
}

private fun findGameClassNames(matches: (ZipFile, ZipEntry) -> Boolean): List<String> {
    val jarFile = gameJarFile() ?: return emptyList()
    return try {
        ZipFile(jarFile).use { jar ->
            jar.entries().asSequence()
                .filter { it.name.endsWith(CLASS_EXTENSION) && matches(jar, it) }
                .map { it.name.removeSuffix(CLASS_EXTENSION).replace('/', '.') }
                .toList()
        }
    } catch (exception: IOException) {
        emptyList()
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

private fun mentions(jar: ZipFile, entry: ZipEntry, text: String): Boolean {
    val content = jar.getInputStream(entry).use { String(it.readBytes(), Charsets.ISO_8859_1) }
    return content.contains(text)
}
