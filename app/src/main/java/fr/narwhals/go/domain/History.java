package fr.narwhals.go.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class History {

    private Node root;
    private Node current;
    private int size;
    private int round;

    private final List<Stone> handicaps;
    public final Score score;

    public History(Score score, List<Stone> handicaps) {
        this.score = score;
        this.handicaps = handicaps;
        clear();
    }

    public int getRound() {
        return round;
    }

    public Node getRoot() {
        return root;
    }

    public Move getCurrentMove() {
        return current.move;
    }

    public boolean hasPrev() {
        return current.parent != null;
    }

    public boolean hasNext() {
        return !current.children.isEmpty();
    }

    public void clear() {
        root = new Node();
        size = 1;
        goFirst();
    }

    public void goFirst() {
        current = root;
        round = 0;
        playHandicaps();
        score.clear();
    }

    private void goPrev() {
        current = current.parent;
        round--;
    }

    private void goTo(Node target) {
        current = target;
        round++;
    }

    /* Actions IHM */

    public void playNext() {
        Move move = current.children.get(current.lastPath).move;
        Stone stone = move.getStone();
        if (stone.getPoint() != Point.PASS) {
            move(move.getStone());
        } else if (stone.getPoint() == Point.PASS) {
            pass(stone);
        }
    }

    public void move(Stone stone) {
        stone.put();
        score.addCapturedStones(stone.getColor(), stone.getCapturedStones().size());
        add(stone);
    }

    public void undo() {
        Stone stone = current.move.getStone();
        if (stone.getPoint() != Point.PASS) {
            score.reduceCapturedStones(stone.getColor(), stone.getCapturedStones().size());
            stone.addNeighborLiberty();
            stone.undo();
        }
        goPrev();
    }

    public boolean hasPassed() {
        Stone current = getCurrentMove().getStone();
        return current != null && current.getPoint() == Point.PASS;
    }

    public void pass(Stone pass) {
        add(pass);
    }

    private void playHandicaps() {
        for (Stone handicap : handicaps) {
            handicap.put();
        }
    }

    private void add(Stone stone) {
        Node node = getChild(stone);
        if (node == null) {
            node = new Node(stone, current);
            size++;
            current.lastPath = current.children.size();
            current.children.add(node);
        }
        goTo(node);
        if (stone.getPoint() != Point.PASS && stone.isPotentialKo()) {
            List<Stone> capturedStones = current.move.getStone().getCapturedStones();
            if (capturedStones.size() == 1) {
                current.move.setKo(capturedStones.get(0).getPoint());
            }
        }
    }

    private Node getChild(Stone stone) {
        int nodeId = 0;
        for (Node node : current.children) {
            if (node.move.getStone().equals(stone)) {
                current.lastPath = nodeId;
                return node;
            }
            nodeId++;
        }
        return null;
    }

    public List<Stone> getChildren() {
        List<Stone> paths = new ArrayList<Stone>(current.children.size());
        for (Node node : current.children) {
            paths.add(node.move.getStone());
        }
        return paths;
    }

    public static class Node {
        private Node parent;
        private final int level;
        private List<Node> children = new LinkedList<>();
        private Move move;
        private int lastPath = 0;

        private Node() {
            this.parent = null;
            this.level = 0;
            this.move = new Move(null);
        }

        private Node(Stone stone, Node parent) {
            this.parent = parent;
            this.level = parent.level + 1;
            this.move = new Move(stone);
        }

        public Move getMove() {
            return move;
        }

        public List<Node> getChildren() {
            return children;
        }

        public String toString() {
            String ret = "";
            for (int i = 0; i < level; ++i) {
                ret += " ";
            }
            return ret + "content: " + this.move.toString() + "; level: " + this.level;
        }
    }
}
