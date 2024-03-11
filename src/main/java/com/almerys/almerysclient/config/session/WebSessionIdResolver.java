package com.almerys.almerysclient.config.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;

@Component
public class WebSessionIdResolver extends CookieWebSessionIdResolver {

    public static final Logger log = LoggerFactory.getLogger(AlmerysInMemoryWebSessionManager.class);

    public static final String RESPONSE_HEADER_AUTHENTICATION_EXPIRED = "X-Authentication-Expired";

    @Override
    public void expireSession(ServerWebExchange exchange) {
        super.expireSession(exchange);

        exchange.getResponse().getHeaders()
                .add(RESPONSE_HEADER_AUTHENTICATION_EXPIRED, Long.toString(1));
    }
}
