package org.pesho.socket.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.pesho.socket.service.QuestionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry;

@Service
public class SocketService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SocketService.class);

	@Autowired
	private SimpUserRegistry simpUserRegistry;
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private QuestionsService questionsService;

	public int getNumberOfSessions() {
	    return simpUserRegistry.getUserCount();
	}

	public void sendUsers() {
		template.convertAndSend("/topic/users", getUsers());
	}
	
	public void updateAnswers(String username, Map<String, String> answers) {
		LOG.info("Updating answers [answers={}]", answers);
		template.convertAndSendToUser(username, "/queue/answers", answers);
	}

	public List<String> getUsers() {
		return simpUserRegistry.getUsers().stream().map(u -> u.getName()).collect(Collectors.toList());
	}
	
	@Bean
	public SimpUserRegistry simpUserRegistry() {
		DefaultSimpUserRegistry simpUserRegistry = new DefaultSimpUserRegistry();
		simpUserRegistry.setOrder(0);
		return simpUserRegistry;
	}

	public void sendQuestion() {
		template.convertAndSend("/topic/questions", questionsService.getQuestion());		
	}
	
}
