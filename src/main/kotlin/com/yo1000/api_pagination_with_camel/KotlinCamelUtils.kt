package com.yo1000.api_pagination_with_camel

import com.fasterxml.jackson.core.type.TypeReference
import org.apache.camel.Message

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
