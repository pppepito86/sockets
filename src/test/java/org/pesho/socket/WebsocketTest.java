package org.pesho.socket;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pesho.socket.model.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

//@RunWith(SpringRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebsocketTest {

    @Value("${local.server.port}")
    private int port;
	
	@Test
	public void test() throws Exception {
		WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		String url = "http://localhost:"+port+"/websocket-example";
		StompSession stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS);
		System.out.println(stompSession);
		stompSession.subscribe("/topic/user", new StompFrameHandler() {
			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				System.out.println("*****" + payload);
			}
			
			@Override
			public Type getPayloadType(StompHeaders headers) {
				System.out.println("1****" + headers);
				return UserResponse.class;
			}
		});
		Thread.sleep(10000);
	}
	
    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

}
