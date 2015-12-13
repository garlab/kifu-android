package fr.narwhals.go.domain;

import java.io.IOException;
import java.io.Serializable;

import fr.narwhals.go.ai.AI;
import fr.narwhals.go.domain.Section.SColor;
import fr.narwhals.go.sgf.SgfWriter;

public class Player implements Serializable {
	private static final long serialVersionUID = -6922611023614575866L;
	
	private final SColor color;
	private final String name; // PB/PW
	private AI ai = null;

	public Player(SColor color, String name) {
		this.color = color;
		this.name = name;
	}
	
	public void toSgf(SgfWriter writer) throws IOException {
		writer.write("P" + color.getKey(), name);
	}
	
	public void setAI(AI ai) {
		this.ai = ai;
	}
	
	public boolean isAI() {
		return ai != null;
	}
	
	public AI getAI() {
		return ai;
	}

	public SColor getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
