package org.pesho.socket;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pesho.socket.config.SocketService;
import org.pesho.socket.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebsocketLoadTest {

	@Value("${local.server.port}")
	private int port;
	
	@Autowired
	private SocketService socketService;

	@Test
	public void test() throws Exception {
		int CONNECTIONS_COUNT = 1000;
		SingleWebsocket[] sockets = new SingleWebsocket[CONNECTIONS_COUNT];
		for (int i = 0; i < sockets.length; i++) sockets[i] = new SingleWebsocket(String.valueOf(i));
		
		for (int i = 0; i < sockets.length; i++) sockets[i].start();
		
		while (true) {
			int br = 0;
			for (int i = 0; i < sockets.length; i++) {
				if (sockets[i].isConnected()) br++;
			};
			if (br == CONNECTIONS_COUNT) break;
		}

		System.out.println("*****-----*****All sessions connected");
		assertThat(socketService.getNumberOfSessions(), is(CONNECTIONS_COUNT));
		
		Thread.sleep(1000);

		for (int i = 0; i < 10; i++) sockets[i].disconnect();
		Thread.sleep(1000);
		assertThat(socketService.getNumberOfSessions(), is(CONNECTIONS_COUNT-10));
		
		for (int i = 10; i < sockets.length; i++) sockets[i].disconnect();
	}

	private List<Transport> createTransportClient() {
		List<Transport> transports = new ArrayList<>(1);
		transports.add(new WebSocketTransport(new StandardWebSocketClient()));
		return transports;
	}

	class SingleWebsocket extends Thread {

		private String id;
		private StompSession stompSession;

		public SingleWebsocket(String id) {
			this.id = id;
		}

		@Override
		public void run() {
			try {
				WebSocketStompClient stompClient = new WebSocketStompClient(
						new SockJsClient(WebsocketLoadTest.this.createTransportClient()));
				stompClient.setMessageConverter(new MappingJackson2MessageConverter());

				String url = "http://localhost:" + port + "/websocket-example";

				StompHeaders headers = new StompHeaders();
				headers.add("login", id);
				headers.add("passcode", "pass");
				
				stompSession = stompClient.connect(url, new WebSocketHttpHeaders(), headers, 
						new StompSessionHandlerAdapter() {}).get(2, TimeUnit.SECONDS);
				stompSession.subscribe("/topic/user", new StompFrameHandler() {
					@Override
					public void handleFrame(StompHeaders headers, Object payload) {
//						System.out.println("*****" + payload);
					}

					@Override
					public Type getPayloadType(StompHeaders headers) {
						return UserResponse.class;
					}
				});
				
				stompSession.send("/app/answer", "alabalaportokala");

				while (isConnected()) Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public boolean isConnected() {
			return stompSession != null;
		}
		
		public void disconnect() {
			if (stompSession != null) {
				stompSession.disconnect();
				stompSession = null;
			}
		}
		
	}

}
