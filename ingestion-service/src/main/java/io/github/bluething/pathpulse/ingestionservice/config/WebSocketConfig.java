package io.github.bluething.pathpulse.ingestionservice.config;

import io.github.bluething.pathpulse.ingestionservice.websocket.LocationWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import java.util.List;
import java.util.Map;

@Configuration
class WebSocketConfig {
    private static final String WS_LOCATION_PATH = "/ws/location";
    private static final int MAX_FRAME_PAYLOAD_BYTES = 64 * 1024;

    @Bean
    public HandlerMapping webSocketHandlerMapping(LocationWebSocketHandler handler) {
        var mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of(WS_LOCATION_PATH, handler));
        mapping.setOrder(-1);

        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("*"));

        cors.addAllowedHeader("*");
        cors.addAllowedMethod("*");

        mapping.setCorsConfigurations(Map.of(WS_LOCATION_PATH, cors));
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }

    @Bean
    public WebSocketService webSocketService() {
        // Best Practice: Explicitly configure Netty Request Upgrade Strategy to limit frame payload length
        // This prevents malicious clients from sending overly large frames that consume excessive memory
        var strategy = new ReactorNettyRequestUpgradeStrategy(
                () -> reactor.netty.http.server.WebsocketServerSpec.builder().maxFramePayloadLength(MAX_FRAME_PAYLOAD_BYTES)
        );
        return new HandshakeWebSocketService(strategy);
    }
}
