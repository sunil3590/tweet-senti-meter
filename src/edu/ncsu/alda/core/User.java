package edu.ncsu.alda.core;

import java.util.HashMap;
import java.util.Map;

public class User {
	long userId;
	Map<String, AggregateEmotion> monthlySentiment = new HashMap<String, AggregateEmotion>();

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Map<String, AggregateEmotion> getMonthlySentiment() {
		return monthlySentiment;
	}

	public void setMonthlySentiment(
			Map<String, AggregateEmotion> monthlySentiment) {
		this.monthlySentiment = monthlySentiment;
	}

}
