package com.example.demo.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class TenantIdHeaderFilter extends AbstractGatewayFilterFactory<Object> {

	@Override
	public GatewayFilter apply(Object config) {
		return (ServerWebExchange exchange, GatewayFilterChain chain) -> {
			String tenantId = exchange.getRequest().getHeaders().getFirst("tenant-id");
			if (tenantId != null) {
				exchange.getRequest().mutate().header("X-Tenant-ID", tenantId).build();
			}
			return chain.filter(exchange);
		};
	}
}
