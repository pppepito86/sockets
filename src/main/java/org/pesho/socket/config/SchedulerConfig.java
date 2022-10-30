package org.pesho.socket.config;

import org.pesho.socket.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Configuration
public class SchedulerConfig {

    @Autowired
    SimpMessagingTemplate template;
    
    @Autowired
    SocketService socketService;

//    @Scheduled(fixedDelay = 3000)
    public void sendAdhocMessages() {
        template.convertAndSend("/topic/user", new UserResponse("Fixed Delay Scheduler"+System.currentTimeMillis()));
    }
    
    @Scheduled(fixedDelay = 1000)
    public void sendUsers() {
    	socketService.sendUsers();
    }
    
    @Scheduled(fixedDelay = 1000)
    public void sendQuestion() {
    	socketService.sendQuestion();
    }
    
}
