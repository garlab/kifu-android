package fr.narwhals.go.domain;

import java.io.Serializable;

public class Label implements Serializable {
	private static final long serialVersionUID = 2241672162747889572L;
	
	private String label;
	public final Point point;

	public Label(Point point, String label) {
		this.point = point;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
