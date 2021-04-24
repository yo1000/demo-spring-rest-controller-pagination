package com.yo1000.api_pagination_with_camel

import com.fasterxml.jackson.core.type.TypeReference
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
//import org.apache.camel.impl.DefaultExchange // Camel 2.x
import org.apache.camel.support.DefaultExchange
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/member")
class MemberRestController(
	private val camelContext: CamelContext
) {
	@GetMapping
	fun get(
		@RequestParam("src", defaultValue = "list")
		src: String,
		pageable: Pageable
	): Page<Member> {
		val producerTemplate: ProducerTemplate = camelContext.createProducerTemplate()

		val exchange = producerTemplate.send(
			when (src) {
				"list" -> "direct://pagedMemberFromList"
				"db" -> "direct://pagedMemberFromDb"
				"db_simple" -> "direct://pagedMemberFromDbSimplified"
				else -> throw IllegalStateException("src value is unsupported")
			},
			DefaultExchange(camelContext).also {
				it.message.headers["pageable"] = pageable
			}
		)

		return exchange.message.getBody(object : TypeReference<Page<Member>>() {})
	}
}
