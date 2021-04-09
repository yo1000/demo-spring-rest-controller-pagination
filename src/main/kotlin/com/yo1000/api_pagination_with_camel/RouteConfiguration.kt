package com.yo1000.api_pagination_with_camel

import org.apache.camel.builder.RouteBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@Configuration
class RouteConfiguration {
    @Bean
    fun pagedMemberListRoute(): RouteBuilder {
        return object : RouteBuilder() {
            override fun configure() {
                from("direct://pagedMemberList")
                    .process { exchange ->
                        // Get Params
                        val pageable: Pageable = exchange.message.getBody(Pageable::class.java)

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
                            pagedMembers.size.toLong()
                        )
                    }
            }
        }
    }
}
