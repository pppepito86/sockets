package org.pesho.socket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Order(Integer.MAX_VALUE)
public class WebSocketEventListener {
    
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketEventListener.class);

	@Autowired
	private SocketService socketService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    	StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
    	LOG.info("Websocket connected [user={}, sessionid={}]", headers.getUser().getName(), headers.getSessionId());
    	socketService.sendUsers();
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    	StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
    	if (headers.getUser() == null) return;
    	LOG.info("Websocket disconnected [user={}, sessionid={}]", headers.getUser().getName(), headers.getSessionId());
    	socketService.sendUsers();
    }

}
