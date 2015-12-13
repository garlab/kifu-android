package fr.narwhals.go.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import fr.narwhals.go.sgf.SgfWriter;

public class Move implements Serializable {
	private static final long serialVersionUID = -7210252052767014950L;
	
	private Stone stone; // B/W
	private Point ko = Point.NO_KO; // KO
	private transient String comment = ""; // C
	private transient List<Label> labels = null; // LB
	private transient List<Point> circles = null; // CR
	private transient List<Point> squares = null; // SQ
	private transient List<Point> triangles = null; // TR
	private transient List<Point> empties = null; // AE
	private transient List<Point> marked = null; // MA

	public Move(Stone stone) {
		this.stone = stone;
	}
	
	public void toSgf(SgfWriter writer) throws IOException {
		writer.write(stone.getColor().getKey(), stone.getPoint());
		if (ko != Point.NO_KO) {
			writer.write("KO", ko);
		}
	}

	public Stone getStone() {
		return stone;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<Point> getEmpties() {
		return empties;
	}

	public void setEmpties(List<Point> empties) {
		this.empties = empties;
	}

	public List<Point> getMarked() {
		return marked;
	}

	public void setMarked(List<Point> marked) {
		this.marked = marked;
	}

	public Point getKo() {
		return ko;
	}

	public void setKo(Point ko) {
		this.ko = ko;
	}

	public void setStone(Stone stone) {
		this.stone = stone;
	}

	public List<Label> getLabels() {
		return this.labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	public boolean hasCircles() {
		return this.circles != null;
	}

	public List<Point> getCircles() {
		return this.circles;
	}

	public void setCircles(List<Point> circles) {
		this.circles = circles;
	}

	public boolean hasSquares() {
		return this.squares != null;
	}

	public List<Point> getSquares() {
		return this.squares;
	}

	public void setSquares(List<Point> squares) {
		this.squares = squares;
	}

	public boolean hasTriangles() {
		return this.triangles != null;
	}

	public List<Point> getTriangles() {
		return this.triangles;
	}

	public void setTriangles(List<Point> triangles) {
		this.triangles = triangles;
	}
}
