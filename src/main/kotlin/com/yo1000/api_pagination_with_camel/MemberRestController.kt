package com.yo1000.api_pagination_with_camel

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultExchange
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/member")
class MemberRestController(
	private val camelContext: CamelContext
) {
	@GetMapping
	fun get(pageable: Pageable): Page<Member> {
		val producerTemplate: ProducerTemplate = camelContext.createProducerTemplate()

		val exchange = producerTemplate.send("direct://pagedMemberList", DefaultExchange(camelContext).also {
			it.message.body = pageable
		})

		return exchange.message.getBody(Page::class.java) as Page<Member>
	}
}
