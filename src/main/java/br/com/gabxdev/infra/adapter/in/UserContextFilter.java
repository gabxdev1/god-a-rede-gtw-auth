package br.com.gabxdev.infra.adapter.in;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)

                .flatMap(authentication -> {

                    String userId =
                            authentication
                                    .getToken()
                                    .getSubject();

                    ServerHttpRequest request =
                            exchange.getRequest()
                                    .mutate()
                                    .header(
                                            "X-User-Id",
                                            userId
                                    )
                                    .build();

                    return chain.filter(
                            exchange.mutate()
                                    .request(request)
                                    .build()
                    );
                })

                .switchIfEmpty(
                        chain.filter(exchange)
                );
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
