package com.almerys.almerysclient.config.session;

import java.util.Optional;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.web.server.session.SpringSessionWebSessionStore;
import org.springframework.web.server.session.WebSessionManager;

/**
 * Overrides session manager bean to gain more control over session lifecycle
 */
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@Configuration(
        proxyBeanMethods = false
)
public class WebSessionConfiguration {

    @Bean({"webSessionManager"})
    public WebSessionManager webSessionManager(
            Optional<ReactiveSessionRepository<? extends Session>> repositoryOptional) {

        AlmerysInMemoryWebSessionManager manager = new AlmerysInMemoryWebSessionManager();

        if (repositoryOptional.isPresent()) {
            ReactiveSessionRepository<? extends Session> repository = repositoryOptional.get();
            SpringSessionWebSessionStore<? extends Session> sessionStore = new SpringSessionWebSessionStore(
                    repository);
            manager.setSessionStore(sessionStore);
        }

        WebSessionIdResolver webSessionIdResolver = new WebSessionIdResolver();
        manager.setSessionIdResolver(webSessionIdResolver);

        return manager;
    }

}
