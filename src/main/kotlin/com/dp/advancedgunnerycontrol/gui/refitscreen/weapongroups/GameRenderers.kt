package com.dp.advancedgunnerycontrol.gui.refitscreen.weapongroups

import com.dp.advancedgunnerycontrol.utils.floatType
import com.dp.advancedgunnerycontrol.utils.gameLookup
import com.dp.advancedgunnerycontrol.utils.signature
import com.dp.advancedgunnerycontrol.utils.voidType
import com.fs.graphics.Sprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSlotAPI
import java.awt.Color
import java.lang.invoke.MethodHandle

private const val LINE_TEXTURE_PATH = "graphics/hud/line4x4.png"
private const val MOUNT_ALPHA_METHOD = "Ô00000"
private const val MOUNT_SCALE_METHOD = "o00000"
private const val MOUNT_RADIUS = 40f
private const val MOUNT_LINE_WIDTH = 2.5f
private const val BUILT_IN_MOUNT_ALPHA = 0.3f
private const val BUILT_IN_MOUNT_SCALE = 0.66f
private const val MAX_ALPHA = 255f

class StrokeStyle(val color: Color, val width: Float, val alpha: Float)

class ArcShape(val centerX: Float, val centerY: Float, val startAngle: Float, val endAngle: Float, val radius: Float)

private class LineHandles(val readTexture: MethodHandle, val drawEdge: MethodHandle, val drawArc: MethodHandle)

private class MountHandles(
    val create: MethodHandle,
    val color: MethodHandle,
    val setAlpha: MethodHandle,
    val setScale: MethodHandle,
    val draw: MethodHandle
)

class GameRenderers private constructor(private val lines: LineHandles, private val mounts: MountHandles) {
    private val lineTexture: Any by lazy { checkNotNull(lines.readTexture.invoke(Sprite(LINE_TEXTURE_PATH))) { "The line texture is not loaded" } }

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
            val supported = resolve()
            if (supported == null) {
                Global.getLogger(GameRenderers::class.java).warn("Weapon arcs are not drawn: the game's renderers were not found")
            }
            return supported
        }

        private fun resolve(): GameRenderers? {
            val discovered = discoverRenderers() ?: return null
            return try {
                GameRenderers(lineHandles(discovered), mountHandles(discovered.mountRing))
            } catch (exception: ReflectiveOperationException) {
                null
            }
        }

        private fun lineHandles(discovered: DiscoveredRenderers): LineHandles {
            val renderer = discovered.lineRenderer
            val textureType = discovered.textureType
            return LineHandles(
                gameLookup.findVirtual(Sprite::class.java, TEXTURE_GETTER, signature(textureType)),
                gameLookup.findStatic(renderer.type, renderer.drawEdgeName, signature(voidType, *edgeParameters(textureType).toTypedArray())),
                gameLookup.findStatic(renderer.type, renderer.drawArcName, signature(voidType, *arcParameters(textureType).toTypedArray()))
            )
        }

        private fun mountHandles(ring: MountRingMembers): MountHandles {
            return MountHandles(
                gameLookup.findConstructor(ring.type, mountConstructorSignature()),
                gameLookup.findStatic(ring.type, ring.colorName, signature(Color::class.java, WeaponAPI.WeaponType::class.java)),
                gameLookup.findVirtual(ring.type, MOUNT_ALPHA_METHOD, signature(voidType, floatType)),
                gameLookup.findVirtual(ring.type, MOUNT_SCALE_METHOD, signature(voidType, floatType)),
                gameLookup.findVirtual(ring.type, ring.drawName, signature(voidType, floatType, floatType, floatType))
            )
        }
    }
}
