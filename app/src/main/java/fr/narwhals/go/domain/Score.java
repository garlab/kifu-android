package fr.narwhals.go.domain;

import java.io.Serializable;
import java.util.List;

import fr.narwhals.go.domain.GameInfo.Rule;
import fr.narwhals.go.domain.Section.SColor;

public class Score implements Serializable {
	private final Rule rule;
	private final Info[] infos = new Info[] { new Info(), new Info() };

	public Score(Rule rule, double komi) {
		this.rule = rule;
		this.infos[SColor.WHITE.ordinal()].komi = komi;
		clear();
	}

	public void clear() {
		infos[0].clear();
		infos[1].clear();
	}

	public int getStones(SColor color) {
		return infos[color.ordinal()].stones;
	}

	public int getCapturedStones(SColor color) {
		return infos[color.ordinal()].capturedStones;
	}

	public int getTerritories(SColor color) {
		return infos[color.ordinal()].territories;
	}

	public int getMarkedDead(SColor color) {
		return infos[color.ordinal()].markedDead;
	}
	
	public double getKomi(SColor color) {
		return infos[color.ordinal()].komi;
	}

	public double getScore(SColor color) {
		if (rule == Rule.Japanese) {
			return getTerritories(color) + getKomi(color) - getCapturedStones(color.getOpponentColor()) - getMarkedDead(color);
		} else {
			return getTerritories(color) + getKomi(color) + getStones(color);
		}
	}

	public String getResult() {
		double w = getScore(SColor.WHITE);
		double b = getScore(SColor.BLACK);

		if (b == w) {
			return "Jigo";
		} else if (b > w) {
			double diff = b - w;
			return "B+" + diff;
		} else {
			double diff = w - b;
			return "W+" + diff;
		}
	}

	public void addCapturedStones(SColor color, int value) {
		infos[color.ordinal()].capturedStones += value;
	}

	public void reduceCapturedStones(SColor color, int value) {
		infos[color.ordinal()].capturedStones -= value;
	}

	public void setStoneGroups(List<StoneGroup> stoneGroups) {
		infos[0].markedDead = 0;
		infos[1].markedDead = 0;
		infos[0].stones = 0;
		infos[1].stones = 0;
		for (StoneGroup stoneGroup : stoneGroups) {
			int i = stoneGroup.getColor().ordinal();
			if (stoneGroup.isDead()) {
				infos[i].markedDead += stoneGroup.getStones().size();
			} else {
				infos[i].stones += stoneGroup.getStones().size();
			}
		}
	}

	public void setTerritories(List<Territory> territories) {
		infos[0].territories = 0;
		infos[1].territories = 0;
		for (Territory territory : territories) {
			SColor color = territory.getColor();
			if (color == SColor.BLACK || color == SColor.WHITE) {
				infos[color.ordinal()].territories += territory.getLiberties().size();
			}
		}
	}

	private static class Info implements Serializable {
		
		private int capturedStones = 0;
		private transient int territories = 0;
		private transient int stones = 0;
		private transient int markedDead = 0;
		private double komi = 0;

		void clear() {
			this.capturedStones = 0;
			this.territories = 0;
			this.stones = 0;
			this.markedDead = 0;
		}
	}
}
