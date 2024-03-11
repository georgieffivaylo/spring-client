package com.almerys.almerysclient.config.session;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {

        return exchange.getSession().flatMap(session -> {
            if (session == null || session.getAttribute("SPRING_SECURITY_CONTEXT") == null) {
                RedirectServerAuthenticationEntryPoint redirect = new RedirectServerAuthenticationEntryPoint(
                        "/oauth2/authorization/");
                return redirect.commence(exchange, e);
            }
            return Mono.empty();
        });
    }
}
