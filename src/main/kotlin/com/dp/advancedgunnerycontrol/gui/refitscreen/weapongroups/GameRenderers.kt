package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.utils.booleanType
import com.dp.advancedgunnerycontrol.utils.floatType
import com.dp.advancedgunnerycontrol.utils.gameLookup
import com.dp.advancedgunnerycontrol.utils.intType
import com.dp.advancedgunnerycontrol.utils.loadGameClass
import com.dp.advancedgunnerycontrol.utils.signature
import com.dp.advancedgunnerycontrol.utils.voidType
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSlotAPI
import java.awt.Color
import java.lang.invoke.MethodHandle

private const val LINE_TEXTURE_PATH = "graphics/hud/line4x4.png"
private const val MOUNT_ALPHA_METHOD = "Ô00000"
private const val RENDER_METHOD = "o00000"
private const val MOUNT_RADIUS = 40f
private const val MOUNT_LINE_WIDTH = 2.5f
private const val BUILT_IN_MOUNT_ALPHA = 0.3f
private const val BUILT_IN_MOUNT_SCALE = 0.66f
private const val MAX_ALPHA = 255f

private class RendererClassNames(
    val textureLoader: String,
    val textureLoaderMethod: String,
    val texture: String,
    val lineRenderer: String,
    val lineRendererMethod: String,
    val mountRing: String
)

private val knownRendererClassNames = listOf(
    RendererClassNames(
        "com.fs.graphics.oOoO", "Ò00000", "com.fs.graphics.Object",
        "com.fs.starfarer.renderers.E", RENDER_METHOD, "com.fs.starfarer.renderers.M"
    ),
    RendererClassNames(
        "com.fs.graphics.H", "Ò00000", "com.fs.graphics.C",
        "com.fs.starfarer.renderers.E", RENDER_METHOD, "com.fs.starfarer.renderers.L"
    ),
    RendererClassNames(
        "com.fs.graphics.null", "new", "com.fs.graphics.C",
        "com.fs.starfarer.renderers.class", RENDER_METHOD, "com.fs.starfarer.renderers.M"
    ),
    RendererClassNames(
        "com.fs.graphics.G", "Ò00000", "com.fs.graphics.Texture",
        "com.fs.starfarer.renderers.E", "super", "com.fs.starfarer.renderers.M"
    )
)

class StrokeStyle(val color: Color, val width: Float, val alpha: Float)

class ArcShape(val centerX: Float, val centerY: Float, val startAngle: Float, val endAngle: Float, val radius: Float)

private class LineHandles(val loadTexture: MethodHandle, val drawEdge: MethodHandle, val drawArc: MethodHandle)

private class MountHandles(
    val create: MethodHandle,
    val color: MethodHandle,
    val setAlpha: MethodHandle,
    val setScale: MethodHandle,
    val draw: MethodHandle
)

class GameRenderers private constructor(private val lines: LineHandles, private val mounts: MountHandles) {
    private val lineTexture: Any by lazy { lines.loadTexture.invoke(LINE_TEXTURE_PATH) }

    fun weaponColor(type: WeaponAPI.WeaponType): Color = mounts.color.invoke(type) as Color

    fun drawEdge(fromX: Float, fromY: Float, toX: Float, toY: Float, style: StrokeStyle) {
        lines.drawEdge.invoke(lineTexture, fromX, fromY, toX, toY, style.width, style.color, 0f, style.alpha, 0f, false)
    }

    fun drawArc(shape: ArcShape, style: StrokeStyle) {
        val alpha = (MAX_ALPHA * style.alpha).toInt()
        lines.drawArc.invoke(
            lineTexture, shape.centerX, shape.centerY, shape.startAngle, shape.endAngle, shape.radius,
            style.width, style.color, alpha, 0, 1f
        )
    }

    fun drawMount(slot: WeaponSlotAPI, centerX: Float, centerY: Float, zoom: Float, alpha: Float) {
        val mount = mounts.create.invoke(slot.weaponType, slot.slotSize, MOUNT_RADIUS, MOUNT_LINE_WIDTH, slot.isBuiltIn)
        if (slot.isBuiltIn) {
            mounts.setAlpha.invoke(mount, BUILT_IN_MOUNT_ALPHA)
            mounts.setScale.invoke(mount, zoom * BUILT_IN_MOUNT_SCALE)
        } else {
            mounts.setScale.invoke(mount, zoom)
        }
        mounts.draw.invoke(mount, centerX, centerY, alpha)
    }

    companion object {
        val instance: GameRenderers? by lazy { findSupported() }

        private fun findSupported(): GameRenderers? {
            val supported = knownRendererClassNames.firstNotNullOfOrNull { resolve(it) }
            if (supported == null) {
                Global.getLogger(GameRenderers::class.java).warn("Weapon arcs are not drawn: unsupported Starsector version")
            }
            return supported
        }

        private fun resolve(names: RendererClassNames): GameRenderers? {
            return try {
                val textureType = loadGameClass(names.texture)
                GameRenderers(lineHandles(names, textureType), mountHandles(loadGameClass(names.mountRing)))
            } catch (exception: ReflectiveOperationException) {
                null
            }
        }

        private fun lineHandles(names: RendererClassNames, textureType: Class<*>): LineHandles {
            val loader = loadGameClass(names.textureLoader)
            val renderer = loadGameClass(names.lineRenderer)
            val edgeSignature = signature(
                voidType, textureType, floatType, floatType, floatType, floatType, floatType,
                Color::class.java, floatType, floatType, floatType, booleanType
            )
            val arcSignature = signature(
                voidType, textureType, floatType, floatType, floatType, floatType, floatType, floatType,
                Color::class.java, intType, intType, floatType
            )
            return LineHandles(
                gameLookup.findStatic(loader, names.textureLoaderMethod, signature(textureType, String::class.java)),
                gameLookup.findStatic(renderer, names.lineRendererMethod, edgeSignature),
                gameLookup.findStatic(renderer, names.lineRendererMethod, arcSignature)
            )
        }

        private fun mountHandles(ring: Class<*>): MountHandles {
            val constructorSignature = signature(
                voidType, WeaponAPI.WeaponType::class.java, WeaponAPI.WeaponSize::class.java, floatType, floatType, booleanType
            )
            return MountHandles(
                gameLookup.findConstructor(ring, constructorSignature),
                gameLookup.findStatic(ring, RENDER_METHOD, signature(Color::class.java, WeaponAPI.WeaponType::class.java)),
                gameLookup.findVirtual(ring, MOUNT_ALPHA_METHOD, signature(voidType, floatType)),
                gameLookup.findVirtual(ring, RENDER_METHOD, signature(voidType, floatType)),
                gameLookup.findVirtual(ring, RENDER_METHOD, signature(voidType, floatType, floatType, floatType))
            )
        }
    }
}
