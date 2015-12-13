package fr.narwhals.go.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Stone extends Section {
	private static final long serialVersionUID = 1170815563861610550L;

	public static final Stone PASS = new Stone(SColor.NONE, new Point(-1, -1), null);

	private StoneGroup stoneGroup = null;
	private List<Stone> capturedStones = null;
	private int round = -1;

	public Stone(SColor color, Point point, Goban goban) {
		super(color, point, goban);
	}

	// Getters

	public StoneGroup getStoneGroup() {
		return this.stoneGroup;
	}

	public List<Stone> getCapturedStones() {
		return capturedStones;
	}

	public int getRound() {
		return round;
	}

	// Liberties

	/**
	 * Renvoit les libertées adjacente à la pierre.
	 */
	public List<Liberty> getLiberties() {
		List<Liberty> liberties = new ArrayList<Liberty>(4);
		for (Section neighbor : goban.getNeighbors(getPoint())) {
			if (neighbor instanceof Liberty) {
				liberties.add((Liberty) neighbor);
			}
		}
		return liberties;
	}

	/**
	 * Renvoit les libertées qui seront adjacente au groupe une fois cette
	 * pierre posée.
	 */
	public List<Liberty> getActualLiberties() {
		List<Liberty> liberties = getLiberties();
		for (StoneGroup neighbor : getSameColorGroupNeighbors()) {
			for (Liberty liberty : neighbor.getLiberties()) {
				if (!liberties.contains(liberty)) {
					liberties.add(liberty);
				}
			}
		}
		liberties.remove(goban.getLiberty(getPoint()));
		return liberties;
	}

	/**
	 * Renvoit les libertées unique de chaque groupe adjacent.
	 */
	public List<Liberty> getActualNeighborLiberties() {
		List<Liberty> liberties = new LinkedList<Liberty>();
		for (StoneGroup neighbor : getSameColorGroupNeighbors()) {
			for (Liberty liberty : neighbor.getLiberties()) {
				if (!liberties.contains(liberty)) {
					liberties.add(liberty);
				}
			}
		}
		return liberties;
	}

	// Stones

	public List<Stone> getSameColorNeighbors() {
		List<Stone> neighbors = new ArrayList<Stone>(4);
		for (Section section : goban.getNeighbors(getPoint())) {
			if (section instanceof Stone) {
				Stone neighbor = (Stone) section;
				if (neighbor.getColor().equals(getColor())) {
					neighbors.add(neighbor);
				}
			}
		}
		return neighbors;
	}

	// StoneGroups

	public List<StoneGroup> getSameColorGroupNeighbors() {
		List<StoneGroup> neighbors = new ArrayList<StoneGroup>(4);
		for (Section section : goban.getNeighbors(getPoint())) {
			if (section instanceof Stone) {
				Stone neighbor = (Stone) section;
				if (neighbor.getColor().equals(getColor()) && !neighbors.contains(neighbor.getStoneGroup())) {
					neighbors.add(neighbor.getStoneGroup());
				}
			}
		}
		return neighbors;
	}

	public List<StoneGroup> getGroupNeighbors() {
		List<StoneGroup> neighbors = new ArrayList<StoneGroup>(4);
		for (Section section : goban.getNeighbors(getPoint())) {
			if (section instanceof Stone) {
				Stone neighbor = (Stone) section;
				if (!neighbors.contains(neighbor.getStoneGroup())) {
					neighbors.add(neighbor.getStoneGroup());
				}
			}
		}
		return neighbors;
	}

	// Indicateurs

	public int getCaptureValue() {
		int value = 0;
		for (StoneGroup neighbor : getGroupNeighbors()) {
			if (neighbor.getColor().equals(getOpponentColor()) && neighbor.getNumberOfLiberties() == 1) {
				value += neighbor.getNumberOfStones();
			}
		}
		return value;
	}

	public int getNumberOfLiberties() {
		return getLiberties().size();
	}

	public int getActualNumberOfLiberties() {
		return getActualLiberties().size();
	}

	public int getActualNumberOfNeighborLiberties() {
		return getActualNeighborLiberties().size();
	}

	public boolean isPotentialKo() {
		return getStoneGroup().getNumberOfStones() == 1 && getNumberOfLiberties() == 1;
	}

	public boolean isMoveValid() {
		if (!goban.isLiberty(getPoint())) {
			return false;
		} else {
			return getActualNumberOfLiberties() > 0 || getCaptureValue() > 0;
		}
	}

	// Setters

	/**
	 * Utilisé par StoneGroup lors des merge
	 */
	public void setStoneGroup(StoneGroup stoneGroup) {
		this.stoneGroup = stoneGroup;
	}

	/**
	 * Appellé par History, permet ensuite de numéroter le coup à l'IHM
	 */
	public void setRound(int round) {
		this.round = round;
	}

	/**
	 * Methode appellée par goban pour poser la pierre, aprés avoir checké que
	 * le coup est légal
	 */
	public void put() {
		capturedStones = new LinkedList<Stone>();
		reput();
	}

	/**
	 * Methode appellée par put ou undo, pour poser une pierre qui avait été
	 * capturée par le passé.
	 */
	public void reput() {
		removeNeighborLiberty();
		setStoneGroup(new StoneGroup(this));
		add();
		getStoneGroup().merge(getSameColorGroupNeighbors());
	}

	/**
	 * Appellé par reput et merge Permet de supprimmer les libertées des groupes
	 * enemis adjacent, et de capturer ceux dont les libertés == 0
	 */
	public void removeNeighborLiberty() {
		Liberty liberty = goban.getLiberty(getPoint());
		for (StoneGroup neighbor : getGroupNeighbors()) {
			if (!neighbor.getColor().equals(getColor())) {
				neighbor.remove(liberty);
				if (neighbor.getNumberOfLiberties() == 0) {
					capturedStones.addAll(neighbor.getStones());
				}
			}
		}
	}

	/**
	 * Appellé par history. Annule le coup précédent
	 */
	public void undo() {
		addNeighborLiberty();
		for (Stone captured : getCapturedStones()) {
			captured.reput();
		}
		split();
		setStoneGroup(null);
	}

	/**
	 * Appellé par undo et stoneGroup.capture Rajoute des libertés au groups
	 * adjacent du au retrait de la pierre
	 */
	public void addNeighborLiberty() {
		Liberty liberty = new Liberty(SColor.NONE, getPoint(), goban);
		for (StoneGroup neighbor : getGroupNeighbors()) {
			neighbor.add(liberty);
		}
		liberty.add();
	}

	/**
	 * Appellé par undo. Divise les groupes aprés le retrait d'une pierre
	 * faisant la liaison.
	 */
	private void split() {
		for (Stone neighbor : getSameColorNeighbors()) {
			if (neighbor.getStoneGroup() == getStoneGroup()) {
				neighbor.setStoneGroup(new StoneGroup(neighbor));
				neighbor.getStoneGroup().rebuild();
			}
		}
	}
}
