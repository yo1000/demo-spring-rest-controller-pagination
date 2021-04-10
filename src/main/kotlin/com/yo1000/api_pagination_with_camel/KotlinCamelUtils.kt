package com.yo1000.api_pagination_with_camel

import com.fasterxml.jackson.core.type.TypeReference
import org.apache.camel.Message
import kotlin.reflect.KClass

fun String.trimForSql(): String {
    return replace(Regex("\\s+"), " ")
}

inline fun <reified T> Message.getBody(typeRef: TypeReference<T>): T {
    if (body is T) {
        return body as T
    } else {
        throw ClassCastException()
    }
}

fun <T: Any> Message.getHeader(kclass: KClass<T>): List<T> {
    return getHeader(kclass.java)
}

fun <T: Any> Message.getHeader(clazz: Class<T>): List<T> {
    return headers.values
        .filter { it != null && clazz.isAssignableFrom(it::class.java) }
        .map { it as T }
}

inline fun <reified T> Message.getHeader(typeRef: TypeReference<T>): List<T> {
    return headers.values
        .filterIsInstance<T>()
}

fun <T: Any> Message.getHeaderFirst(kclass: KClass<T>): T? {
    return getHeader(kclass)
        .firstOrNull()
}

fun <T: Any> Message.getHeaderFirst(clazz: Class<T>): T? {
    return getHeader(clazz)
        .firstOrNull()
}

inline fun <reified T> Message.getHeaderFirst(typeRef: TypeReference<T>): T? {
    return getHeader(typeRef)
        .firstOrNull()
}
