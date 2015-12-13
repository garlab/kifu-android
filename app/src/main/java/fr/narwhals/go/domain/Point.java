package fr.narwhals.go.domain;

import java.io.Serializable;

public class Point implements Serializable {
	private static final long serialVersionUID = -2116601862851801202L;
	public static final Point NO_KO = new Point(-2, -2);

	private final int x;
	private final int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Point) {
			Point p = (Point) obj;
			return p.x == x && p.y == y;
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
