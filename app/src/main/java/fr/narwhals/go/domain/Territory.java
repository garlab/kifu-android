package fr.narwhals.go.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.narwhals.go.domain.Section.SColor;

public class Territory implements Serializable {
	private static final long serialVersionUID = 5283424520329301912L;

	private transient List<Liberty> liberties;
	private transient List<StoneGroup> stoneGroups;
	private transient SColor color;
	private final Goban goban;

	Territory(Goban goban) {
		this.color = SColor.NONE;
		this.liberties = new ArrayList<Liberty>();
		this.stoneGroups = new ArrayList<StoneGroup>();
		this.goban = goban;
	}

	// Color

	public SColor getColor() {
		return color;
	}

	public void setColor(SColor color) {
		this.color = color;
	}

	public void addColor(SColor color) {
		if (getColor() == SColor.SHARED || color == SColor.BORDER) {
			return;
		}
		if (getColor() == SColor.NONE) {
			setColor(color);
		} else if (getColor() != color) {
			setColor(SColor.SHARED);
		}
	}

	// Liberty

	public List<Liberty> getLiberties() {
		return liberties;
	}

	/*
	 * TODO: Dégager ça et le rendre itératif (cf rapport de bug) La stack pete
	 * sur 19x19 Faire une itération sur tous les pierres, regarder que les
	 * neighbor à chaque fois, et faire des merges
	 */

	public void add(Liberty liberty) {
		liberty.setTerritory(this);
		liberties.add(liberty);
		for (Section section : goban.getNeighbors(liberty.getPoint())) {
			if (section instanceof Liberty) {
				Liberty neighbor = (Liberty) section;
				if (!neighbor.hasTerritory()) {
					add(neighbor);
				}
			} else if (section instanceof Stone) {
				Stone stone = (Stone) section;
				addColor(stone.getColor());
				add(stone.getStoneGroup());
			}
		}
	}

	// StoneGroup

	public void add(StoneGroup stoneGroup) {
		if (!stoneGroups.contains(stoneGroup)) {
			stoneGroups.add(stoneGroup);
			stoneGroup.add(this);
		}
	}

	public void refresh() {
		SColor color = getColor();
		setColor(SColor.NONE);
		for (StoneGroup stoneGroup : stoneGroups) {
			if (color == SColor.SHARED) {
				addColor(stoneGroup.getColor());
				if (stoneGroup.isDead()) {
					stoneGroup.mark();
				}
			} else {
				if (stoneGroup.getColor() == color) {
					if (stoneGroup.isDead()) {
						stoneGroup.mark();
					}
				} else {
					if (!stoneGroup.isDead()) {
						stoneGroup.mark();
					}
				}
			}
		}
		if (getColor() == SColor.NONE) {
			setColor(color);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " [liberties=" + liberties + "]";
	}

}
