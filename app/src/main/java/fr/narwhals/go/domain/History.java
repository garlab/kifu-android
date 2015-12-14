package fr.narwhals.go.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.narwhals.go.sgf.SgfWriter;

import android.util.Log;

public class History implements Serializable {

	private transient Node root;
	private transient Node current;
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
		if (stone.hasGoban()) {
			move(move.getStone());
		} else if (stone == Stone.PASS) {
			pass();
		}
	}

	public void move(Stone stone) {
		stone.put();
		score.addCapturedStones(stone.getColor(), stone.getCapturedStones().size());
		add(stone);
	}

	public void undo() {
		Stone stone = current.move.getStone();
		if (stone.hasGoban()) {
			score.reduceCapturedStones(stone.getColor(), stone.getCapturedStones().size());
			stone.undo();
		}
		goPrev();
	}

	public void pass() {
		add(Stone.PASS);
	}

	private void playHandicaps() {
		for (Stone handicap : handicaps) {
			handicap.put();
		}
	}

	private void add(Stone stone) {
		stone.setRound(round + 1);
		Node node = getChild(stone);
		if (node == null) {
			node = new Node(stone, current);
			size++;
			current.lastPath = current.children.size();
			current.children.add(node);
		}
		goTo(node);
		if (stone.hasGoban() && stone.isPotentialKo()) {
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

	public void toSgf(SgfWriter writer) throws IOException {
		for (Node node = root; node != null; node = getNext(node, writer)) {
			writer.beginNode();
			node.move.toSgf(writer);
		}
	}

	private static Node getNext(Node node, SgfWriter writer) throws IOException {
		while (node != null) {
			if (node.lastSer < node.children.size()) {
				node.lastSer++;
				if (node.lastSer > 0) {
					writer.beginGameTree();
				}
				Node next = node.children.get(node.lastSer);
				next.lastSer = 0;
				return next;
			}
			writer.endGameTree();
			node = node.parent;
		}
		return null;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		write();
		out.defaultWriteObject();
		writeNodes(out);
		Node c = root;
		for (int i = 0; i < current.level; ++i) {
			Stone stone = c.move.getStone();
			Log.i("current2", stone == null ? "NULL" : stone.toString());
			c = c.children.get(c.lastPath);
		}
		out.writeInt(current.level);
	}

	private void writeNodes(ObjectOutputStream out) throws IOException {
		Node node = root;
		root.lastSer = 0;
		while (node != null) {
			Log.w("writeObject", node.toString());
			out.writeObject(node);
			node = getNext(node);
		}
	}

	private void write() {
		Node node = root;
		root.lastSer = 0;
		while (node != null) {
			Log.w("print", node.toString());
			node = getNext(node);
		}
	}

	private static Node getNext(Node node) {
		while (node != null) {
			if (node.lastSer < node.children.size()) {
				Node next = node.children.get(node.lastSer++);
				next.lastPath = 0;
				return next;
			}
			node = node.parent;
		}
		return null;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		readNodes(in);
		current = root;
		int currentLevel = in.readInt();
		for (int i = 0; i < currentLevel; ++i) {
			Stone stone = current.move.getStone();
			Log.i("current", stone == null ? "NULL" : stone.toString());
			current = current.children.get(current.lastPath);
		}
		write();
	}

	private void readNodes(ObjectInputStream in) throws OptionalDataException, ClassNotFoundException, IOException {
		root = (Node) in.readObject();
		Node currentParent = root;
		for (int i = 1; i < size; ++i) {
			Node node = (Node) in.readObject();
			currentParent = getParent(currentParent, node.level);
			currentParent.children.add(node);
			currentParent = node;
			Log.w("readObject", node.toString());
		}
	}

	private Node getParent(Node currentParent, int level) {
		if (level > currentParent.level) {
			return currentParent;
		} else {
			while (currentParent.parent != null && level < currentParent.level) {
				currentParent = currentParent.parent;
			}
			return currentParent == root ? root : currentParent.parent;
		}
	}

	private static class Node implements Serializable {
		private static final long serialVersionUID = -7938261636193798354L;
		private transient Node parent;
		private final int level;
		private transient List<Node> children = new ArrayList<Node>();
		private Move move;
		private int lastPath = 0;
		private transient int lastSer = 0;

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

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			children = new ArrayList<Node>();
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
