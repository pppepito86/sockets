package org.pesho.socket.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private MessageChannel outChannel;

	@Autowired
	public WebSocketConfig(MessageChannel clientOutboundChannel) {
		this.outChannel = clientOutboundChannel;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry.addEndpoint("/websocket-example").addInterceptors(new HandshakeInterceptor() {
			@Override
			public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
					WebSocketHandler wsHandler, Exception exception) {
			}

			@Override
			public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
					WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
				System.out.println(request.getHeaders());
				return true;
			}

		}).setAllowedOrigins("*").withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {

			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if (StompCommand.CONNECT == accessor.getCommand()) {
					String username = accessor.getLogin();
//	              String password = accessor.getPasscode();
					accessor.setUser(new Principal() {
						@Override
						public String getName() {
							return username;
						}
					});
				}
				return message;
			}
		});

		registration.interceptors(new ExecutorChannelInterceptor() {

			@Override
			public void afterMessageHandled(Message<?> inMessage, MessageChannel inChannel, MessageHandler handler,
					Exception ex) {

				StompHeaderAccessor inAccessor = StompHeaderAccessor.wrap(inMessage);
				String receipt = inAccessor.getReceipt();
				if (receipt == null || receipt.isBlank()) {
					return;
				}

				StompHeaderAccessor outAccessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
				outAccessor.setSessionId(inAccessor.getSessionId());
				outAccessor.setReceiptId(receipt);
				outAccessor.setLeaveMutable(true);

				Message<byte[]> outMessage = MessageBuilder.createMessage(new byte[0], outAccessor.getMessageHeaders());

				System.out.println("sending receipt: " + inMessage.getHeaders());
				outChannel.send(outMessage);
			}
		});

	}

}
