package fr.narwhals.go.domain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import fr.narwhals.go.domain.Section.SColor;

public class StoneGroup implements Serializable {
	private static final long serialVersionUID = -7425464981891746751L;

	private List<Territory> territories;
	private List<Stone> stones;
	private List<Liberty> liberties;
	private boolean dead = false;

	public StoneGroup(Stone stone) {
		this.territories = new LinkedList<Territory>();
		this.stones = new LinkedList<Stone>();
		this.liberties = new LinkedList<Liberty>();
		add(stone);
	}

	public void dump() {
		Log.d("StoneGroup.stones", String.valueOf(getNumberOfStones()));
		Log.d("StoneGroup.stones", stones.toString());
		Log.d("StoneGroup.liberties", String.valueOf(getNumberOfLiberties()));
		Log.d("StoneGroup.liberties", liberties.toString());
	}

	/**
	 * Appellé par stone Permet d'ajouter les pierres lors d'une capture
	 */
	public List<Stone> getStones() {
		return stones;
	}

	public List<Liberty> getLiberties() {
		return liberties;
	}

	public SColor getColor() {
		return stones.get(0).getColor();
	}

	public int getNumberOfLiberties() {
		return liberties.size();
	}

	public boolean hasLiberty(Liberty liberty) {
		return liberties.contains(liberty);
	}

	public int getNumberOfStones() {
		return stones.size();
	}

	public boolean isDead() {
		return dead;
	}

	public int getLibertiesAdded(Stone stone) {
		int numberOfLibertiesAdded = -1;
		for (Liberty liberty : stone.getLiberties()) {
			if (!hasLiberty(liberty)) {
				numberOfLibertiesAdded++;
			}
		}
		return numberOfLibertiesAdded;
	}

	// Setter

	public void add(Stone stone) {
		stones.add(stone);
		add(stone.getLiberties());
	}

	public void add(List<Liberty> liberties) {
		for (Liberty liberty : liberties) {
			add(liberty);
		}
	}

	public void add(Liberty liberty) {
		if (!liberties.contains(liberty)) {
			liberties.add(liberty);
		}
	}

	public void remove(Stone stone) {
		stones.remove(stone);
		refresh();
	}

	private void refresh() {
		liberties.clear();
		for (Stone stone : getStones()) {
			add(stone.getLiberties());
		}
	}

	public void remove(Liberty liberty) {
		liberties.remove(liberty);
		if (getNumberOfLiberties() == 0) {
			capture();
			liberties.clear();
		}
	}

	private void capture() {
		for (Stone removed : getStones()) {
			removed.addNeighborLiberty();
		}
		this.dead = true;
	}

	public void rebuild() {
		addNeighbors(stones.get(0));
	}

	private void addNeighbors(Stone stone) {
		for (Stone neighbor : stone.getSameColorNeighbors()) {
			if (neighbor.getStoneGroup() != this) {
				neighbor.setStoneGroup(this);
				add(neighbor);
				addNeighbors(neighbor);
			}
		}
	}

	public void merge(List<StoneGroup> merges) {
		for (StoneGroup merge : merges) {
			merge(merge);
		}
	}

	private void merge(StoneGroup groupToMerge) {
		for (Stone stone : groupToMerge.getStones()) {
			add(stone);
			stone.setStoneGroup(this);
		}
	}

	// Territory

	public void add(Territory territory) {
		territories.add(territory);
	}

	public void mark() {
		SColor opponent = getColor().getOpponentColor();
		dead = !dead;
		for (Territory territory : territories) {
			if (dead) {
				if (!territory.getColor().equals(opponent)) {
					territory.setColor(opponent);
					territory.refresh();
				}
			} else {
				if (territory.getColor().equals(opponent)) {
					territory.addColor(getColor());
					territory.refresh();
				}
			}
		}
	}

	@Override
	public String toString() {
		return super.toString() + " [stones=" + stones + ", liberties=" + liberties + "]";
	}
}
