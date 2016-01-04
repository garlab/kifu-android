package fr.narwhals.go.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import fr.narwhals.go.domain.Section.SColor;

public class Goban implements Serializable {

    private final Point[] hoshis;
    private final Section[][] board;
    private transient List<Territory> territories = Collections.emptyList();
    private transient List<StoneGroup> stoneGroups;

    public Goban(int size) {
        this.hoshis = getHoshis(size);
        this.board = new Section[size + 2][size + 2];
        Section sentinel = new Section(SColor.BORDER, new Point(0, 0), this);
        for (int i = 0; i < board.length; ++i) {
            board[0][i] = sentinel;
            board[board.length - 1][i] = sentinel;
            board[i][0] = sentinel;
            board[i][board.length - 1] = sentinel;
        }
        clear();
    }

    public void clear() {
        for (int i = 1; i < board.length - 1; ++i) {
            for (int j = 1; j < board.length - 1; ++j) {
                board[i][j] = new Liberty(SColor.NONE, new Point(i, j), this);
            }
        }
        territories = Collections.emptyList();
        stoneGroups = null;
    }

    private static Point[] getHoshis(int size) {
        Point[] hoshis = new Point[9];
        int pos = size == 9 ? 3 : 4;
        int[] v = { pos, size / 2 + 1, size - pos + 1 };

        for (int i = 0; i < 9; ++i) {
            hoshis[i] = new Point(v[i % 3], v[i / 3]);
        }

        return hoshis;
    }

    public Point[] getHoshis() {
        return hoshis;
    }

    // Section

    public void set(Section section) {
        int x = section.getPoint().getX();
        int y = section.getPoint().getY();

        board[x][y] = section;
    }

    public Section[] getNeighbors(Point point) {
        Section[] neighbors = new Section[4];
        int x = point.getX();
        int y = point.getY();

        neighbors[0] = board[x + 1][y];
        neighbors[1] = board[x][y + 1];
        neighbors[2] = board[x - 1][y];
        neighbors[3] = board[x][y - 1];

        return neighbors;
    }

    // Stones

    public List<Stone> getStones() {
        List<Stone> stones = new LinkedList<Stone>();
        for (int i = 1; i < board.length - 1; ++i) {
            for (int j = 1; j < board.length - 1; ++j) {
                if (board[i][j] instanceof Stone) {
                    stones.add((Stone) board[i][j]);
                }
            }
        }
        return stones;
    }

    // Stones

    public Stone getStone(Point point) {
        int x = point.getX();
        int y = point.getY();
        return board[x][y] instanceof Stone ? (Stone) board[x][y] : null;
    }

    // Liberties

    public List<Liberty> getAllLiberties() {
        List<Liberty> allLiberties = new ArrayList<Liberty>();
        for (int i = 1; i < board.length - 1; ++i) {
            for (int j = 1; j < board.length - 1; ++j) {
                if (board[i][j] instanceof Liberty) {
                    allLiberties.add((Liberty) board[i][j]);
                }
            }
        }
        return allLiberties;
    }

    public List<Liberty> getShuffledLiberties() {
        List<Liberty> shuffledLiberties = getAllLiberties();
        Collections.shuffle(shuffledLiberties);
        return shuffledLiberties;
    }

    public Liberty getLiberty(Point point) {
        int x = point.getX();
        int y = point.getY();
        return board[x][y] instanceof Liberty ? (Liberty) board[x][y] : null;
    }

    public boolean isLiberty(Point point) {
        int x = point.getX();
        int y = point.getY();
        return board[x][y] instanceof Liberty;
    }

    // StoneGroup

    public List<StoneGroup> getStoneGroups() {
        if (stoneGroups == null) {
            computeStoneGroups();
        }
        return stoneGroups;
    }

    private void computeStoneGroups() {
        // TODO replace by a Set
        stoneGroups = new ArrayList<StoneGroup>();
        for (Stone stone : getStones()) {
            if (!stoneGroups.contains(stone.getStoneGroup())) {
                stoneGroups.add(stone.getStoneGroup());
            }
        }
    }

    // Territory

    public List<Territory> getTerritories() {
        return territories;
    }

    public void comptuteTerritories() {
        this.territories = new LinkedList<>();
        for (Liberty liberty : getAllLiberties()) {
            if (!liberty.hasTerritory()) {
                Territory territory = new Territory();
                territory.findAllLiberties(this, liberty);
                territories.add(territory);
            }
        }
    }
}
