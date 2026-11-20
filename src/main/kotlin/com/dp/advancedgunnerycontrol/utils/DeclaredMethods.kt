package com.dp.advancedgunnerycontrol.utils

import java.lang.invoke.MethodHandle

private const val STATIC_MODIFIER = 0x0008

class DeclaredMethod(
    val name: String,
    val parameterTypes: List<Class<*>>,
    val returnType: Class<*>,
    val isStatic: Boolean
)

private class MethodAccess {
    private val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    val name: MethodHandle = handle("getName", String::class.java)
    val parameterTypes: MethodHandle = handle("getParameterTypes", emptyArray<Class<*>>().javaClass)
    val returnType: MethodHandle = handle("getReturnType", Class::class.java)
    val modifiers: MethodHandle = handle("getModifiers", intType)

    private fun handle(methodName: String, returnType: Class<*>): MethodHandle {
        return gameLookup.findVirtual(methodClass, methodName, signature(returnType))
    }
}

private val methodAccess by lazy { MethodAccess() }

fun declaredMethods(type: Class<*>): List<DeclaredMethod> {
    return try {
        val methods: Array<out Any> = type.declaredMethods
        methods.map { describe(it) }
    } catch (error: LinkageError) {
        emptyList()
    }
}

@Suppress("UNCHECKED_CAST")
private fun describe(method: Any): DeclaredMethod {
    val access = methodAccess
    return DeclaredMethod(
        access.name.invoke(method) as String,
        (access.parameterTypes.invoke(method) as Array<Class<*>>).toList(),
        access.returnType.invoke(method) as Class<*>,
        (access.modifiers.invoke(method) as Int) and STATIC_MODIFIER != 0
    )
}
