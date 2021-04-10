package com.yo1000.api_pagination_with_camel

import com.fasterxml.jackson.core.type.TypeReference
import org.apache.camel.builder.RouteBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.lang.IllegalArgumentException

@Configuration
class RouteConfiguration {
    @Bean
    fun pagedMemberFromListRoute(): RouteBuilder {
        return object : RouteBuilder() {
            override fun configure() {
                from("direct://pagedMemberFromList").process { exchange ->
                    // Get Params
                    val pageable: Pageable = exchange.message.getHeaderFirst(Pageable::class)
                        ?: throw IllegalArgumentException("Pageable is missing")

                    // Process
                    val pagedMembers = Member.all.subList(
                        pageable.offset.toInt(),
                        (pageable.offset.toInt() + pageable.pageSize).let {
                            if (it < Member.all.size) it
                            else Member.all.size
                        }
                    )

                    // Set Result
                    exchange.message.body = PageImpl(
                        pagedMembers,
                        pageable,
                        Member.all.size.toLong()
                    )
                }
            }
        }
    }

    @Bean
    fun pagedMemberFromDbRoute(): RouteBuilder {
        return object : RouteBuilder() {
            override fun configure() {
                from("direct://pagedMemberFromDb")
                    .process { exchange ->
                        val pageable: Pageable = exchange.message.getHeaderFirst(Pageable::class)
                            ?: throw IllegalArgumentException("Pageable is missing")

                        exchange.message.headers["offset"] = pageable.offset
                        exchange.message.headers["pageSize"] = pageable.pageSize
                    }
                    .to("""sql://
                        SELECT
                          COUNT(name) AS total
                        FROM
                          member
                    """.replace(Regex("\\s+"), " "))
                    .process { exchange ->
                        val results: List<Map<String, Long>> = exchange.message.body as List<Map<String, Long>>
                        exchange.message.headers["total"] = results.first()["TOTAL"] as Long
                    }
                    .to("""sql://
                        SELECT
                          name
                        FROM
                          member
                        LIMIT
                            :#pageSize
                        OFFSET
                            :#offset
                    """.replace(Regex("\\s+"), " "))
                    .process { exchange ->
                        val pageable: Pageable = exchange.message.getHeaderFirst(Pageable::class)
                            ?: throw IllegalArgumentException("Pageable is missing")

                        val total: Long = exchange.message.headers["total"] as Long
                        val results: List<Map<String, Any?>> = exchange.message.body as List<Map<String, Any?>>

                        results.map {
                            Member(
                                name = it["NAME"] as String? ?: ""
                            )
                        }.let {
                            exchange.message.body = PageImpl(
                                it,
                                pageable,
                                total
                            )
                        }
                    }
            }
        }
    }

    @Bean
    fun pagedMemberFromDbSimplifiedRoute(): RouteBuilder {
        return object : RouteBuilder() {
            override fun configure() {
                from("direct://pagedMemberFromDbSimplified")
                    .process { exchange ->
                        val pageable: Pageable = exchange.message.getHeaderFirst(Pageable::class)
                            ?: throw IllegalArgumentException("Pageable is missing")

                        exchange.message.headers["offset"] = pageable.offset
                        exchange.message.headers["pageSize"] = pageable.pageSize
                    }
                    .to("""sql://
                        SELECT
                          COUNT(name) AS total
                        FROM
                          member
                    """.trimForSql())
                    .process { exchange ->
                        val results: List<Map<String, Long>> = exchange.message.getBody(object : TypeReference<List<Map<String, Long>>>() {})
                        exchange.message.headers["total"] = results.first()["TOTAL"]
                    }
                    .to("""sql://
                        SELECT
                          name
                        FROM
                          member
                        LIMIT
                            :#pageSize
                        OFFSET
                            :#offset
                    """.trimForSql())
                    .process { exchange ->
                        val pageable: Pageable = exchange.message.getHeaderFirst(Pageable::class)
                            ?: throw IllegalArgumentException("Pageable is missing")

                        val total: Long = exchange.message.getHeader("total", Long::class.java)
                        val results: List<Map<String, Any?>> = exchange.message.getBody(object : TypeReference<List<Map<String, Any?>>>() {})

                        results.map {
                            Member(
                                name = it["NAME"] as String? ?: ""
                            )
                        }.let {
                            exchange.message.body = PageImpl(
                                it,
                                pageable,
                                total
                            )
                        }
                    }
            }
        }
    }
}
