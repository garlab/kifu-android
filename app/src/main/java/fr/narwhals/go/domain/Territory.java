package fr.narwhals.go.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import fr.narwhals.go.domain.Section.SColor;

public class Territory implements Serializable {

	private transient List<Liberty> liberties = new ArrayList<Liberty>();
	private transient List<StoneGroup> stoneGroups = new ArrayList<StoneGroup>();
	private transient SColor color = SColor.NONE;

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

	/**
     * Find all connected liberties, starting from the given first liberty.
     * Each connected liberties are traversed using a BFS.
	 */
	public void findAllLiberties(Goban goban, Liberty first) {
        Set<Liberty> visited = new HashSet<>();
        Queue<Liberty> queue = new LinkedList<>();
        queue.add(first);

        while (!queue.isEmpty()) {
            Liberty liberty = queue.remove();
            liberty.setTerritory(this);
            liberties.add(liberty);
            for (Section section : goban.getNeighbors(liberty.getPoint())) {
                if (section instanceof Liberty) {
                    Liberty neighbor = (Liberty) section;
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                } else if (section instanceof Stone) {
                    Stone stone = (Stone) section;
                    addColor(stone.getColor());
                    findAllLiberties(stone.getStoneGroup());
                }
            }
        }
	}

	// StoneGroup

	public void findAllLiberties(StoneGroup stoneGroup) {
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
