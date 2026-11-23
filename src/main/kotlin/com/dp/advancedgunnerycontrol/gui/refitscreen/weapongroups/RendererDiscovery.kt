package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.utils.DeclaredMethod
import com.dp.advancedgunnerycontrol.utils.booleanType
import com.dp.advancedgunnerycontrol.utils.declaredMethods
import com.dp.advancedgunnerycontrol.utils.floatType
import com.dp.advancedgunnerycontrol.utils.gameLookup
import com.dp.advancedgunnerycontrol.utils.intType
import com.dp.advancedgunnerycontrol.utils.loadGameClass
import com.dp.advancedgunnerycontrol.utils.signature
import com.dp.advancedgunnerycontrol.utils.voidType
import com.fs.graphics.Sprite
import com.fs.starfarer.api.combat.WeaponAPI
import java.awt.Color

private const val RENDERERS_PACKAGE = "com.fs.starfarer.renderers."
const val TEXTURE_GETTER = "getTexture"

private val reservedWordNames = listOf(
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do",
    "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
    "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short",
    "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile",
    "while", "Object", "String"
)

private const val LOOKALIKE_CHARACTERS = "Oo0"
private const val LOOKALIKE_NAME_LENGTH = 4

private fun lookalikeNames(length: Int): List<String> {
    if (length == 0) return listOf("")
    return lookalikeNames(length - 1).flatMap { prefix -> LOOKALIKE_CHARACTERS.map { prefix + it } }
}

private val candidateClassNames = ('A'..'Z').map { it.toString() } + ('a'..'z').map { it.toString() } +
    reservedWordNames + lookalikeNames(LOOKALIKE_NAME_LENGTH)

class LineRendererMembers(val type: Class<*>, val drawEdgeName: String, val drawArcName: String)

class MountRingMembers(
    val type: Class<*>,
    val colorName: String,
    val drawName: String,
    val alphaName: String,
    val scaleName: String
)

class DiscoveredRenderers(val textureType: Class<*>, val lineRenderer: LineRendererMembers, val mountRing: MountRingMembers)

fun discoverRenderers(): DiscoveredRenderers? {
    val textureType = findTextureType() ?: return null
    val candidates = candidateClassNames.mapNotNull { loadOrNull(RENDERERS_PACKAGE + it) }
    val lineRenderer = candidates.mapNotNull { lineRendererIn(it, textureType) }.singleOrNull() ?: return null
    val mountRing = candidates.mapNotNull { mountRingIn(it) }.singleOrNull() ?: return null
    return DiscoveredRenderers(textureType, lineRenderer, mountRing)
}

fun findTextureType(): Class<*>? {
    return declaredMethods(Sprite::class.java)
        .singleOrNull { it.name == TEXTURE_GETTER && it.parameterTypes.isEmpty() }
        ?.returnType
}

private fun loadOrNull(className: String): Class<*>? {
    return try {
        loadGameClass(className)
    } catch (exception: ReflectiveOperationException) {
        null
    } catch (error: LinkageError) {
        null
    }
}

private fun lineRendererIn(type: Class<*>, textureType: Class<*>): LineRendererMembers? {
    val methods = declaredMethods(type).filter { it.isStatic && it.returnType == voidType }
    val drawEdge = methods.singleOrNull { it.parameterTypes == edgeParameters(textureType) } ?: return null
    val drawArc = methods.singleOrNull { it.parameterTypes == arcParameters(textureType) } ?: return null
    return LineRendererMembers(type, drawEdge.name, drawArc.name)
}

private fun mountRingIn(type: Class<*>): MountRingMembers? {
    if (!hasMountConstructor(type)) return null
    val methods = declaredMethods(type)
    val color = methods.singleOrNull(::isWeaponColorLookup) ?: return null
    val draw = methods.singleOrNull(::isMountDraw) ?: return null
    val floatSetters = methods.filter(::isFloatSetter)
    val scale = floatSetters.singleOrNull { it.name == color.name } ?: return null
    val alpha = floatSetters.singleOrNull { isAlphaFamily(it, methods) } ?: return null
    return MountRingMembers(type, color.name, draw.name, alpha.name, scale.name)
}

private fun isFloatSetter(method: DeclaredMethod): Boolean {
    return !method.isStatic && method.returnType == voidType && method.parameterTypes == listOf<Class<*>>(floatType)
}

private fun isAlphaFamily(setter: DeclaredMethod, methods: List<DeclaredMethod>): Boolean {
    val family = methods.filter { it.name == setter.name }
    return family.size == 2 && family.any { it.parameterTypes.isEmpty() && it.returnType == IntArray::class.java }
}

private fun hasMountConstructor(type: Class<*>): Boolean {
    return try {
        gameLookup.findConstructor(type, mountConstructorSignature())
        true
    } catch (exception: ReflectiveOperationException) {
        false
    }
}

fun mountConstructorSignature() = signature(
    voidType, WeaponAPI.WeaponType::class.java, WeaponAPI.WeaponSize::class.java, floatType, floatType, booleanType
)

private fun isWeaponColorLookup(method: DeclaredMethod): Boolean {
    return method.isStatic && method.returnType == Color::class.java &&
        method.parameterTypes == listOf<Class<*>>(WeaponAPI.WeaponType::class.java)
}

private fun isMountDraw(method: DeclaredMethod): Boolean {
    return !method.isStatic && method.returnType == voidType &&
        method.parameterTypes == listOf(floatType, floatType, floatType)
}

fun edgeParameters(textureType: Class<*>): List<Class<*>> {
    return listOf(
        textureType, floatType, floatType, floatType, floatType, floatType,
        Color::class.java, floatType, floatType, floatType, booleanType
    )
}

fun arcParameters(textureType: Class<*>): List<Class<*>> {
    return listOf(
        textureType, floatType, floatType, floatType, floatType, floatType, floatType,
        Color::class.java, intType, intType, floatType
    )
}
