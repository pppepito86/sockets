package org.pesho.socket.controller;

import java.security.Principal;
import java.util.Optional;

import org.pesho.socket.config.SocketService;
import org.pesho.socket.service.Question;
import org.pesho.socket.service.QuestionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SocketController.class);

	@Autowired
	private QuestionsService questionsService;
	
	@Autowired
	private SocketService socketService;

    @MessageMapping("/question/start")
    @SendTo("/topic/questions")
    public Question startQuestion(Principal principal, String message) {
    	LOG.info("Question started [user={}, question={}]", principal.getName(), message);
    	questionsService.setQuestion(new Question(message));
    	return questionsService.getQuestion();
    }
    
    @MessageMapping("/question/stop")
    @SendTo("/topic/questions")
    public Question stopQuestion(Principal principal, String message) {
    	LOG.info("Question stopped [user={}, question={}]", principal.getName(), message);
    	questionsService.getQuestion().setFinished(true);
    	return questionsService.getQuestion();
    }
    
    @MessageMapping("/question/clear")
    @SendTo("/topic/questions")
    public Question clearQuestion(Principal principal, String message) {
    	LOG.info("Question cleared [user={}, question={}]", principal.getName(), message);
    	questionsService.setQuestion(new Question(null));
    	return questionsService.getQuestion();
    }

    @MessageMapping("/answer")
    public void sendAnswer(Principal principal, String message) {
    	LOG.info("Answer received [user={}, answer={}]", principal.getName(), message);
    	Optional.ofNullable(questionsService.getQuestion()).ifPresent(q -> {
    		q.addAnswer(principal.getName(), message);
    		socketService.updateAnswers("admin", questionsService.getQuestion().getAnswers());
    	});
    }

}
