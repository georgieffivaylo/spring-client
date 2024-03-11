package com.almerys.almerysclient;

import com.almerys.almerysclient.config.session.AlmerysInMemoryWebSessionManager;
import com.almerys.almerysclient.config.session.WebSessionIdResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.session.web.server.session.SpringSessionWebSessionStore;
import org.springframework.web.server.session.WebSessionManager;

import java.util.Optional;

@SpringBootApplication
@EnableRedisWebSession
public class AlmerysClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlmerysClientApplication.class, args);
	}

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb
				.routes()
				.route(rs -> rs
						.path("/")
						.filters(GatewayFilterSpec::tokenRelay)
						.uri("http://localhost:8080"))
				.build();
	}

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
