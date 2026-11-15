package com.dp.advancedgunnerycontrol.utils

import com.fs.starfarer.combat.entities.Ship
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

val gameLookup: MethodHandles.Lookup = MethodHandles.lookup()

fun loadGameClass(name: String): Class<*> = Class.forName(name, false, Ship::class.java.classLoader)

fun signature(returnType: Class<*>, vararg parameterTypes: Class<*>): MethodType {
    return MethodType.methodType(returnType, parameterTypes.toList())
}

val floatType: Class<*> = java.lang.Float.TYPE
val intType: Class<*> = Integer.TYPE
val booleanType: Class<*> = java.lang.Boolean.TYPE
val voidType: Class<*> = Void.TYPE
