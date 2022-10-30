package org.pesho.socket.service;

import org.springframework.stereotype.Service;

@Service
public class QuestionsService {
	
	private Question question = new Question(null);
	
	public void setQuestion(Question question) {
		this.question = question;
	}
	
	public Question getQuestion() {
		return question;
	}
	
}
