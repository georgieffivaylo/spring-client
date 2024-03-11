package com.almerys.almerysclient.config.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.InMemoryWebSessionStore;
import org.springframework.web.server.session.WebSessionManager;
import org.springframework.web.server.session.WebSessionStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class AlmerysInMemoryWebSessionManager implements WebSessionManager {

    public static final Logger log = LoggerFactory.getLogger(AlmerysInMemoryWebSessionManager.class);

    private WebSessionIdResolver sessionIdResolver = new WebSessionIdResolver();
    private WebSessionStore sessionStore = new InMemoryWebSessionStore();

    public void setSessionIdResolver(WebSessionIdResolver sessionIdResolver) {
        Assert.notNull(sessionIdResolver, "WebSessionIdResolver is required");
        this.sessionIdResolver = sessionIdResolver;
    }

    public WebSessionIdResolver getSessionIdResolver() {
        return this.sessionIdResolver;
    }

    public void setSessionStore(WebSessionStore sessionStore) {
        Assert.notNull(sessionStore, "WebSessionStore is required");
        this.sessionStore = sessionStore;
    }

    @Override
    public Mono<WebSession> getSession(ServerWebExchange exchange) {
        return Mono.defer(() ->
                this.retrieveSession(exchange).switchIfEmpty(this.createWebSession())
                        .doOnNext(session ->
                                exchange.getResponse().beforeCommit(() ->
                                        this.save(exchange, session)
                                )
                        )
        );
    }

    private Mono<WebSession> createWebSession() {
        Mono<WebSession> session = this.sessionStore.createWebSession();
        if (log.isTraceEnabled()) {
            session = session.doOnNext(s ->
                    log.trace("Created new WebSession.")
            );
        }

        return session;
    }

    private Mono<WebSession> retrieveSession(ServerWebExchange exchange) {
        Flux<String> ids = Flux.fromIterable(this.getSessionIdResolver().resolveSessionIds(exchange));
        return ids.concatMap(sessionStore::retrieveSession).next();
    }

    private Mono<Void> save(ServerWebExchange exchange, WebSession session) {
        List<String> ids = this.getSessionIdResolver().resolveSessionIds(exchange);
        if (session.isStarted() && !session.isExpired()) {
            if (ids.isEmpty() || !session.getId().equals(ids.get(0))) {
                this.sessionIdResolver.setSessionId(exchange, session.getId());
            }

            if (session.getAttribute(WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME) != null) {
                exchange.getResponse().getHeaders().put("X-Last-Accessed-Time",
                        Collections.singletonList(Long.toString(session.getLastAccessTime().getEpochSecond()))
                );
            }

            return session.save();
        } else {
            if (!ids.isEmpty()) {
                if (log.isTraceEnabled()) {
                    log.trace("WebSession expired or has been invalidated ID: " + session.getId());
                }

                this.sessionIdResolver.expireSession(exchange);
            }

            return Mono.empty();
        }
    }
}
