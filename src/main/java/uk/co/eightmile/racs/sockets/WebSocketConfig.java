package uk.co.eightmile.racs.sockets;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final HealthSocketHandler healthSocketHandler;
    private final MeshSocketHandler meshSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(healthSocketHandler, "/ws/health")
                .setAllowedOrigins("*");
        registry
                .addHandler(meshSocketHandler, "/ws/mesh")
                .setAllowedOrigins("*");
    }
}