package org.pesho.socket.service;

import java.util.HashMap;
import java.util.Map;

public class Question {

	private String question;
	private Map<String, String> answers;
	private boolean finished;
	
	public Question(String question) {
		this.question = question;
		this.answers = new HashMap<>();
	}
	
	public void addAnswer(String username, String answer) {
		if (!answers.containsKey(username)) answers.put(username, answer);
	}
	
	public String getQuestion() {
		return question;
	}
	
	public Map<String, String> getAnswers() {
		return answers;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
}
