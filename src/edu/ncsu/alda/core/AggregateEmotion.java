package edu.ncsu.alda.core;

public class AggregateEmotion {
	String timeInterval;
	int bjpPositive = 0;
	int bjpNegative = 0;
	int bjpNeutral = 0;
	int congressPositive = 0;
	int congressNegative = 0;
	int congressNeutral = 0;
	int aapPositive = 0;
	int aapNegative = 0;
	int aapNeutral = 0;

	public String printAll() {
		StringBuilder result = new StringBuilder(timeInterval);
		result.append(",");
		result.append(bjpPositive).append(",").append(bjpNegative).append(",")
				.append(bjpNeutral).append(",");
		result.append(congressPositive).append(",").append(congressNegative)
				.append(",").append(congressNeutral).append(",");
		result.append(aapPositive).append(",").append(aapNegative).append(",")
				.append(aapNeutral).append("\n");
		return result.toString();
	}

	public int getBjpNeutral() {
		return bjpNeutral;
	}

	public void incrementBjpNeutral() {
		this.bjpNeutral += 1;
	}

	public int getCongressNeutral() {
		return congressNeutral;
	}

	public void incrementCongressNeutral() {
		this.congressNeutral += 1;
	}

	public int getAapNeutral() {
		return aapNeutral;
	}

	public void incrementAapNeutral() {
		this.aapNeutral += 1;
	}

	public String getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(String id) {
		this.timeInterval = id;
	}

	public int getBjpPositive() {
		return bjpPositive;
	}

	public void incrementBjpPositive() {
		this.bjpPositive += 1;
	}

	public int getBjpNegative() {
		return bjpNegative;
	}

	public void incrementBjpNegative() {
		this.bjpNegative += 1;
	}

	public int getCongressPositive() {
		return congressPositive;
	}

	public void incrementCongressPositive() {
		this.congressPositive += 1;
	}

	public int getCongressNegative() {
		return congressNegative;
	}

	public void incrementCongressNegative() {
		this.congressNegative += 1;
	}

	public int getAapPositive() {
		return aapPositive;
	}

	public void incrementAapPositive() {
		this.aapPositive += 1;
	}

	public int getAapNegative() {
		return aapNegative;
	}

	public void incrementAapNegative() {
		this.aapNegative += 1;
	}

}
