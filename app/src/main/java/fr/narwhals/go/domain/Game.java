package fr.narwhals.go.domain;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Game implements Serializable {
	public enum Rule {
		Japanese(6.5), Chinese(7.5);
		
		final public double komi; // KM

		Rule(double komi) {
			this.komi = komi;
		}
	}

	public enum Overtime {
		Byoyomi, Canadian, Absolute
	}

	private int size;
	private int time;
	private Rule rule;

	private String name;
	private List<String> dates = new LinkedList<String>();
	private String copyright;
	private String comment;
	private String event;
	private String round;
	private String place;
	private String source;
	private String annotation;
	private String user;

	private Point[] hoshis;
	private Point[] handicaps;
	
	public Game(int size, int handicap, Rule rule) {
		setSize(size);
		setHandicap(handicap);
		setRule(rule);
		this.dates.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}

	public int getSize() {
		return size;
	}

	private void setSize(int size) {
		this.size = size;
		this.hoshis = new Point[9];

		int pos = size == 9 ? 3 : 4;
		int[] v = new int[] { pos, size / 2 + 1, size - pos + 1 };

		for (int i = 0; i < 9; ++i) {
			hoshis[i] = new Point(v[i % 3], v[i / 3]);
		}
	}

	public int getHandicap() {
		return handicaps.length;
	}

	private void setHandicap(int handicap) {
		if (handicap == 0) {
			handicaps = new Point[0];
		} else {
			handicaps = new Point[handicap];
			switch (handicap) {
			case 9:
			case 8:
				handicaps[7] = hoshis[5];
				handicaps[6] = hoshis[3];
			case 7:
			case 6:
				handicaps[5] = hoshis[7];
				handicaps[4] = hoshis[1];
			case 5:
			case 4:
				handicaps[3] = hoshis[6];
			case 3:
				handicaps[2] = hoshis[2];
			case 2:
				handicaps[1] = hoshis[8];
			case 1:
				handicaps[0] = hoshis[0];
				break;
			}

			if (handicap > 4 && handicap % 2 == 1) {
				handicaps[handicap - 1] = hoshis[4];
			}
		}
	}

	public double getKomi() {
		return getHandicap() == 0 ? 0.5 : rule.komi;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public Rule getRule() {
		return rule;
	}

	private void setRule(Rule rule) {
		this.rule = rule;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getDates() {
		return dates;
	}

	public void setDates(List<String> dates) {
		this.dates = dates;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getRound() {
		return round;
	}

	public void setRound(String round) {
		this.round = round;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Point[] getHoshis() {
		return hoshis;
	}

	public Point[] getHandicaps() {
		return handicaps;
	}
}
