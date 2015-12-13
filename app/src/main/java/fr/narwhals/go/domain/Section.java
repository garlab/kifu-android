package fr.narwhals.go.domain;

import java.io.Serializable;

/* Base commune aux Stones et Liberties */
public class Section implements Serializable {
	private static final long serialVersionUID = 2375898017319161992L;

	public enum SColor {
		BLACK, WHITE, BORDER, SHARED, NONE;

		public SColor getOpponentColor() {
			switch (this) {
			case BLACK:
				return SColor.WHITE;
			case WHITE:
				return SColor.BLACK;
			default:
				return SColor.NONE;
			}
		}
		
		public String getKey() {
			return this == BLACK ? "B" : "W";
		}
	}

	private final Point point;
	private SColor color;
	protected final Goban goban;

	public Section(SColor color, Point point, Goban goban) {
		this.color = color;
		this.point = point;
		this.goban = goban;
	}

	public Point getPoint() {
		return point;
	}

	public SColor getColor() {
		return color;
	}

	public void setColor(SColor color) {
		this.color = color;
	}

	public SColor getOpponentColor() {
		return color.getOpponentColor();
	}

	public boolean hasGoban() {
		return goban != null;
	}

	public void add() {
		goban.set(this);
	}

	@Override
	public String toString() {
		return "[point=" + point + ", color=" + color + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Section other = (Section) obj;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		return true;
	}
}
